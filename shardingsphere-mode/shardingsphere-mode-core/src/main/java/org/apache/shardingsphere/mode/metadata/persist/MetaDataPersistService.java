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

package org.apache.shardingsphere.mode.metadata.persist;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyerFactory;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.mode.metadata.persist.service.ComputeNodePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.PropertiesPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Meta data persist service.
 */
@Getter
public final class MetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final DataSourcePersistService dataSourceService;
    
    private final SchemaMetaDataPersistService schemaMetaDataService;
    
    private final SchemaRulePersistService schemaRuleService;
    
    private final GlobalRulePersistService globalRuleService;
    
    private final PropertiesPersistService propsService;
    
    private final ComputeNodePersistService computeNodePersistService;
    
    public MetaDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        dataSourceService = new DataSourcePersistService(repository);
        schemaMetaDataService = new SchemaMetaDataPersistService(repository);
        schemaRuleService = new SchemaRulePersistService(repository);
        globalRuleService = new GlobalRulePersistService(repository);
        propsService = new PropertiesPersistService(repository);
        computeNodePersistService = new ComputeNodePersistService(repository);
    }
    
    /**
     * Persist configurations.
     *
     * @param schemaConfigs schema configurations
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistConfigurations(final Map<String, ? extends SchemaConfiguration> schemaConfigs,
                                      final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        globalRuleService.persist(globalRuleConfigs, isOverwrite);
        propsService.persist(props, isOverwrite);
        for (Entry<String, ? extends SchemaConfiguration> entry : schemaConfigs.entrySet()) {
            String schemaName = entry.getKey();
            dataSourceService.persist(schemaName, getDataSourcePropertiesMap(entry.getValue().getDataSources()), isOverwrite);
            schemaRuleService.persist(schemaName, entry.getValue().getRuleConfigurations(), isOverwrite);
        }
    }
    
    private Map<String, DataSourceProperties> getDataSourcePropertiesMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> each : dataSourceMap.entrySet()) {
            result.put(each.getKey(), DataSourcePropertiesCreator.create(each.getValue()));
        }
        return result;
    }
    
    /**
     * Persist instance labels.
     * 
     * @param instanceId instance id
     * @param labels labels
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistInstanceLabels(final String instanceId, final Collection<String> labels, final boolean isOverwrite) {
        computeNodePersistService.persistInstanceLabels(instanceId, labels, isOverwrite);
    }
    
    /**
     * Get effective data sources.
     * 
     * @param schemaName schema name
     * @param schemaConfigs schema configurations
     * @return effective data sources
     * @throws SQLException SQL exception
     */
    public Map<String, DataSource> getEffectiveDataSources(final String schemaName, final Map<String, ? extends SchemaConfiguration> schemaConfigs) throws SQLException {
        Map<String, DataSourceProperties> persistedDataPropsMap = dataSourceService.load(schemaName);
        return schemaConfigs.containsKey(schemaName)
                ? mergeEffectiveDataSources(persistedDataPropsMap, schemaConfigs.get(schemaName).getDataSources()) : DataSourcePoolCreator.create(persistedDataPropsMap);
    }
    
    private Map<String, DataSource> mergeEffectiveDataSources(
            final Map<String, DataSourceProperties> persistedDataSourcePropsMap, final Map<String, DataSource> localConfiguredDataSources) throws SQLException {
        Map<String, DataSource> result = new LinkedHashMap<>(persistedDataSourcePropsMap.size(), 1);
        for (Entry<String, DataSourceProperties> entry : persistedDataSourcePropsMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSourceProperties persistedDataSourceProps = entry.getValue();
            DataSource localConfiguredDataSource = localConfiguredDataSources.get(dataSourceName);
            if (null == localConfiguredDataSource) {
                result.put(dataSourceName, DataSourcePoolCreator.create(persistedDataSourceProps));
            } else if (DataSourcePropertiesCreator.create(localConfiguredDataSource).equals(persistedDataSourceProps)) {
                result.put(dataSourceName, localConfiguredDataSource);
            } else {
                DataSourcePoolDestroyerFactory.destroy(localConfiguredDataSource);
            }
        }
        return result;
    }
}
