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

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.manager.impl.MemoryContextManagerBuilder;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Memory bootstrap initializer.
 */
public final class MemoryBootstrapInitializer extends AbstractBootstrapInitializer {
    
    public MemoryBootstrapInitializer(final ShardingSphereMode mode) {
        super(mode);
    }
    
    @Override
    protected ContextManager createContextManager(final ShardingSphereMode mode, final ProxyConfiguration proxyConfig) throws SQLException {
        return new MemoryContextManagerBuilder().build(
                mode, getDataSourcesMap(proxyConfig.getSchemaDataSources()), proxyConfig.getSchemaRules(), proxyConfig.getGlobalRules(), proxyConfig.getProps(), true);
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
        getScalingConfiguration(yamlConfig).ifPresent(optional -> ScalingContext.getInstance().init(optional));
    }
}
