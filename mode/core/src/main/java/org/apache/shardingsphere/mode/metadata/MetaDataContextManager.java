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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.manager.DatabaseRuleConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.GlobalConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.ResourceSwitchManager;
import org.apache.shardingsphere.mode.metadata.manager.RuleItemManager;
import org.apache.shardingsphere.mode.metadata.manager.SchemaMetaDataManager;
import org.apache.shardingsphere.mode.metadata.manager.ShardingSphereDatabaseDataManager;
import org.apache.shardingsphere.mode.metadata.manager.StorageUnitManager;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Meta data context manager..
 */
@Getter
public class MetaDataContextManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ShardingSphereDatabaseDataManager databaseManager;
    
    private final SchemaMetaDataManager schemaMetaDataManager;
    
    private final RuleItemManager ruleItemManager;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final StorageUnitManager storageUnitManager;
    
    private final DatabaseRuleConfigurationManager databaseRuleConfigurationManager;
    
    private final GlobalConfigurationManager globalConfigurationManager;
    
    public MetaDataContextManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                  final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        resourceSwitchManager = new ResourceSwitchManager();
        databaseManager = new ShardingSphereDatabaseDataManager(metaDataContexts);
        storageUnitManager = new StorageUnitManager(metaDataContexts, computeNodeInstanceContext, repository, resourceSwitchManager);
        databaseRuleConfigurationManager = new DatabaseRuleConfigurationManager(metaDataContexts, computeNodeInstanceContext, repository);
        schemaMetaDataManager = new SchemaMetaDataManager(metaDataContexts, repository);
        ruleItemManager = new RuleItemManager(metaDataContexts, repository, databaseRuleConfigurationManager);
        globalConfigurationManager = new GlobalConfigurationManager(metaDataContexts, repository);
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Delete schema names.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void deletedSchemaNames(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        GenericSchemaManager.getToBeDeletedSchemaNames(reloadDatabase.getSchemas(), currentDatabase.getSchemas()).keySet()
                .forEach(each -> metaDataPersistService.getDatabaseMetaDataService().dropSchema(databaseName, each));
    }
    
    /**
     * Renew meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     */
    public void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts.set(metaDataContexts);
    }
}
