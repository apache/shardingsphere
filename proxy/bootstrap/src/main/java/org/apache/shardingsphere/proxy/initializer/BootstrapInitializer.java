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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.ProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyGlobalConfiguration;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.version.ShardingSphereProxyVersion;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Bootstrap initializer.
 */
public final class BootstrapInitializer {
    
    /**
     * Initialize.
     *
     * @param loadResult proxy configuration load result
     * @param port proxy port
     * @throws SQLException SQL exception
     */
    public void init(final ProxyConfigurationLoadResult loadResult, final int port) throws SQLException {
        ProxyConfiguration proxyConfig = createProxyConfiguration(loadResult);
        ContextManager contextManager = createContextManager(proxyConfig, loadResult.getModeConfiguration(), port);
        ProxyContext.init(contextManager);
        BackendExecutorContext.getInstance().init();
        ShardingSphereProxyVersion.setVersion(contextManager);
    }
    
    private ProxyConfiguration createProxyConfiguration(final ProxyConfigurationLoadResult loadResult) {
        boolean isInstanceConnectionEnabled = (boolean) loadResult.getProperties().getOrDefault(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED.getKey(), Boolean.FALSE);
        Map<String, DatabaseConfiguration> databaseConfigs = new LinkedHashMap<>(loadResult.getDatabaseConfigurations().size(), 1F);
        for (Entry<String, ProxyDatabaseConfiguration> entry : loadResult.getDatabaseConfigurations().entrySet()) {
            databaseConfigs.put(entry.getKey(), new DataSourceGeneratedDatabaseConfiguration(entry.getValue().getDataSources(), entry.getValue().getRules(), isInstanceConnectionEnabled));
        }
        Map<String, DataSourcePoolProperties> globalDataSourceProps = loadResult.getDataSources().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        ProxyGlobalConfiguration globalConfig = new ProxyGlobalConfiguration(DataSourcePoolCreator.create(globalDataSourceProps, true), loadResult.getRules(), loadResult.getProperties());
        return new ProxyConfiguration(databaseConfigs, globalConfig);
    }
    
    private ContextManager createContextManager(final ProxyConfiguration proxyConfig, final ModeConfiguration modeConfig, final int port) throws SQLException {
        InstanceMetaData instanceMetaData = TypedSPILoader.getService(InstanceMetaDataBuilder.class, "Proxy").build(port, "");
        ContextManagerBuilderParameter param = new ContextManagerBuilderParameter(modeConfig, proxyConfig.getDatabaseConfigurations(), proxyConfig.getGlobalConfiguration().getDataSources(),
                proxyConfig.getGlobalConfiguration().getRules(), proxyConfig.getGlobalConfiguration().getProperties(), instanceMetaData);
        return TypedSPILoader.getService(ContextManagerBuilder.class, null == modeConfig ? null : modeConfig.getType()).build(param, new EventBusContext());
    }
}
