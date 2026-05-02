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

package org.apache.shardingsphere.mcp.api.resource;

/**
 * MCP resource request.
 */
public final class MCPResourceRequest {
    
    private final String resourceUri;
    
    private final MCPUriVariables uriVariables;
    
    /**
     * Create MCP resource request.
     *
     * @param resourceUri requested resource URI
     * @param uriVariables matched URI variables
     */
    public MCPResourceRequest(final String resourceUri, final MCPUriVariables uriVariables) {
        this.resourceUri = resourceUri;
        this.uriVariables = uriVariables;
    }
    
    /**
     * Get requested resource URI.
     *
     * @return requested resource URI
     */
    public String resourceUri() {
        return resourceUri;
    }
    
    /**
     * Get matched URI variables.
     *
     * @return matched URI variables
     */
    public MCPUriVariables uriVariables() {
        return uriVariables;
    }
}
