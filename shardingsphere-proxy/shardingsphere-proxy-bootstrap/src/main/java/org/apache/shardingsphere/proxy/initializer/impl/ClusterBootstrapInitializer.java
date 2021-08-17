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

package org.apache.shardingsphere.proxy.initializer.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.governance.context.ClusterContextManagerBuilder;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.scaling.core.api.ScalingWorker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cluster bootstrap initializer.
 */
public final class ClusterBootstrapInitializer extends AbstractBootstrapInitializer {
    
    private final RegistryCenter registryCenter;
    
    private final boolean isOverwrite;
    
    public ClusterBootstrapInitializer(final ShardingSphereMode mode, final boolean isOverwrite) {
        super(mode);
        Preconditions.checkState(mode.getPersistRepository().isPresent());
        registryCenter = new RegistryCenter((RegistryCenterRepository) mode.getPersistRepository().get());
        this.isOverwrite = isOverwrite;
    }
    
    @Override
    protected ContextManager createContextManager(final ShardingSphereMode mode, final ProxyConfiguration proxyConfig) throws SQLException {
        return new ClusterContextManagerBuilder().build(
                mode, getDataSourcesMap(proxyConfig.getSchemaDataSources()), proxyConfig.getSchemaRules(), proxyConfig.getGlobalRules(), proxyConfig.getProps(), isOverwrite);
    }
    
    // TODO add DataSourceParameter param to ContextManagerBuilder to avoid re-build data source
    private Map<String, Map<String, DataSource>> getDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourceParametersMap.size(), 1);
        for (Entry<String, Map<String, DataSourceParameter>> entry : dataSourceParametersMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceMap(DataSourceParameterConverter.getDataSourceConfigurationMap(entry.getValue())));
        }
        return result;
    }
    
    private Map<String, DataSource> getDataSourceMap(final Map<String, DataSourceConfiguration> dataSourceConfigMap) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceConfigMap.size(), 1);
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().createDataSource());
        }
        return result;
    }
    
    @Override
    protected void initScaling(final YamlProxyConfiguration yamlConfig) {
        Optional<ServerConfiguration> scalingConfig = getScalingConfiguration(yamlConfig);
        if (!scalingConfig.isPresent()) {
            return;
        }
        scalingConfig.ifPresent(optional -> initScalingDetails(yamlConfig.getServerConfiguration().getMode(), optional));
    }
    
    private void initScalingDetails(final YamlModeConfiguration yamlModeConfig, final ServerConfiguration scalingConfig) {
        scalingConfig.setModeConfiguration(new ModeConfigurationYamlSwapper().swapToObject(yamlModeConfig));
        ScalingContext.getInstance().init(scalingConfig);
        ScalingWorker.init();
    }
    
    @Override
    protected void postInit(final YamlProxyConfiguration yamlConfig) {
        registryCenter.onlineInstance(getSchemaNames(yamlConfig));
    }
    
    private Set<String> getSchemaNames(final YamlProxyConfiguration yamlConfig) {
        return Stream.of(getDistMetaDataPersistService().getSchemaMetaDataService().loadAllNames(), 
                yamlConfig.getRuleConfigurations().keySet()).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
