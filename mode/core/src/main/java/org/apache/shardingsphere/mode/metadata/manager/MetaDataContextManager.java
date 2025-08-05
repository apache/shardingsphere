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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.database.DatabaseMetaDataManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.ResourceSwitchManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.StorageUnitManager;
import org.apache.shardingsphere.mode.metadata.manager.rule.DatabaseRuleConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.rule.DatabaseRuleItemManager;
import org.apache.shardingsphere.mode.metadata.manager.rule.GlobalConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.statistics.StatisticsManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

/**
 * Meta data context manager.
 */
@Getter
public class MetaDataContextManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    private final StatisticsManager statisticsManager;
    
    private final DatabaseMetaDataManager databaseMetaDataManager;
    
    private final DatabaseRuleItemManager databaseRuleItemManager;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    private final StorageUnitManager storageUnitManager;
    
    private final DatabaseRuleConfigurationManager databaseRuleConfigurationManager;
    
    private final GlobalConfigurationManager globalConfigurationManager;
    
    public MetaDataContextManager(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext, final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        metaDataPersistFacade = new MetaDataPersistFacade(repository, metaDataContexts.getMetaData().getProps().getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED));
        resourceSwitchManager = new ResourceSwitchManager();
        statisticsManager = new StatisticsManager(metaDataContexts);
        storageUnitManager = new StorageUnitManager(metaDataContexts, computeNodeInstanceContext, resourceSwitchManager, metaDataPersistFacade);
        databaseRuleConfigurationManager = new DatabaseRuleConfigurationManager(metaDataContexts, computeNodeInstanceContext, metaDataPersistFacade);
        databaseMetaDataManager = new DatabaseMetaDataManager(metaDataContexts, metaDataPersistFacade);
        databaseRuleItemManager = new DatabaseRuleItemManager(metaDataContexts, databaseRuleConfigurationManager, metaDataPersistFacade);
        globalConfigurationManager = new GlobalConfigurationManager(metaDataContexts, metaDataPersistFacade);
    }
}
