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

import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.api.protocol.exception.ShardingSphereMCPException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;

/**
 * MCP resource handler.
 *
 * @param <T> required request context type
 */
public interface MCPResourceHandler<T extends MCPRequestContext> {
    
    /**
     * Get required request context type.
     *
     * @return required request context type
     */
    Class<T> getContextType();
    
    /**
     * Get canonical resource URI template.
     *
     * <p>A fixed resource URI is represented as a URI template without variables.</p>
     *
     * @return canonical resource URI template
     */
    String getResourceUriTemplate();
    
    /**
     * Handle one resource request.
     *
     * @param context request context
     * @param uriVariables URI variables
     * @return successful resource response
     * @throws ShardingSphereMCPException controlled failure to be converted by runtime
     */
    MCPResponse handle(T context, MCPUriVariables uriVariables);
}
