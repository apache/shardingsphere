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
 * Exception for invalid workflow approval steps.
 */
@Getter
public final class MCPInvalidApprovedStepsException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = -8231262912952143464L;
    
    private final List<String> allowedValues;
    
    private final Map<String, Object> suggestedArguments;
    
    public MCPInvalidApprovedStepsException(final List<String> allowedValues) {
        this(allowedValues, Map.of());
    }
    
    public MCPInvalidApprovedStepsException(final List<String> allowedValues, final Map<String, Object> suggestedArguments) {
        super(String.format("approved_steps must contain only %s.", allowedValues));
        this.allowedValues = allowedValues;
        this.suggestedArguments = suggestedArguments;
    }
}
