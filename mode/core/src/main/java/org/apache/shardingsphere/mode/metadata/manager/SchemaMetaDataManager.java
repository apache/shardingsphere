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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collections;
import java.util.Optional;

/**
 * Resource meta data manager.
 */
public final class SchemaMetaDataManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public SchemaMetaDataManager(final MetaDataContexts metaDataContexts, final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Add database.
     *
     * @param databaseName to be added database name
     */
    public synchronized void addDatabase(final String databaseName) {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.getMetaData().getProps());
        metaDataContexts.getMetaData().addDatabase(databaseName, protocolType, metaDataContexts.getMetaData().getProps());
        metaDataContexts.update(metaDataContexts.getMetaData(), metaDataPersistService);
    }
    
    /**
     * Drop database.
     *
     * @param databaseName to be dropped database name
     */
    public synchronized void dropDatabase(final String databaseName) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.getMetaData().dropDatabase(metaDataContexts.getMetaData().getDatabase(databaseName).getName());
    }
    
    /**
     * Add schema.
     *
     * @param databaseName to be added database name
     * @param schemaName to be added schema name
     */
    public synchronized void addSchema(final String databaseName, final String schemaName) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        if (database.containsSchema(schemaName)) {
            return;
        }
        database.addSchema(new ShardingSphereSchema(schemaName));
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName to be dropped database name
     * @param schemaName to be dropped schema name
     */
    public synchronized void dropSchema(final String databaseName, final String schemaName) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        if (!database.containsSchema(schemaName)) {
            return;
        }
        database.dropSchema(schemaName);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     * @param toBeChangedView to be changed view
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final ShardingSphereTable toBeChangedTable, final ShardingSphereView toBeChangedView) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
        if (!metaData.getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeChangedView).ifPresent(optional -> alterView(databaseName, schemaName, optional));
        if (null != toBeChangedTable || null != toBeChangedView) {
            metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        }
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDeletedTableName to be deleted table name
     * @param toBeDeletedViewName to be deleted view name
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final String toBeDeletedTableName, final String toBeDeletedViewName) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
        if (!metaData.getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeDeletedViewName).ifPresent(optional -> dropView(databaseName, schemaName, optional));
        if (!Strings.isNullOrEmpty(toBeDeletedTableName) || !Strings.isNullOrEmpty(toBeDeletedViewName)) {
            metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        }
    }
    
    private void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (TableRefreshUtils.isSingleTable(beBoChangedTable.getName(), database)) {
            database.reloadRules();
        }
        database.getSchema(schemaName).putTable(beBoChangedTable);
    }
    
    private void alterView(final String databaseName, final String schemaName, final ShardingSphereView beBoChangedView) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (TableRefreshUtils.isSingleTable(beBoChangedView.getName(), database)) {
            database.reloadRules();
        }
        database.getSchema(schemaName).putView(beBoChangedView);
    }
    
    private void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).removeTable(toBeDeletedTableName);
        metaDataContexts.getMetaData().getDatabase(databaseName)
                .getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.remove(schemaName, toBeDeletedTableName));
    }
    
    private void dropView(final String databaseName, final String schemaName, final String toBeDeletedViewName) {
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).removeView(toBeDeletedViewName);
        metaDataContexts.getMetaData().getDatabase(databaseName)
                .getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.remove(schemaName, toBeDeletedViewName));
    }
}
