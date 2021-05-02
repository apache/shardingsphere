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

package org.apache.shardingsphere.governance.core.facade;

import lombok.Getter;
import org.apache.shardingsphere.governance.core.facade.repository.GovernanceRepositoryFacade;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.listener.GovernanceListenerManager;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Governance facade.
 */
public final class GovernanceFacade implements AutoCloseable {
    
    private boolean isOverwrite;
    
    private GovernanceRepositoryFacade repositoryFacade;
    
    @Getter
    private RegistryCenter registryCenter;
    
    private GovernanceListenerManager listenerManager;
    
    /**
     * Initialize governance facade.
     *
     * @param config governance configuration
     * @param schemaNames schema names
     */
    public void init(final GovernanceConfiguration config, final Collection<String> schemaNames) {
        isOverwrite = config.isOverwrite();
        repositoryFacade = new GovernanceRepositoryFacade(config);
        registryCenter = new RegistryCenter(repositoryFacade.getRegistryRepository());
        listenerManager = new GovernanceListenerManager(repositoryFacade.getRegistryRepository(), schemaNames.isEmpty()
                ? registryCenter.getAllSchemaNames() : Stream.of(registryCenter.getAllSchemaNames(), schemaNames).flatMap(Collection::stream).distinct().collect(Collectors.toList()));
    }
    
    /**
     * Online instance.
     *
     * @param dataSourceConfigMap schema data source configuration map
     * @param schemaRuleMap schema rule map
     * @param users users
     * @param props properties
     */
    public void onlineInstance(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigMap,
                               final Map<String, Collection<RuleConfiguration>> schemaRuleMap, final Collection<ShardingSphereUser> users, final Properties props) {
        registryCenter.persistGlobalConfiguration(users, props, isOverwrite);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigMap.entrySet()) {
            registryCenter.persistConfigurations(entry.getKey(), dataSourceConfigMap.get(entry.getKey()), schemaRuleMap.get(entry.getKey()), isOverwrite);
        }
        onlineInstance();
    }
    
    /**
     * Online instance.
     */
    public void onlineInstance() {
        registryCenter.persistInstanceOnline();
        registryCenter.persistDataNodes();
        registryCenter.persistPrimaryNodes();
        listenerManager.initListeners();
    }
    
    @Override
    public void close() {
        repositoryFacade.close();
    }
}
