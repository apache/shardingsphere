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

package org.apache.shardingsphere.driver.orchestration.internal.schema;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.driver.orchestration.internal.util.DataSourceConverter;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.schema.OrchestrationSchemaContexts;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * JDBC orchestration schemaContexts.
 */
public final class JDBCOrchestrationSchemaContexts extends OrchestrationSchemaContexts {
    
    public JDBCOrchestrationSchemaContexts(final SchemaContexts schemaContexts, final OrchestrationFacade orchestrationFacade) {
        super(schemaContexts, orchestrationFacade);
    }
    
    @Override
    protected Map<String, DataSource> getAddedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) {
        Map<String, DataSourceConfiguration> newDataSourceConfigs = Maps.filterKeys(newDataSources, each -> !oldSchemaContext.getSchema().getDataSources().containsKey(each));
        return DataSourceConverter.getDataSourceMap(newDataSourceConfigs);
    }
    
    @Override
    protected Map<String, DataSource> getModifiedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) {
        Map<String, DataSourceConfiguration> modifiedDataSourceConfigs =
                newDataSources.entrySet().stream().filter(this::isModifiedDataSource).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key, repeatKey) -> key, LinkedHashMap::new));
        return DataSourceConverter.getDataSourceMap(modifiedDataSourceConfigs);
    }
    
    private synchronized boolean isModifiedDataSource(final Entry<String, DataSourceConfiguration> dataSourceConfigs) {
        Map<String, DataSourceConfiguration> oldDataSourceConfigs = DataSourceConverter.getDataSourceConfigurationMap(getSchemaContexts().get(DefaultSchema.LOGIC_NAME).getSchema().getDataSources());
        return oldDataSourceConfigs.containsKey(dataSourceConfigs.getKey()) && !oldDataSourceConfigs.get(dataSourceConfigs.getKey()).equals(dataSourceConfigs.getValue());
    }
    
    @Override
    protected Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap) {
        Map<String, Map<String, DataSource>> result = new LinkedMap<>(dataSourcesMap.size());
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourcesMap.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceMap(entry.getValue()));
        }
        return result;
    }
}
