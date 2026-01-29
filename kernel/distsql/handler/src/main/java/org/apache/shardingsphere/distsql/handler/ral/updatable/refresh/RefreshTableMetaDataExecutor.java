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

package org.apache.shardingsphere.distsql.handler.ral.updatable.refresh;

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collections;
import java.util.Map;

/**
 * Refresh table meta data executor.
 */
@Setter
public final class RefreshTableMetaDataExecutor implements DistSQLUpdateExecutor<RefreshTableMetaDataStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final RefreshTableMetaDataStatement sqlStatement, final ContextManager contextManager) {
        String schemaName = getSchemaName(sqlStatement);
        checkBeforeUpdate(sqlStatement, schemaName);
        if (sqlStatement.getStorageUnitName().isPresent()) {
            if (sqlStatement.getTableName().isPresent()) {
                contextManager.reloadTable(database, schemaName, sqlStatement.getStorageUnitName().get(), sqlStatement.getTableName().get());
            } else {
                contextManager.reloadSchema(database, schemaName, sqlStatement.getStorageUnitName().get());
            }
            return;
        }
        if (sqlStatement.getTableName().isPresent()) {
            contextManager.reloadTable(database, schemaName, sqlStatement.getTableName().get());
        } else {
            contextManager.reloadDatabase(database);
        }
    }
    
    private String getSchemaName(final RefreshTableMetaDataStatement sqlStatement) {
        return sqlStatement.getSchemaName().isPresent() ? sqlStatement.getSchemaName().get() : new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
    }
    
    private void checkBeforeUpdate(final RefreshTableMetaDataStatement sqlStatement, final String schemaName) {
        checkStorageUnit(database.getResourceMetaData().getStorageUnits(), sqlStatement);
        checkSchema(schemaName);
        checkTable(sqlStatement, schemaName);
    }
    
    private void checkStorageUnit(final Map<String, StorageUnit> storageUnits, final RefreshTableMetaDataStatement sqlStatement) {
        ShardingSpherePreconditions.checkNotEmpty(storageUnits, () -> new EmptyStorageUnitException(database.getName()));
        if (sqlStatement.getStorageUnitName().isPresent()) {
            String storageUnitName = sqlStatement.getStorageUnitName().get();
            ShardingSpherePreconditions.checkContainsKey(storageUnits, storageUnitName, () -> new MissingRequiredStorageUnitsException(database.getName(), Collections.singleton(storageUnitName)));
        }
    }
    
    private void checkSchema(final String schemaName) {
        ShardingSpherePreconditions.checkState(database.containsSchema(schemaName), () -> new SchemaNotFoundException(schemaName));
    }
    
    private void checkTable(final RefreshTableMetaDataStatement sqlStatement, final String schemaName) {
        if (sqlStatement.getTableName().isPresent()) {
            String tableName = sqlStatement.getTableName().get();
            ShardingSpherePreconditions.checkState(database.getSchema(schemaName).containsTable(tableName), () -> new TableNotFoundException(tableName));
        }
    }
    
    @Override
    public Class<RefreshTableMetaDataStatement> getType() {
        return RefreshTableMetaDataStatement.class;
    }
}
