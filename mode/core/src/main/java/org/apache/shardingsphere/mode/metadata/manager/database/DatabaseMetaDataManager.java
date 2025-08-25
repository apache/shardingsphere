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

package org.apache.shardingsphere.mode.metadata.manager.database;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Database meta data manager.
 */
@RequiredArgsConstructor
public final class DatabaseMetaDataManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
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
        metaDataContexts.update(metaDataContexts.getMetaData(), metaDataPersistFacade);
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
        if (database.getSchema(schemaName).getAllTables().stream().anyMatch(each -> TableRefreshUtils.isSingleTable(each.getName(), database))) {
            database.reloadRules();
        }
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    /**
     * Rename schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param renamedSchemaName renamed schema name
     */
    public synchronized void renameSchema(final String databaseName, final String schemaName, final String renamedSchemaName) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSphereSchema schema = database.getSchema(schemaName);
        ShardingSphereSchema renamedSchema = new ShardingSphereSchema(renamedSchemaName, schema.getAllTables(), schema.getAllViews());
        database.addSchema(renamedSchema);
        database.dropSchema(schemaName);
        database.reloadRules();
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    /**
     * Alter table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeAlteredTable to be altered table
     */
    public synchronized void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable toBeAlteredTable) {
        alterTableOrView(databaseName, schemaName, toBeAlteredTable.getName(), schema -> schema.putTable(toBeAlteredTable));
    }
    
    /**
     * Alter view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeAlteredView to be altered view
     */
    public synchronized void alterView(final String databaseName, final String schemaName, final ShardingSphereView toBeAlteredView) {
        alterTableOrView(databaseName, schemaName, toBeAlteredView.getName(), schema -> schema.putView(toBeAlteredView));
    }
    
    private void alterTableOrView(final String databaseName, final String schemaName, final String tableOrViewName, final Consumer<ShardingSphereSchema> alterAction) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
        if (!metaData.getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        alterAction.accept(database.getSchema(schemaName));
        if (TableRefreshUtils.isSingleTable(tableOrViewName, database)) {
            database.reloadRules();
        }
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    /**
     * Drop table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDroppedTableName to be dropped table name
     */
    public synchronized void dropTable(final String databaseName, final String schemaName, final String toBeDroppedTableName) {
        dropTableOrView(databaseName, schemaName, toBeDroppedTableName, true);
    }
    
    /**
     * Drop view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDroppedViewName to be dropped view name
     */
    public synchronized void dropView(final String databaseName, final String schemaName, final String toBeDroppedViewName) {
        dropTableOrView(databaseName, schemaName, toBeDroppedViewName, false);
    }
    
    private void dropTableOrView(final String databaseName, final String schemaName, final String toBeDroppedTableOrViewName, final boolean isTable) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (!database.containsSchema(schemaName)) {
            return;
        }
        if (isTable) {
            database.getSchema(schemaName).removeTable(toBeDroppedTableOrViewName);
        } else {
            database.getSchema(schemaName).removeView(toBeDroppedTableOrViewName);
        }
        database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.remove(schemaName, toBeDroppedTableOrViewName));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContexts.getMetaData().getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
}
