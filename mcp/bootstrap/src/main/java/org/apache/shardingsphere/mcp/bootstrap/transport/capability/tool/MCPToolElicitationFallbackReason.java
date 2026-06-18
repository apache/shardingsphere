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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MCP tool elicitation fallback reason.
 */
@Getter
@RequiredArgsConstructor
public enum MCPToolElicitationFallbackReason {
    
    CLIENT_UNSUPPORTED("client_unsupported", "structured_fallback"),
    
    REMOTE_IDENTITY_REQUIRED("remote_identity_required", "structured_fallback"),
    
    MISSING_PLAN_ID("missing_plan_id", "structured_fallback"),
    
    SENSITIVE_FORM_BLOCKED("sensitive_form_blocked", "url_fallback"),
    
    URL_MODE_NOT_IMPLEMENTED("url_mode_not_implemented", "url_fallback"),
    
    AMBIGUOUS_FIELD_BINDING("ambiguous_field_binding", "structured_fallback"),
    
    ELICITATION_FAILED("elicitation_failed", "structured_fallback"),
    
    MALFORMED_ELICITATION_RESULT("malformed_elicitation_result", "structured_fallback"),
    
    INVALID_ELICITED_CONTENT("invalid_elicited_content", "structured_fallback"),
    
    STALE_ELICITATION("stale_elicitation", "structured_fallback");
    
    private final String value;
    
    private final String selectedInteraction;
    
    /**
     * Adjust fallback reason according to client capabilities.
     *
     * @param clientCapabilities client elicitation capabilities
     * @return fallback reason
     */
    public MCPToolElicitationFallbackReason withClientCapabilities(final MCPClientElicitationCapabilities clientCapabilities) {
        return SENSITIVE_FORM_BLOCKED == this && clientCapabilities.isUrlModeSupported() ? URL_MODE_NOT_IMPLEMENTED : this;
    }
}
