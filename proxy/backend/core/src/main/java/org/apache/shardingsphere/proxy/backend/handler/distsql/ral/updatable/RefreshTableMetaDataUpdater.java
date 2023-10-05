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

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.updater.ConnectionSessionRequiredRALUpdater;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Collections;
import java.util.Map;

/**
 * Refresh table meta data handler.
 */
public final class RefreshTableMetaDataUpdater implements ConnectionSessionRequiredRALUpdater<RefreshTableMetaDataStatement> {
    
    @Override
    public void executeUpdate(final ConnectionSession connectionSession, final RefreshTableMetaDataStatement sqlStatement) {
        String databaseName = getDatabaseName(connectionSession);
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        checkStorageUnit(databaseName, contextManager.getStorageUnits(databaseName), sqlStatement);
        String schemaName = getSchemaName(databaseName, sqlStatement, connectionSession);
        if (sqlStatement.getStorageUnitName().isPresent()) {
            if (sqlStatement.getTableName().isPresent()) {
                contextManager.reloadTable(databaseName, schemaName, sqlStatement.getStorageUnitName().get(), sqlStatement.getTableName().get());
            } else {
                contextManager.reloadSchema(databaseName, schemaName, sqlStatement.getStorageUnitName().get());
            }
            return;
        }
        if (sqlStatement.getTableName().isPresent()) {
            contextManager.reloadTable(databaseName, schemaName, sqlStatement.getTableName().get());
        } else {
            contextManager.reloadDatabaseMetaData(databaseName);
        }
    }
    
    private void checkStorageUnit(final String databaseName, final Map<String, StorageUnit> storageUnits, final RefreshTableMetaDataStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(!storageUnits.isEmpty(), () -> new EmptyStorageUnitException(databaseName));
        if (sqlStatement.getStorageUnitName().isPresent()) {
            String storageUnitName = sqlStatement.getStorageUnitName().get();
            ShardingSpherePreconditions.checkState(
                    storageUnits.containsKey(storageUnitName), () -> new MissingRequiredStorageUnitsException(databaseName, Collections.singleton(storageUnitName)));
        }
    }
    
    private String getDatabaseName(final ConnectionSession connectionSession) {
        String result = connectionSession.getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(result)) {
            throw new UnknownDatabaseException(result);
        }
        return result;
    }
    
    private String getSchemaName(final String databaseName, final RefreshTableMetaDataStatement sqlStatement, final ConnectionSession connectionSession) {
        return sqlStatement.getSchemaName().isPresent() ? sqlStatement.getSchemaName().get() : new DatabaseTypeRegistry(connectionSession.getProtocolType()).getDefaultSchemaName(databaseName);
    }
    
    @Override
    public Class<RefreshTableMetaDataStatement> getType() {
        return RefreshTableMetaDataStatement.class;
    }
}
