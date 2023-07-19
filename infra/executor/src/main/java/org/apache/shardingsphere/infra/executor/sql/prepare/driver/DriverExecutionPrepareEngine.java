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

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.DriverExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.AbstractExecutionPrepareEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

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
public final class DriverExecutionPrepareEngine<T extends DriverExecutionUnit<?>, C> extends AbstractExecutionPrepareEngine<T> {
    
    @SuppressWarnings("rawtypes")
    private static final Map<String, SQLExecutionUnitBuilder> TYPE_TO_BUILDER_MAP = new ConcurrentHashMap<>(8, 1F);
    
    private final DatabaseConnectionManager<C> databaseConnectionManager;
    
    private final ExecutorStatementManager<C, ?, ?> statementManager;
    
    private final StorageResourceOption option;
    
    @SuppressWarnings("rawtypes")
    private final SQLExecutionUnitBuilder sqlExecutionUnitBuilder;
    
    private final Map<String, DatabaseType> databaseTypes;
    
    public DriverExecutionPrepareEngine(final String type, final int maxConnectionsSizePerQuery, final DatabaseConnectionManager<C> databaseConnectionManager,
                                        final ExecutorStatementManager<C, ?, ?> statementManager, final StorageResourceOption option, final Collection<ShardingSphereRule> rules,
                                        final Map<String, DatabaseType> databaseTypes) {
        super(maxConnectionsSizePerQuery, rules);
        this.databaseConnectionManager = databaseConnectionManager;
        this.statementManager = statementManager;
        this.option = option;
        sqlExecutionUnitBuilder = getCachedSqlExecutionUnitBuilder(type);
        this.databaseTypes = databaseTypes;
    }
    
    /**
     * Refer to <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>.
     *
     * @param type type
     * @return sql execution unit builder
     */
    @SuppressWarnings("rawtypes")
    private SQLExecutionUnitBuilder getCachedSqlExecutionUnitBuilder(final String type) {
        SQLExecutionUnitBuilder result;
        if (null == (result = TYPE_TO_BUILDER_MAP.get(type))) {
            result = TYPE_TO_BUILDER_MAP.computeIfAbsent(type, key -> TypedSPILoader.getService(SQLExecutionUnitBuilder.class, key));
        }
        return result;
    }
    
    @Override
    protected List<ExecutionGroup<T>> group(final String dataSourceName, final int connectionOffset, final List<List<SQLUnit>> sqlUnitGroups, final ConnectionMode connectionMode) throws SQLException {
        List<ExecutionGroup<T>> result = new LinkedList<>();
        List<C> connections = databaseConnectionManager.getConnections(dataSourceName, connectionOffset, sqlUnitGroups.size(), connectionMode);
        int count = 0;
        for (List<SQLUnit> each : sqlUnitGroups) {
            result.add(createExecutionGroup(dataSourceName, each, connections.get(count++), connectionMode));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroup<T> createExecutionGroup(final String dataSourceName, final List<SQLUnit> sqlUnits, final C connection, final ConnectionMode connectionMode) throws SQLException {
        List<T> result = new LinkedList<>();
        for (SQLUnit each : sqlUnits) {
            result.add((T) sqlExecutionUnitBuilder.build(new ExecutionUnit(dataSourceName, each), statementManager, connection, connectionMode, option, databaseTypes.get(dataSourceName)));
        }
        return new ExecutionGroup<>(result);
    }
}
