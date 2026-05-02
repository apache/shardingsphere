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

package org.apache.shardingsphere.mcp.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.context.MCPServiceHandlerContext;
import org.apache.shardingsphere.mcp.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.workflow.MCPWorkflowHandlerContext;

/**
 * MCP handler context utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPHandlerContexts {
    
    /**
     * Validate handler context type.
     *
     * @param contextType context type
     * @param handlerType handler type
     */
    public static void validateContextType(final Class<?> contextType, final Class<?> handlerType) {
        ShardingSpherePreconditions.checkState(isSupportedContextType(contextType),
                () -> new IllegalArgumentException(String.format("Unsupported handler context type `%s` for `%s`.", getContextTypeName(contextType), handlerType.getName())));
    }
    
    /**
     * Resolve request scope as required handler context.
     *
     * @param requestScope request scope
     * @param contextType context type
     * @param handlerType handler type
     * @param <T> type of handler context
     * @return handler context
     */
    public static <T extends MCPHandlerContext> T resolve(final MCPRequestScope requestScope, final Class<T> contextType, final Class<?> handlerType) {
        validateContextType(contextType, handlerType);
        return contextType.cast(requestScope);
    }
    
    private static boolean isSupportedContextType(final Class<?> contextType) {
        return MCPServiceHandlerContext.class == contextType || MCPDatabaseHandlerContext.class == contextType || MCPWorkflowHandlerContext.class == contextType;
    }
    
    private static String getContextTypeName(final Class<?> contextType) {
        return null == contextType ? "null" : contextType.getName();
    }
}
