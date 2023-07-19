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

package org.apache.shardingsphere.mode.manager.context;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.identifier.type.MetaDataHeldRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Resource meta data context manager.
 */
@RequiredArgsConstructor
public final class ResourceMetaDataContextManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    /**
     * Add database.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabase(final String databaseName) {
        if (metaDataContexts.get().getMetaData().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.get().getMetaData().getProps());
        metaDataContexts.get().getMetaData().addDatabase(databaseName, protocolType, metaDataContexts.get().getMetaData().getProps());
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        alterMetaDataHeldRule(database);
    }
    
    private void alterMetaDataHeldRule(final ShardingSphereDatabase database) {
        metaDataContexts.get().getMetaData().getGlobalRuleMetaData().findRules(MetaDataHeldRule.class).forEach(each -> each.alterDatabase(database));
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabase(final String databaseName) {
        if (!metaDataContexts.get().getMetaData().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.get().getMetaData().dropDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName).getName());
        metaDataContexts.get().getMetaData().getGlobalRuleMetaData().findRules(MetaDataHeldRule.class).forEach(each -> each.dropDatabase(databaseName));
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addSchema(final String databaseName, final String schemaName) {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        if (database.containsSchema(schemaName)) {
            return;
        }
        database.putSchema(schemaName, new ShardingSphereSchema());
        alterMetaDataHeldRule(database);
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropSchema(final String databaseName, final String schemaName) {
        if (!metaDataContexts.get().getMetaData().containsDatabase(databaseName)) {
            return;
        }
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        if (!database.containsSchema(schemaName)) {
            return;
        }
        database.removeSchema(schemaName);
        alterMetaDataHeldRule(database);
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
        if (!metaDataContexts.get().getMetaData().containsDatabase(databaseName) || !metaDataContexts.get().getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeDeletedViewName).ifPresent(optional -> dropView(databaseName, schemaName, optional));
        alterMetaDataHeldRule(metaDataContexts.get().getMetaData().getDatabase(databaseName));
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
        if (!metaDataContexts.get().getMetaData().containsDatabase(databaseName) || !metaDataContexts.get().getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeChangedView).ifPresent(optional -> alterView(databaseName, schemaName, optional));
        alterMetaDataHeldRule(metaDataContexts.get().getMetaData().getDatabase(databaseName));
    }
    
    private void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchema(schemaName).removeTable(toBeDeletedTableName);
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(MutableDataNodeRule.class::isInstance).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedTableName));
    }
    
    private void dropView(final String databaseName, final String schemaName, final String toBeDeletedViewName) {
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchema(schemaName).removeView(toBeDeletedViewName);
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(MutableDataNodeRule.class::isInstance).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedViewName));
    }
    
    private void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        if (isSingleTable(database, beBoChangedTable.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putTable(beBoChangedTable.getName(), beBoChangedTable);
    }
    
    private void alterView(final String databaseName, final String schemaName, final ShardingSphereView beBoChangedView) {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        if (isSingleTable(database, beBoChangedView.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putView(beBoChangedView.getName(), beBoChangedView);
    }
    
    private boolean isSingleTable(final ShardingSphereDatabase database, final String tableName) {
        return database.getRuleMetaData().findRules(TableContainedRule.class).stream().noneMatch(each -> each.getDistributedTableMapper().contains(tableName));
    }
}
