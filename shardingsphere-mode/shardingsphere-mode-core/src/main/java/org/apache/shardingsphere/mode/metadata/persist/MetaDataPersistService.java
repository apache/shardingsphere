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
import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.mode.metadata.persist.service.ComputeNodePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.PropertiesPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
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
     * @param dataSourcePropsMaps schema and data source properties maps
     * @param schemaRuleConfigs schema and rule configuration map
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistConfigurations(final Map<String, Map<String, DataSourceProperties>> dataSourcePropsMaps, final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs,
                                      final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        globalRuleService.persist(globalRuleConfigs, isOverwrite);
        propsService.persist(props, isOverwrite);
        for (Entry<String, Map<String, DataSourceProperties>> entry : dataSourcePropsMaps.entrySet()) {
            String schemaName = entry.getKey();
            dataSourceService.persist(schemaName, dataSourcePropsMaps.get(schemaName), isOverwrite);
            schemaRuleService.persist(schemaName, schemaRuleConfigs.get(schemaName), isOverwrite);
        }
    }
    
    /**
     * Persist instance configurations.
     * 
     * @param instanceId instance id
     * @param labels collection of label
     * @param isOverwrite whether overwrite registry center's configuration if existed               
     */
    public void persistInstanceConfigurations(final String instanceId, final Collection<String> labels, final boolean isOverwrite) {
        computeNodePersistService.persistInstanceLabels(instanceId, labels, isOverwrite);
    }
}
