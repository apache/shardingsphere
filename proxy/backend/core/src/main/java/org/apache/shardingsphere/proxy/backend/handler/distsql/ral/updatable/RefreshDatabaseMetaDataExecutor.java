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

import org.apache.shardingsphere.distsql.handler.type.ral.update.UpdatableRALExecutor;
import org.apache.shardingsphere.distsql.statement.ral.updatable.RefreshDatabaseMetaDataStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Refresh database meta data executor.
 */
public final class RefreshDatabaseMetaDataExecutor implements UpdatableRALExecutor<RefreshDatabaseMetaDataStatement> {
    
    @Override
    public void executeUpdate(final RefreshDatabaseMetaDataStatement sqlStatement) throws SQLException {
        Optional<String> toBeRefreshedDatabaseName = sqlStatement.getDatabaseName();
        Map<String, ShardingSphereDatabase> databases = toBeRefreshedDatabaseName.map(optional -> Collections.singletonMap(optional, ProxyContext.getInstance().getDatabase(optional)))
                .orElseGet(() -> ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases());
        for (ShardingSphereDatabase each : databases.values()) {
            if (!SystemSchemaUtils.isSystemSchema(each)) {
                ProxyContext.getInstance().getContextManager().refreshDatabaseMetaData(each, sqlStatement.isForce());
            }
        }
    }
    
    @Override
    public Class<RefreshDatabaseMetaDataStatement> getType() {
        return RefreshDatabaseMetaDataStatement.class;
    }
}
