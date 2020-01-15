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

package org.apache.shardingsphere.orchestration.internal.registry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationListenerManager;
import org.apache.shardingsphere.orchestration.internal.registry.state.service.StateService;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Sharding orchestration facade.
 *
 * @author zhangliang
 * @author caohao
 * @author panjuan
 */
@Slf4j
public final class ShardingOrchestrationFacade implements AutoCloseable {
    
    private final RegistryCenter regCenter;
    
    private final boolean isOverwrite;
    
    @Getter
    private final ConfigurationService configService;
    
    private final StateService stateService;
    
    private final ShardingOrchestrationListenerManager listenerManager;
    
    public ShardingOrchestrationFacade(final OrchestrationConfiguration orchestrationConfig, final Collection<String> shardingSchemaNames) {
        regCenter = new RegistryCenterServiceLoader().load(orchestrationConfig.getRegCenterConfig());
        isOverwrite = orchestrationConfig.isOverwrite();
        configService = new ConfigurationService(orchestrationConfig.getName(), regCenter);
        stateService = new StateService(orchestrationConfig.getName(), regCenter);
        listenerManager = shardingSchemaNames.isEmpty() ? new ShardingOrchestrationListenerManager(orchestrationConfig.getName(), regCenter, configService.getAllShardingSchemaNames())
                : new ShardingOrchestrationListenerManager(orchestrationConfig.getName(), regCenter, shardingSchemaNames);
    }
    
    /**
     * Initialize for orchestration.
     *
     * @param dataSourceConfigurationMap schema data source configuration map
     * @param schemaRuleMap schema rule map
     * @param authentication authentication
     * @param props properties
     */
    public void init(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigurationMap,
                     final Map<String, RuleConfiguration> schemaRuleMap, final Authentication authentication, final Properties props) {
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigurationMap.entrySet()) {
            configService.persistConfiguration(entry.getKey(), dataSourceConfigurationMap.get(entry.getKey()), schemaRuleMap.get(entry.getKey()), authentication, props, isOverwrite);
        }
        stateService.persistInstanceOnline();
        stateService.persistDataSourcesNode();
        listenerManager.initListeners();
    }
    
    /**
     * Initialize for orchestration.
     */
    public void init() {
        stateService.persistInstanceOnline();
        stateService.persistDataSourcesNode();
        listenerManager.initListeners();
    }
    
    @Override
    public void close() {
        try {
            regCenter.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("RegCenter exception for: {}", ex.getMessage());
        }
    }
}
