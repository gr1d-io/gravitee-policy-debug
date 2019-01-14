/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.debug;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import io.gravitee.common.http.GraviteeHttpHeader;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.BufferedReadWriteStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.SimpleReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.PolicyResult;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.debug.configuration.DebugPolicyConfiguration;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiKeyRepository;
import io.gravitee.repository.management.model.ApiKey;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.jar.JarException;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.mozilla.javascript.tools.debugger.treetable.AbstractCellEditor;
import org.json.JSONException;


/**
 * @author Alexandre (tolstenko at gr1d.io)
 * @author GR1D Team
 */
@SuppressWarnings("unused")
public class DebugPolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugPolicy.class);

    /**
     * Policy configuration
     */
    private final DebugPolicyConfiguration debugPolicyConfiguration;

    public DebugPolicy(DebugPolicyConfiguration debugPolicyConfiguration) {
        this.debugPolicyConfiguration = debugPolicyConfiguration;
    }

    @OnRequest
    public void onRequest(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        Map<String, List<Map.Entry<String,String>>> data = new HashMap<String, List<Map.Entry<String,String>>>();

        if(this.debugPolicyConfiguration.isLogRequestContextAttributes())
        {
            data.put("Context", this.processContext(executionContext));
        }

        if(this.debugPolicyConfiguration.isLogRequestHeaders())
        {
            data.put("Headers", this.processHeaders(request.headers()));
        }

        if (data.size() > 0)
        {
            String message = this.getLogMessage(data);
            this.log("onRequest", message);
            request.metrics().setMessage(request.metrics().getMessage() + "\n\n->onRequest: " + message);
        }

        policyChain.doNext(request,response);
    }


    @OnResponse
    public void onResponse(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        Map<String, List<Map.Entry<String,String>>> data = new HashMap<String, List<Map.Entry<String,String>>>();

        if(this.debugPolicyConfiguration.isLogRequestContextAttributes())
        {
            data.put("Context", this.processContext(executionContext));
        }

        if(this.debugPolicyConfiguration.isLogRequestHeaders())
        {
            data.put("Headers", this.processHeaders(response.headers()));
        }

        if (data.size() > 0)
        {
            String message = this.getLogMessage(data);
            this.log("onResponse", message);
            // request.metrics().setMessage(request.metrics().getMessage() + "\n\n->onResponse: " + message);
        }

        policyChain.doNext(request,response);
    }

    // @OnRequestContent
    // public ReadWriteStream onRequestContent(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
//        LOGGER.debug("Execute json schema validation policy on request {}", request.id());
//
//        return new BufferedReadWriteStream() {
//            Buffer buffer = Buffer.buffer();
//
//            @Override
//            public SimpleReadWriteStream<Buffer> write(Buffer content) {
//                buffer.appendBuffer(content);
//                return this;
//            }
//
//            @Override
//            public void end() {
//                try {
//                    JsonNode schema = JsonLoader.fromString(debugPolicyConfiguration.getSchema());
//                    JsonNode content = JsonLoader.fromString(buffer.toString());
//
//                    ProcessingReport report;
//
//                    if (configuration.isValidateUnchecked()) {
//                        report = validator.validateUnchecked(schema, content, configuration.isDeepCheck());
//                    } else {
//                        report = validator.validate(schema, content, configuration.isDeepCheck());
//                    }
//
//                    if (!report.isSuccess()) {
//                        request.metrics().setMessage(report.toString());
//                        sendBadRequestResponse(executionContext, policyChain);
//                    } else {
//                        super.write(buffer);
//                        super.end();
//                    }
//                } catch (Exception ex) {
//                    request.metrics().setMessage(ex.getMessage());
//                    sendBadRequestResponse(executionContext, policyChain);
//                }
//            }
//        };
    // }

    private List<Map.Entry<String,String>> processContext(ExecutionContext executionContext)
    {
        List<Map.Entry<String,String>> result = new ArrayList<Map.Entry<String,String>>();

        for (Enumeration<String> enumeration = executionContext.getAttributeNames(); enumeration.hasMoreElements(); )
        {
            String key = enumeration.nextElement();
            Object value = executionContext.getAttribute(key);
            result.add(new AbstractMap.SimpleEntry(key,value));
        }
        
        return result;
    }

    private List<Map.Entry<String,String>> processHeaders(HttpHeaders headers)
    {
        List<Map.Entry<String,String>> result = new ArrayList<Map.Entry<String,String>>();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for(String innervalue:entry.getValue()) {
                result.add(new AbstractMap.SimpleEntry(entry.getKey(), innervalue));
            }
        }
        
        return result;
    }


    private String getLogMessage(Map<String, List<Map.Entry<String,String>>> data)
    {
        String message = "{\n\t"
            + String.join(",", data
                .entrySet()
                .stream()
                .map(entry -> this.getLogMessageForCategory(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())) 
            + "\n}";

        return message;
    }

    private String getLogMessageForCategory(String category, List<Map.Entry<String, String>> entries)
    {
        String result = "\n\t\""+category+"\": {\n\t\t"+this.convertEntriesToString(entries, 2)+"\n\t}";
        return result;
    }

    private String convertEntriesToString(List<Map.Entry<String, String>> entries, int identationLevel)
    {
        String delimeter = ",\n";
        for(int i=0; i<identationLevel; i++)
        {
            delimeter += "\t";
        }
        List<String> entryValues = entries
            .stream()
            .map(entry -> "\""+entry.getKey()+"\": \""+entry.getValue()+"\"")
            .collect(Collectors.toList());
        return String.join(delimeter, entryValues);
    } 

    private void log(String type, String message)
    {
        LOGGER.warn("[DEBUG] Debug info for \"" + type + "\": " + message);
    }
}
