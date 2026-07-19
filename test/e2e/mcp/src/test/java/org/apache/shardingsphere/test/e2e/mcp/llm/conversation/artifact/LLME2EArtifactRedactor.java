/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact;

import java.util.regex.Pattern;

final class LLME2EArtifactRedactor {
    
    private static final Pattern JSON_SECRET_FIELD_PATTERN = Pattern.compile("(?i)(\"(?:api[_-]?key|token|password|authorization|secret)\"\\s*:\\s*\")([^\"]+)(\")");
    
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/=-]+");
    
    private static final Pattern ENV_SECRET_ASSIGNMENT_PATTERN = Pattern.compile("(?i)((?:MCP_LLM_API_KEY|HF_TOKEN|HUGGING_FACE_HUB_TOKEN|LLAMA_API_KEY)\\s*=\\s*)\\S+");
    
    String redact(final String value) {
        String result = JSON_SECRET_FIELD_PATTERN.matcher(value).replaceAll("$1<redacted>$3");
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1<redacted>");
        return ENV_SECRET_ASSIGNMENT_PATTERN.matcher(result).replaceAll("$1<redacted>");
    }
}
