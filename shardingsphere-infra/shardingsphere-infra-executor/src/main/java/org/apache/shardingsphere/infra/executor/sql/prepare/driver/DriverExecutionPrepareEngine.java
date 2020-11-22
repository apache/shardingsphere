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

import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.AbstractExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.driver.DriverExecutionUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Driver execution prepare engine.
 * 
 * @param <T> type of storage resource execute unit
 * @param <M> type of driver executor manager
 * @param <C> type of resource connection
 * @param <O> type of storage resource option
 */
public abstract class DriverExecutionPrepareEngine
        <T extends DriverExecutionUnit<?>, M extends ExecutorDriverManager<C, ?, O>, C, O extends StorageResourceOption> extends AbstractExecutionPrepareEngine<T> {
    
    private final M executorDriverManager;
    
    private final O option;
    
    protected DriverExecutionPrepareEngine(final int maxConnectionsSizePerQuery, final M executorDriverManager, final O option, final Collection<ShardingSphereRule> rules) {
        super(maxConnectionsSizePerQuery, rules);
        this.executorDriverManager = executorDriverManager;
        this.option = option;
    }
    
    @Override
    protected final List<ExecutionGroup<T>> group(final String dataSourceName, final List<List<SQLUnit>> sqlUnitGroups, final ConnectionMode connectionMode) throws SQLException {
        List<ExecutionGroup<T>> result = new LinkedList<>();
        List<C> connections = executorDriverManager.getConnections(dataSourceName, sqlUnitGroups.size(), connectionMode);
        int count = 0;
        for (List<SQLUnit> each : sqlUnitGroups) {
            result.add(createExecutionGroup(dataSourceName, each, connections.get(count++), connectionMode));
        }
        return result;
    }
    
    private ExecutionGroup<T> createExecutionGroup(final String dataSourceName, final List<SQLUnit> sqlUnits, final C connection, final ConnectionMode connectionMode) throws SQLException {
        List<T> result = new LinkedList<>();
        for (SQLUnit each : sqlUnits) {
            result.add(createDriverSQLExecutionUnit(new ExecutionUnit(dataSourceName, each), executorDriverManager, connection, connectionMode, option));
        }
        return new ExecutionGroup<>(result);
    }
    
    protected abstract T createDriverSQLExecutionUnit(ExecutionUnit executionUnit, M executorManager, C connection, ConnectionMode connectionMode, O option) throws SQLException;
}
