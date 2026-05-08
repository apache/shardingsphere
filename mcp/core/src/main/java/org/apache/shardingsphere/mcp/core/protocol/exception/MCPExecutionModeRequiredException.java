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

import java.util.List;

/**
 * Exception for missing MCP execution mode.
 */
@Getter
public final class MCPExecutionModeRequiredException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = -2508620171429867748L;
    
    private final String toolName;
    
    private final List<String> allowedValues;
    
    public MCPExecutionModeRequiredException(final String toolName, final List<String> allowedValues) {
        super(String.format("%s execution_mode is required.", toolName));
        this.toolName = toolName;
        this.allowedValues = allowedValues;
    }
}
