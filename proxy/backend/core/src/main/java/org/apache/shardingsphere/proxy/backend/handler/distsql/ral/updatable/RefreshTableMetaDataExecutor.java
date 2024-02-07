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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Refresh table meta data executor.
 */
@Setter
public final class RefreshTableMetaDataExecutor implements DistSQLUpdateExecutor<RefreshTableMetaDataStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final RefreshTableMetaDataStatement sqlStatement, final ContextManager contextManager) throws SQLException {
        checkStorageUnit(contextManager.getStorageUnits(database.getName()), sqlStatement);
        String schemaName = getSchemaName(sqlStatement);
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
            contextManager.refreshTableMetaData(database);
        }
    }
    
    private void checkStorageUnit(final Map<String, StorageUnit> storageUnits, final RefreshTableMetaDataStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(!storageUnits.isEmpty(), () -> new EmptyStorageUnitException(database.getName()));
        if (sqlStatement.getStorageUnitName().isPresent()) {
            String storageUnitName = sqlStatement.getStorageUnitName().get();
            ShardingSpherePreconditions.checkState(
                    storageUnits.containsKey(storageUnitName), () -> new MissingRequiredStorageUnitsException(database.getName(), Collections.singleton(storageUnitName)));
        }
    }
    
    private String getSchemaName(final RefreshTableMetaDataStatement sqlStatement) {
        return sqlStatement.getSchemaName().isPresent() ? sqlStatement.getSchemaName().get() : new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
    }
    
    @Override
    public Class<RefreshTableMetaDataStatement> getType() {
        return RefreshTableMetaDataStatement.class;
    }
}
