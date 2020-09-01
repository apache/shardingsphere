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

package org.apache.shardingsphere.proxy.governance.schema;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.schema.GovernanceSchemaContexts;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Proxy governance schema contexts.
 */
public final class ProxyGovernanceSchemaContexts extends GovernanceSchemaContexts {
    
    private final JDBCBackendDataSourceFactory backendDataSourceFactory;
    
    public ProxyGovernanceSchemaContexts(final SchemaContexts schemaContexts, final GovernanceFacade governanceFacade) {
        super(schemaContexts, governanceFacade);
        backendDataSourceFactory = JDBCRawBackendDataSourceFactory.getInstance();
    }
    
    @Override
    protected Map<String, DataSource> getAddedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) {
        Map<String, DataSourceConfiguration> newDataSourceConfigs = Maps.filterKeys(newDataSources, each -> !oldSchemaContext.getSchema().getDataSources().containsKey(each));
        return createDataSources(DataSourceConverter.getDataSourceParameterMap(newDataSourceConfigs));
    }
    
    @Override
    protected Map<String, DataSource> getModifiedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Map<String, DataSourceParameter> newDataSourceParameters = DataSourceConverter.getDataSourceParameterMap(newDataSourceConfigs);
        Map<String, DataSourceParameter> parameters = new LinkedHashMap<>(newDataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : newDataSourceParameters.entrySet()) {
            if (isModifiedDataSource(oldSchemaContext.getSchema().getDataSources(), entry.getKey(), entry.getValue())) {
                parameters.put(entry.getKey(), entry.getValue());
            }
        }
        return createDataSources(parameters);
    }
    
    private synchronized boolean isModifiedDataSource(final Map<String, DataSource> oldDataSources, final String newDataSourceName, final DataSourceParameter newDataSourceParameter) {
        return oldDataSources.containsKey(newDataSourceName) && !DataSourceConverter.getDataSourceParameter(oldDataSources.get(newDataSourceName)).equals(newDataSourceParameter);
    }
    
    @Override
    protected Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesConfigs) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourcesConfigs.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourcesConfigs.entrySet()) {
            result.put(entry.getKey(), org.apache.shardingsphere.infra.config.datasource.DataSourceConverter.getDataSourceMap(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry: dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), backendDataSourceFactory.build(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
