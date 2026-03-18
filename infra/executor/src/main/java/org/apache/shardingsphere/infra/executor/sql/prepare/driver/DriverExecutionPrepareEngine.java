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

package org.apache.shardingsphere.infra.executor.sql.prepare.driver;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.DriverExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.AbstractExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Driver execution prepare engine.
 * 
 * @param <T> type of driver execution unit
 * @param <C> type of resource connection
 */
@HighFrequencyInvocation
public final class DriverExecutionPrepareEngine<T extends DriverExecutionUnit<?>, C> extends AbstractExecutionPrepareEngine<T> {
    
    @SuppressWarnings("rawtypes")
    private static final Map<JDBCDriverType, SQLExecutionUnitBuilder> TYPE_TO_BUILDER_MAP = new ConcurrentHashMap<>(8, 1F);
    
    @Getter
    private final JDBCDriverType type;
    
    private final DatabaseConnectionManager<C> databaseConnectionManager;
    
    private final ExecutorStatementManager<C, ?, ?> statementManager;
    
    private final StorageResourceOption option;
    
    @SuppressWarnings("rawtypes")
    private final SQLExecutionUnitBuilder sqlExecutionUnitBuilder;
    
    private final ShardingSphereMetaData metaData;
    
    public DriverExecutionPrepareEngine(final JDBCDriverType type, final int maxConnectionsSizePerQuery, final DatabaseConnectionManager<C> databaseConnectionManager,
                                        final ExecutorStatementManager<C, ?, ?> statementManager, final StorageResourceOption option, final Collection<ShardingSphereRule> rules,
                                        final ShardingSphereMetaData metaData) {
        super(maxConnectionsSizePerQuery, rules);
        this.type = type;
        this.databaseConnectionManager = databaseConnectionManager;
        this.statementManager = statementManager;
        this.option = option;
        sqlExecutionUnitBuilder = getCachedSQLExecutionUnitBuilder(type);
        this.metaData = metaData;
    }
    
    /*
     * Refer to <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>.
     */
    @SuppressWarnings("rawtypes")
    private SQLExecutionUnitBuilder getCachedSQLExecutionUnitBuilder(final JDBCDriverType type) {
        SQLExecutionUnitBuilder result;
        if (null == (result = TYPE_TO_BUILDER_MAP.get(type))) {
            result = TYPE_TO_BUILDER_MAP.computeIfAbsent(type, key -> TypedSPILoader.getService(SQLExecutionUnitBuilder.class, key));
        }
        return result;
    }
    
    @Override
    protected List<ExecutionGroup<T>> group(final String databaseName, final String dataSourceName, final int connectionOffset, final List<List<ExecutionUnit>> executionUnitGroups,
                                            final ConnectionMode connectionMode) throws SQLException {
        List<ExecutionGroup<T>> result = new LinkedList<>();
        List<C> connections = databaseConnectionManager.getConnections(databaseName, dataSourceName, connectionOffset, executionUnitGroups.size(), connectionMode);
        int count = 0;
        for (List<ExecutionUnit> each : executionUnitGroups) {
            result.add(createExecutionGroup(databaseName, dataSourceName, each, connections.get(count++), connectionOffset, connectionMode));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroup<T> createExecutionGroup(final String databaseName, final String dataSourceName, final List<ExecutionUnit> executionUnits, final C connection, final int connectionOffset,
                                                   final ConnectionMode connectionMode) throws SQLException {
        List<T> inputs = new LinkedList<>();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSpherePreconditions.checkNotNull(database, () -> new UnknownDatabaseException(databaseName));
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        DatabaseType databaseType = storageUnits.containsKey(dataSourceName) ? storageUnits.get(dataSourceName).getStorageType() : storageUnits.values().iterator().next().getStorageType();
        for (ExecutionUnit each : executionUnits) {
            inputs.add((T) sqlExecutionUnitBuilder.build(each, statementManager, connection, connectionOffset, connectionMode, option, databaseType));
        }
        return new ExecutionGroup<>(inputs);
    }
}
