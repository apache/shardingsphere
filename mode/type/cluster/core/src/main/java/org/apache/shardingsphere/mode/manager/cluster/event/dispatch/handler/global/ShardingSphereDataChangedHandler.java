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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.handler.global;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.handler.DataChangedEventHandler;
import org.apache.shardingsphere.mode.metadata.manager.ShardingSphereDatabaseDataManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * ShardingSphere data changed handler.
 */
public final class ShardingSphereDataChangedHandler implements DataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return ShardingSphereDataNode.getShardingSphereDataNodePath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        ShardingSphereDatabaseDataManager databaseManager = contextManager.getMetaDataContextManager().getDatabaseManager();
        Optional<String> databaseName = ShardingSphereDataNode.getDatabaseName(event.getKey());
        if (databaseName.isPresent()) {
            handleDatabaseChanged(databaseManager, event.getType(), databaseName.get());
            return;
        }
        databaseName = ShardingSphereDataNode.getDatabaseNameByDatabasePath(event.getKey());
        if (!databaseName.isPresent()) {
            return;
        }
        Optional<String> schemaName = ShardingSphereDataNode.getSchemaName(event.getKey());
        if (schemaName.isPresent()) {
            handleSchemaChanged(databaseManager, event.getType(), databaseName.get(), schemaName.get());
            return;
        }
        schemaName = ShardingSphereDataNode.getSchemaNameBySchemaPath(event.getKey());
        if (!schemaName.isPresent()) {
            return;
        }
        Optional<String> tableName = ShardingSphereDataNode.getTableName(event.getKey());
        if (tableName.isPresent()) {
            handleTableChanged(databaseManager, event.getType(), databaseName.get(), schemaName.get(), tableName.get());
            return;
        }
        tableName = ShardingSphereDataNode.getTableNameByRowPath(event.getKey());
        if (!tableName.isPresent()) {
            return;
        }
        Optional<String> rowPath = ShardingSphereDataNode.getRowUniqueKey(event.getKey());
        if (rowPath.isPresent()) {
            handleRowDataChanged(databaseManager, event.getType(), event.getValue(), databaseName.get(), schemaName.get(), tableName.get(), rowPath.get());
        }
    }
    
    private void handleDatabaseChanged(final ShardingSphereDatabaseDataManager databaseManager, final Type type, final String databaseName) {
        switch (type) {
            case ADDED:
            case UPDATED:
                databaseManager.addShardingSphereDatabaseData(databaseName);
                return;
            case DELETED:
                databaseManager.dropShardingSphereDatabaseData(databaseName);
                return;
            default:
        }
    }
    
    private void handleSchemaChanged(final ShardingSphereDatabaseDataManager databaseManager, final Type type, final String databaseName, final String schemaName) {
        switch (type) {
            case ADDED:
            case UPDATED:
                databaseManager.addShardingSphereSchemaData(databaseName, schemaName);
                return;
            case DELETED:
                databaseManager.dropShardingSphereSchemaData(databaseName, schemaName);
                return;
            default:
        }
    }
    
    private void handleTableChanged(final ShardingSphereDatabaseDataManager databaseManager, final Type type, final String databaseName, final String schemaName, final String tableName) {
        switch (type) {
            case ADDED:
            case UPDATED:
                databaseManager.addShardingSphereTableData(databaseName, schemaName, tableName);
                return;
            case DELETED:
                databaseManager.dropShardingSphereTableData(databaseName, schemaName, tableName);
                return;
            default:
        }
    }
    
    private void handleRowDataChanged(final ShardingSphereDatabaseDataManager databaseManager, final Type type, final String eventValue,
                                      final String databaseName, final String schemaName, final String tableName, final String rowPath) {
        if ((Type.ADDED == type || Type.UPDATED == type) && !Strings.isNullOrEmpty(eventValue)) {
            databaseManager.alterShardingSphereRowData(databaseName, schemaName, tableName, YamlEngine.unmarshal(eventValue, YamlShardingSphereRowData.class));
        } else if (Type.DELETED == type) {
            databaseManager.deleteShardingSphereRowData(databaseName, schemaName, tableName, rowPath);
        }
    }
}
