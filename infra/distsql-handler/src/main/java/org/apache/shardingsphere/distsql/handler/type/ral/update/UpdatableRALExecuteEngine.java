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

package org.apache.shardingsphere.distsql.handler.type.ral.update;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.ral.updatable.UpdatableRALStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;

/**
 * Updatable RAL execute engine.
 */
@RequiredArgsConstructor
public abstract class UpdatableRALExecuteEngine {
    
    private final UpdatableRALStatement sqlStatement;
    
    private final String currentDatabaseName;
    
    private final ContextManager contextManager;
    
    /**
     * Execute update.
     * 
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void executeUpdate() throws SQLException {
        UpdatableRALExecutor executor = TypedSPILoader.getService(UpdatableRALExecutor.class, sqlStatement.getClass());
        if (executor instanceof DistSQLExecutorDatabaseAware) {
            ((DistSQLExecutorDatabaseAware) executor).setDatabase(getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName)));
        }
        if (null != executor.getClass().getAnnotation(DistSQLExecutorClusterModeRequired.class)) {
            ShardingSpherePreconditions.checkState(contextManager.getInstanceContext().isCluster(), () -> new UnsupportedSQLOperationException("Mode must be `Cluster`."));
        }
        executor.checkBeforeUpdate(sqlStatement, contextManager);
        executor.executeUpdate(sqlStatement, contextManager);
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
}
