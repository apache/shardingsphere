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

package org.apache.shardingsphere.mcp.core.protocol.exception;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;

import java.util.Map;

/**
 * Exception for side-effecting MCP calls without explicit user approval.
 */
@Getter
public final class MCPUserApprovalRequiredException extends MCPInvalidRequestException {

    private static final long serialVersionUID = -1076855367988915199L;

    private final String toolName;

    private final Map<String, Object> suggestedArguments;

    /**
     * Create exception.
     *
     * @param toolName tool name
     * @param suggestedArguments suggested arguments
     */
    public MCPUserApprovalRequiredException(final String toolName, final Map<String, Object> suggestedArguments) {
        super(String.format("%s approved_by_user=true is required for real side effects.", toolName));
        this.toolName = toolName;
        this.suggestedArguments = suggestedArguments;
    }
}
