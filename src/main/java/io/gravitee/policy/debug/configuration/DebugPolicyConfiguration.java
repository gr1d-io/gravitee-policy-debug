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
package io.gravitee.policy.debug.configuration;

import io.gravitee.policy.api.PolicyConfiguration;

/**
 * @author Alexandre (tolstenko at gr1d.io)
 * @author GR1D Team
 */
public class DebugPolicyConfiguration implements PolicyConfiguration {
    private boolean logRequestHeaders=false;
    private boolean logRequestBody=false;
    private boolean logRequestContextAttributes=false;
    private boolean logResponseHeaders=false;
    private boolean logResponseBody=false;
    private boolean logResponseContextAttributes=false;

    public boolean isLogRequestHeaders() {
        return logRequestHeaders;
    }

    public void setLogRequestHeaders(boolean logRequestHeaders) {
        this.logRequestHeaders = logRequestHeaders;
    }

    public boolean isLogRequestBody() {
        return logRequestBody;
    }

    public void setLogRequestBody(boolean logRequestBody) {
        this.logRequestBody = logRequestBody;
    }

    public boolean isLogRequestContextAttributes() {
        return logRequestContextAttributes;
    }

    public void setLogRequestContextAttributes(boolean logRequestContextAttributes) {
        this.logRequestContextAttributes = logRequestContextAttributes;
    }

    public boolean isLogResponseHeaders() {
        return logResponseHeaders;
    }

    public void setLogResponseHeaders(boolean logResponseHeaders) {
        this.logResponseHeaders = logResponseHeaders;
    }

    public boolean isLogResponseBody() {
        return logResponseBody;
    }

    public void setLogResponseBody(boolean logResponseBody) {
        this.logResponseBody = logResponseBody;
    }

    public boolean isLogResponseContextAttributes() {
        return logResponseContextAttributes;
    }

    public void setLogResponseContextAttributes(boolean logResponseContextAttributes) {
        this.logResponseContextAttributes = logResponseContextAttributes;
    }
}
