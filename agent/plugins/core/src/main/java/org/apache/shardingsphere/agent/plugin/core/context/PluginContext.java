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

package org.apache.shardingsphere.agent.plugin.core.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtils;
import org.apache.shardingsphere.agent.plugin.core.util.ShardingSphereDriverUtils;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Map;
import java.util.Optional;

/**
 * Plugin Context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
public final class PluginContext {
    
    private static final PluginContext INSTANCE = new PluginContext();
    
    private ContextManager contextManager;
    
    private boolean isEnhancedForProxy;
    
    /**
     * Get instance of plugin context.
     *
     * @return instance
     */
    public static PluginContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Check if the plugin is enabled.
     *
     * @return true or false
     */
    public boolean isPluginEnabled() {
        if (null == contextManager) {
            contextManager = getContextManager().orElse(null);
        }
        return null == contextManager || contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED);
    }
    
    /**
     * Get context manager.
     *
     * @return context manager
     */
    public Optional<ContextManager> getContextManager() {
        if (isEnhancedForProxy) {
            return Optional.ofNullable(ProxyContext.getInstance().getContextManager());
        }
        Optional<Map<String, ShardingSphereDataSource>> dataSourceMap = ShardingSphereDriverUtils.findShardingSphereDataSources();
        if (dataSourceMap.isPresent() && !dataSourceMap.get().isEmpty()) {
            return Optional.ofNullable(AgentReflectionUtils.getFieldValue(dataSourceMap.get().values().iterator().next(), "contextManager"));
        }
        return Optional.empty();
    }
}
