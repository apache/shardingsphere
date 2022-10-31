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
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.distsql.exception.resource.EmptyResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * Refresh table metadata handler.
 */
public final class RefreshTableMetadataHandler extends UpdatableRALBackendHandler<RefreshTableMetadataStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        String databaseName = getDatabaseName();
        checkDataSources(databaseName, contextManager.getDataSourceMap(databaseName));
        String schemaName = getSchemaName(databaseName);
        if (getSqlStatement().getStorageUnitName().isPresent()) {
            if (getSqlStatement().getTableName().isPresent()) {
                contextManager.reloadTable(databaseName, schemaName, getSqlStatement().getStorageUnitName().get(), getSqlStatement().getTableName().get());
            } else {
                contextManager.reloadSchema(databaseName, schemaName, getSqlStatement().getStorageUnitName().get());
            }
            return;
        }
        if (getSqlStatement().getTableName().isPresent()) {
            contextManager.reloadTable(databaseName, schemaName, getSqlStatement().getTableName().get());
        } else {
            contextManager.reloadDatabase(databaseName);
        }
    }
    
    private void checkDataSources(final String databaseName, final Map<String, DataSource> dataSources) {
        ShardingSpherePreconditions.checkState(!dataSources.isEmpty(), () -> new EmptyResourceException(databaseName));
        if (getSqlStatement().getStorageUnitName().isPresent()) {
            String storageUnitName = getSqlStatement().getStorageUnitName().get();
            ShardingSpherePreconditions.checkState(dataSources.containsKey(storageUnitName), () -> new MissingRequiredResourcesException(databaseName, Collections.singletonList(storageUnitName)));
        }
    }
    
    private String getDatabaseName() {
        String result = getConnectionSession().getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(result)) {
            throw new UnknownDatabaseException(result);
        }
        return result;
    }
    
    private String getSchemaName(final String databaseName) {
        return getSqlStatement().getSchemaName().isPresent()
                ? getSqlStatement().getSchemaName().get()
                : DatabaseTypeEngine.getDefaultSchemaName(getConnectionSession().getProtocolType(), databaseName);
    }
}
