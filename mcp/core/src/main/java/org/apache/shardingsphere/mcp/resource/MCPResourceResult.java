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
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;

import java.util.List;

/**
 * MCP resource result.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MCPResourceResult {
    
    private final ResourceResultType type;
    
    private final ServiceCapability serviceCapability;
    
    private final DatabaseCapability databaseCapability;
    
    private final List<MetadataObject> metadataObjects;
    
    private final MCPErrorCode errorCode;
    
    private final String message;
    
    /**
     * Create service capability result.
     *
     * @param capability service capability
     * @return resource result
     */
    public static MCPResourceResult serviceCapability(final ServiceCapability capability) {
        return new MCPResourceResult(ResourceResultType.SERVICE_CAPABILITY, capability, null, null, null, null);
    }
    
    /**
     * Create database capability result.
     *
     * @param capability database capability
     * @return resource result
     */
    public static MCPResourceResult databaseCapability(final DatabaseCapability capability) {
        return new MCPResourceResult(ResourceResultType.DATABASE_CAPABILITY, null, capability, null, null, null);
    }
    
    /**
     * Create metadata result.
     *
     * @param metadataObjects metadata objects
     * @return resource result
     */
    public static MCPResourceResult metadata(final List<MetadataObject> metadataObjects) {
        return new MCPResourceResult(ResourceResultType.METADATA, null, null, metadataObjects, null, null);
    }
    
    /**
     * Create error result.
     *
     * @param errorCode error code
     * @param message error message
     * @return resource result
     */
    public static MCPResourceResult error(final MCPErrorCode errorCode, final String message) {
        return new MCPResourceResult(ResourceResultType.ERROR, null, null, null, errorCode, message);
    }
    
    /**
     * Resource result type.
     */
    public enum ResourceResultType {
        
        SERVICE_CAPABILITY,
        
        DATABASE_CAPABILITY,
        
        METADATA,
        
        ERROR
    }
}
