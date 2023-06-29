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
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilder;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;

import java.sql.SQLException;

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
     * @param force force start
     * @throws SQLException SQL exception
     */
    public void init(final YamlProxyConfiguration yamlConfig, final int port, final boolean force) throws SQLException {
        ModeConfiguration modeConfig = null == yamlConfig.getServerConfiguration().getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(yamlConfig.getServerConfiguration().getMode());
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        ContextManager contextManager = createContextManager(proxyConfig, modeConfig, port, force);
        ProxyContext.init(contextManager);
        contextManagerInitializedCallback(contextManager);
        ShardingSphereProxyVersion.setVersion(contextManager);
    }
    
    private ContextManager createContextManager(final ProxyConfiguration proxyConfig, final ModeConfiguration modeConfig, final int port, final boolean force) throws SQLException {
        ContextManagerBuilderParameter param = new ContextManagerBuilderParameter(modeConfig, proxyConfig.getDatabaseConfigurations(), proxyConfig.getGlobalConfiguration().getDataSources(),
                proxyConfig.getGlobalConfiguration().getRules(), proxyConfig.getGlobalConfiguration().getProperties(), proxyConfig.getGlobalConfiguration().getLabels(),
                createInstanceMetaData(port), force);
        return TypedSPILoader.getService(ContextManagerBuilder.class, null == modeConfig ? null : modeConfig.getType()).build(param);
    }
    
    private InstanceMetaData createInstanceMetaData(final int port) {
        return TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy").build(port);
    }
    
    private void contextManagerInitializedCallback(final ContextManager contextManager) {
        for (ContextManagerLifecycleListener each : ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class)) {
            try {
                each.onInitialized(null, contextManager);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.error("contextManager onInitialized callback for '{}' failed", each.getClass().getName(), ex);
            }
        }
    }
}
