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

package org.apache.shardingsphere.proxy.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderFactory;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;
import org.apache.shardingsphere.spi.singleton.SingletonSPIRegistry;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Bootstrap initializer.
 */
@RequiredArgsConstructor
@Slf4j
public final class BootstrapInitializer {
    
    /**
     * Initialize.
     *
     * @param yamlConfig YAML proxy configuration
     * @param port proxy port
     * @throws SQLException SQL exception
     */
    public void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ModeConfiguration modeConfig = null == yamlConfig.getServerConfiguration().getMode() ? null : new ModeConfigurationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getMode());
        ContextManager contextManager = createContextManager(yamlConfig, modeConfig, port);
        ProxyContext.getInstance().init(contextManager);
        contextManagerInitializedCallback(modeConfig, contextManager);
        ShardingSphereProxyVersion.setVersion(contextManager);
    }
    
    private ContextManager createContextManager(final YamlProxyConfiguration yamlConfig, final ModeConfiguration modeConfig, final int port) throws SQLException {
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        ContextManagerBuilderParameter parameter = ContextManagerBuilderParameter.builder()
                .modeConfig(modeConfig)
                .schemaConfigs(proxyConfig.getSchemaConfigurations())
                .globalRuleConfigs(proxyConfig.getGlobalConfiguration().getRules())
                .props(proxyConfig.getGlobalConfiguration().getProperties())
                .labels(proxyConfig.getGlobalConfiguration().getLabels())
                .instanceDefinition(new InstanceDefinition(InstanceType.PROXY, port)).build();
        return ContextManagerBuilderFactory.newInstance(modeConfig).build(parameter);
    }
    
    private void contextManagerInitializedCallback(final ModeConfiguration modeConfig, final ContextManager contextManager) {
        Map<String, ContextManagerLifecycleListener> listeners = SingletonSPIRegistry.getTypedSingletonInstancesMap(ContextManagerLifecycleListener.class);
        log.info("listeners.keySet={}", listeners.keySet());
        for (Entry<String, ContextManagerLifecycleListener> entry : listeners.entrySet()) {
            try {
                entry.getValue().onInitialized(modeConfig, contextManager);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("contextManager onInitialized callback for '{}' failed", entry.getKey(), ex);
            }
        }
    }
}
