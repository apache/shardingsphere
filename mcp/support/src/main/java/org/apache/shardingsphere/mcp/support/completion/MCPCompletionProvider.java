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

package org.apache.shardingsphere.mcp.support.completion;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;

/**
 * MCP completion provider.
 *
 * @param <T> handler context type
 */
@SingletonSPI
public interface MCPCompletionProvider<T extends MCPHandlerContext> extends ShardingSphereSPI {
    
    /**
     * Get required handler context type.
     *
     * @return handler context type
     */
    Class<T> getContextType();
    
    /**
     * Judge whether this provider supports the completion request.
     *
     * @param requestContext completion request context
     * @return whether this provider supports the completion request
     */
    boolean supports(MCPCompletionRequestContext requestContext);
    
    /**
     * Complete one MCP argument.
     *
     * @param handlerContext handler context
     * @param requestContext completion request context
     * @return completion provider result
     */
    MCPCompletionProviderResult complete(T handlerContext, MCPCompletionRequestContext requestContext);
}
