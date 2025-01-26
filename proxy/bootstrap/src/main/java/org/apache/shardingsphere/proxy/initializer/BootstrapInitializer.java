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

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;

import java.sql.SQLException;

/**
 * Bootstrap initializer.
 */
public final class BootstrapInitializer {
    
    /**
     * Initialize.
     *
     * @param yamlConfig YAML proxy configuration
     * @param port proxy port
     * @throws SQLException SQL exception
     */
    public void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ModeConfiguration modeConfig = null == yamlConfig.getServerConfiguration().getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(yamlConfig.getServerConfiguration().getMode());
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        ContextManager contextManager = createContextManager(proxyConfig, modeConfig, port);
        ProxyContext.init(contextManager);
        ShardingSphereProxyVersion.setVersion(contextManager);
    }
    
    private ContextManager createContextManager(final ProxyConfiguration proxyConfig, final ModeConfiguration modeConfig, final int port) throws SQLException {
        InstanceMetaData instanceMetaData = TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy").build(port, "");
        ContextManagerBuilderParameter param = new ContextManagerBuilderParameter(modeConfig, proxyConfig.getDatabaseConfigurations(), proxyConfig.getGlobalConfiguration().getDataSources(),
                proxyConfig.getGlobalConfiguration().getRules(), proxyConfig.getGlobalConfiguration().getProperties(), proxyConfig.getGlobalConfiguration().getLabels(), instanceMetaData);
        return TypedSPILoader.getService(ContextManagerBuilder.class, null == modeConfig ? null : modeConfig.getType()).build(param, new EventBusContext());
    }
}
