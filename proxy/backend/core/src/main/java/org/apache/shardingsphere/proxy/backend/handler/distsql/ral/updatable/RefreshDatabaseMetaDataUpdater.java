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
import org.apache.shardingsphere.distsql.statement.ral.updatable.RefreshDatabaseMetaDataStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.updater.ConnectionSessionRequiredRALUpdater;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Refresh database meta data updater.
 */
public final class RefreshDatabaseMetaDataUpdater implements ConnectionSessionRequiredRALUpdater<RefreshDatabaseMetaDataStatement> {
    
    @Override
    public void executeUpdate(final ConnectionSession connectionSession, final RefreshDatabaseMetaDataStatement sqlStatement) throws SQLException {
        Optional<String> toBeRefreshedDatabaseName = sqlStatement.getDatabaseName();
        if (toBeRefreshedDatabaseName.isPresent()) {
            String databaseName = toBeRefreshedDatabaseName.get();
            ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(databaseName), NoDatabaseSelectedException::new);
            ShardingSpherePreconditions.checkState(ProxyContext.getInstance().databaseExists(databaseName), () -> new UnknownDatabaseException(databaseName));
            reloadDatabaseMetaData(databaseName, sqlStatement.isForce());
            return;
        }
        Map<String, ShardingSphereDatabase> databases = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases();
        databases.values().forEach(each -> checkAndRefresh(each, sqlStatement.isForce()));
    }
    
    private void checkAndRefresh(final ShardingSphereDatabase each, final boolean force) {
        if (!SystemSchemaUtils.isSystemSchema(each)) {
            reloadDatabaseMetaData(each.getName(), force);
        }
    }
    
    private void reloadDatabaseMetaData(final String databaseName, final boolean force) {
        ProxyContext.getInstance().getContextManager().refreshDatabaseMetaData(databaseName, force);
    }
    
    @Override
    public Class<RefreshDatabaseMetaDataStatement> getType() {
        return RefreshDatabaseMetaDataStatement.class;
    }
}
