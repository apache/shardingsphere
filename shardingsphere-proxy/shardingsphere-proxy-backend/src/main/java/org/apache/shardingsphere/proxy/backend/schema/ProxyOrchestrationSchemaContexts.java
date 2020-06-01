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

package org.apache.shardingsphere.proxy.backend.schema;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.orchestration.core.schema.OrchestrationSchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.util.DataSourceConverter;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Proxy control panel subscriber.
 * 
 */
public final class ProxyOrchestrationSchemaContexts extends OrchestrationSchemaContexts {
    
    private final JDBCBackendDataSourceFactory backendDataSourceFactory;
    
    public ProxyOrchestrationSchemaContexts(final SchemaContexts schemaContexts) {
        super(schemaContexts);
        backendDataSourceFactory = JDBCRawBackendDataSourceFactory.getInstance();
    }
    
    @Override
    public Map<String, DataSource> getAddedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) throws Exception {
        Map<String, DataSourceParameter> newDataSourceParameters = DataSourceConverter.getDataSourceParameterMap(newDataSources);
        Map<String, DataSourceParameter> parameters = 
                Maps.filterEntries(newDataSourceParameters, input -> !oldSchemaContext.getSchema().getDataSourceParameters().containsKey(input.getKey()));
        return createDataSources(parameters);
    }
    
    @Override
    public Map<String, DataSource> getModifiedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) throws Exception {
        Map<String, DataSourceParameter> newDataSourceParameters = DataSourceConverter.getDataSourceParameterMap(newDataSources);
        Map<String, DataSourceParameter> parameters = new LinkedHashMap<>();
        for (Entry<String, DataSourceParameter> entry : newDataSourceParameters.entrySet()) {
            if (isModifiedDataSource(oldSchemaContext.getSchema().getDataSourceParameters(), entry)) {
                parameters.put(entry.getKey(), entry.getValue());
            }
        }
        return createDataSources(parameters);
    }
    
    private synchronized boolean isModifiedDataSource(final Map<String, DataSourceParameter> oldDataSourceParameters, final Entry<String, DataSourceParameter> target) {
        return oldDataSourceParameters.containsKey(target.getKey()) && !oldDataSourceParameters.get(target.getKey()).equals(target.getValue());
    }
    
    @Override
    public Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap) throws Exception {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>();
        for (Entry<String, Map<String, DataSourceParameter>> entry : createDataSourceParametersMap(dataSourcesMap).entrySet()) {
            result.put(entry.getKey(), createDataSources(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Map<String, Map<String, DataSourceParameter>> createDataSourceParametersMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>();
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourcesMap.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceParameterMap(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> parameters) throws Exception {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, DataSourceParameter> entry: parameters.entrySet()) {
            result.put(entry.getKey(), backendDataSourceFactory.build(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
