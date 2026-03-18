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

package org.apache.shardingsphere.agent.plugin.core.holder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.plugin.core.context.ShardingSphereDataSourceContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere data source context holder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDataSourceContextHolder {
    
    private static final Map<String, ShardingSphereDataSourceContext> DATA_SOURCE_CONTEXTS = new ConcurrentHashMap<>();
    
    /**
     * Put.
     *
     * @param instanceId instance ID
     * @param dataSourceContext sharding sphere data source context
     */
    public static void put(final String instanceId, final ShardingSphereDataSourceContext dataSourceContext) {
        DATA_SOURCE_CONTEXTS.put(instanceId, dataSourceContext);
    }
    
    /**
     * Remove.
     *
     * @param instanceId instance ID
     */
    public static void remove(final String instanceId) {
        DATA_SOURCE_CONTEXTS.remove(instanceId);
    }
    
    /**
     * Get sharding sphere data source contexts.
     *
     * @return sharding sphere data source contexts
     */
    public static Map<String, ShardingSphereDataSourceContext> getShardingSphereDataSourceContexts() {
        return DATA_SOURCE_CONTEXTS;
    }
}
