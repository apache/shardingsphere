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
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilderFactory;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderFactory;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListenerFactory;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;

import java.sql.SQLException;
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
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        checkRuleConfiguration(proxyConfig);
        ContextManager contextManager = createContextManager(proxyConfig, modeConfig, port);
        ProxyContext.init(contextManager);
        contextManagerInitializedCallback(modeConfig, contextManager);
        ShardingSphereProxyVersion.setVersion(contextManager);
    }
    
    @SuppressWarnings("unchecked")
    private void checkRuleConfiguration(final ProxyConfiguration proxyConfig) {
        for (Entry<String, DatabaseConfiguration> entry : proxyConfig.getDatabaseConfigurations().entrySet()) {
            entry.getValue().getRuleConfigurations().forEach(each -> RuleConfigurationCheckerFactory.findInstance(each).ifPresent(optional -> optional.check(entry.getKey(), each)));
        }
    }
    
    private ContextManager createContextManager(final ProxyConfiguration proxyConfig, final ModeConfiguration modeConfig, final int port) throws SQLException {
        ContextManagerBuilderParameter parameter = new ContextManagerBuilderParameter(modeConfig, proxyConfig.getDatabaseConfigurations(),
                proxyConfig.getGlobalConfiguration().getRules(), proxyConfig.getGlobalConfiguration().getProperties(), proxyConfig.getGlobalConfiguration().getLabels(),
                createInstanceMetaData(proxyConfig, port));
        return ContextManagerBuilderFactory.getInstance(modeConfig).build(parameter);
    }
    
    private InstanceMetaData createInstanceMetaData(final ProxyConfiguration proxyConfig, final int port) {
        String instanceType = proxyConfig.getGlobalConfiguration().getProperties().getProperty(ConfigurationPropertyKey.PROXY_INSTANCE_TYPE.getKey(),
                ConfigurationPropertyKey.PROXY_INSTANCE_TYPE.getDefaultValue());
        return InstanceMetaDataBuilderFactory.create(instanceType, port);
    }
    
    private void contextManagerInitializedCallback(final ModeConfiguration modeConfig, final ContextManager contextManager) {
        for (ContextManagerLifecycleListener each : ContextManagerLifecycleListenerFactory.getAllInstances()) {
            try {
                each.onInitialized(modeConfig, contextManager);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("contextManager onInitialized callback for '{}' failed", each.getClass().getName(), ex);
            }
        }
    }
}
