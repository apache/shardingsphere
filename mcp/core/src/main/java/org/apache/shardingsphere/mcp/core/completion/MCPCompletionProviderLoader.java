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

package org.apache.shardingsphere.mcp.core.completion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.core.handler.MCPRequestContextTypes;

import java.util.Collection;

/**
 * MCP completion provider loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPCompletionProviderLoader {
    
    /**
     * Load completion providers.
     *
     * @return completion providers
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Collection<MCPCompletionProvider<?>> load() {
        Collection<MCPCompletionProvider> result = ShardingSphereServiceLoader.getServiceInstances(MCPCompletionProvider.class);
        for (MCPCompletionProvider<?> each : result) {
            MCPRequestContextTypes.validateContextType(each.getContextType(), each.getClass());
        }
        return (Collection) result;
    }
}
