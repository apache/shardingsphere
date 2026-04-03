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

package org.apache.shardingsphere.mcp.resource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;

/**
 * MCP resource response.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MCPResourceResponse {
    
    private final ResourceResponseType type;
    
    private final ServiceCapability serviceCapability;
    
    private final DatabaseCapability databaseCapability;
    
    private final MetadataResourceResult metadataResourceResult;
    
    private final MCPErrorCode errorCode;
    
    private final String message;
    
    /**
     * Create service capability response.
     *
     * @param capability service capability
     * @return resource response
     */
    public static MCPResourceResponse serviceCapability(final ServiceCapability capability) {
        return new MCPResourceResponse(ResourceResponseType.SERVICE_CAPABILITY, capability, null, null, null, null);
    }
    
    /**
     * Create database capability response.
     *
     * @param capability database capability
     * @return resource response
     */
    public static MCPResourceResponse databaseCapability(final DatabaseCapability capability) {
        return new MCPResourceResponse(ResourceResponseType.DATABASE_CAPABILITY, null, capability, null, null, null);
    }
    
    /**
     * Create metadata response.
     *
     * @param result metadata resource result
     * @return resource response
     */
    public static MCPResourceResponse metadata(final MetadataResourceResult result) {
        return new MCPResourceResponse(ResourceResponseType.METADATA, null, null, result, null, null);
    }
    
    /**
     * Create error response.
     *
     * @param errorCode error code
     * @param message error message
     * @return resource response
     */
    public static MCPResourceResponse error(final MCPErrorCode errorCode, final String message) {
        return new MCPResourceResponse(ResourceResponseType.ERROR, null, null, null, errorCode, message);
    }
    
    /**
     * Resource response type.
     */
    public enum ResourceResponseType {
        
        SERVICE_CAPABILITY,
        
        DATABASE_CAPABILITY,
        
        METADATA,
        
        ERROR
    }
}
