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
import java.util.Map;

/**
 * Exception for MCP tool arguments that violate the declared input schema contract.
 */
@Getter
public final class MCPToolArgumentContractViolationException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = 2730478881513659324L;
    
    private final String toolName;
    
    private final String argumentPath;
    
    private final String category;
    
    private final String expectedType;
    
    private final List<String> allowedValues;
    
    private final Map<String, Object> suggestedArguments;
    
    public MCPToolArgumentContractViolationException(final String toolName, final String argumentPath, final String category, final String expectedType,
                                                     final List<String> allowedValues, final Map<String, Object> suggestedArguments) {
        super(createMessage(toolName, argumentPath, category, expectedType, allowedValues));
        this.toolName = toolName;
        this.argumentPath = argumentPath;
        this.category = category;
        this.expectedType = expectedType;
        this.allowedValues = allowedValues;
        this.suggestedArguments = suggestedArguments;
    }
    
    private static String createMessage(final String toolName, final String argumentPath, final String category, final String expectedType, final List<String> allowedValues) {
        if ("unknown_argument".equals(category)) {
            return String.format("%s is not a supported argument for %s.", argumentPath, toolName);
        }
        if ("invalid_enum_value".equals(category)) {
            return String.format("%s must be one of %s.", argumentPath, allowedValues);
        }
        if (!expectedType.isEmpty()) {
            return String.format("%s must be %s.", argumentPath, createExpectedTypeMessage(expectedType));
        }
        return String.format("%s does not match the input schema for %s.", argumentPath, toolName);
    }
    
    private static String createExpectedTypeMessage(final String expectedType) {
        return "integer".equals(expectedType) || "array".equals(expectedType) || "object".equals(expectedType) ? "an " + expectedType : "a " + expectedType;
    }
}
