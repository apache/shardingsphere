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
 * Exception for unsupported metadata object types.
 */
@Getter
public final class MCPInvalidMetadataObjectTypesException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = 9086935225120763337L;
    
    private final String actualValue;
    
    private final List<String> allowedValues;
    
    public MCPInvalidMetadataObjectTypesException(final String actualValue, final List<String> allowedValues) {
        super(String.format("Unsupported object_types value `%s`.", actualValue));
        this.actualValue = actualValue;
        this.allowedValues = allowedValues;
    }
}
