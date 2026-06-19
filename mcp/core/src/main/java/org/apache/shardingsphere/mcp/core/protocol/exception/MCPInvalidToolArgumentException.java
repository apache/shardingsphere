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

/**
 * Exception for invalid public MCP tool arguments.
 */
@Getter
public final class MCPInvalidToolArgumentException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = -7814892563970257543L;
    
    private final String sourceTool;
    
    private final String targetTool;
    
    private final String argumentPath;
    
    private final int minimumValue;
    
    private final int maximumValue;
    
    private final int suggestedValue;
    
    public MCPInvalidToolArgumentException(final String sourceTool, final String targetTool, final String argumentPath, final int minimumValue, final int maximumValue,
                                           final int suggestedValue, final MCPInvalidRequestException cause) {
        super(String.format("%s must be an integer between %d and %d.", argumentPath, minimumValue, maximumValue), cause);
        this.sourceTool = sourceTool;
        this.targetTool = targetTool;
        this.argumentPath = argumentPath;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.suggestedValue = suggestedValue;
    }
}
