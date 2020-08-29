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

package org.apache.shardingsphere.infra.executor.sql.resourced.group;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.group.AbstractExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.ExecutionConnection;
import org.apache.shardingsphere.infra.executor.sql.resourced.ResourceManagedExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.StorageResourceOption;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Resource managed execute group engine.
 * 
 * @param <U> type of storage resource execute unit
 * @param <E> type of execution connection
 * @param <C> type of resource connection
 * @param <O> type of storage resource option
 */
public abstract class ResourceManagedExecuteGroupEngine
        <U extends ResourceManagedExecuteUnit, E extends ExecutionConnection<C, ?, O>, C, O extends StorageResourceOption> extends AbstractExecuteGroupEngine<U> {
    
    private final int maxConnectionsSizePerQuery;
    
    private final E executionConnection;
    
    private final O option;

    protected ResourceManagedExecuteGroupEngine(final int maxConnectionsSizePerQuery, final E executionConnection, final O option, final Collection<ShardingSphereRule> rules,
                                                final ExecutionContext executionContext) {
        super(rules, executionContext);
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
        this.executionConnection = executionConnection;
        this.option = option;
    }
    
    @Override
    protected final List<InputGroup<U>> generateSQLExecuteGroups(final String dataSourceName, final List<SQLUnit> sqlUnits) throws SQLException {
        List<InputGroup<U>> result = new LinkedList<>();
        int desiredPartitionSize = Math.max(0 == sqlUnits.size() % maxConnectionsSizePerQuery ? sqlUnits.size() / maxConnectionsSizePerQuery : sqlUnits.size() / maxConnectionsSizePerQuery + 1, 1);
        List<List<SQLUnit>> sqlUnitPartitions = Lists.partition(sqlUnits, desiredPartitionSize);
        ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnits.size() ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
        List<C> connections = executionConnection.getConnections(dataSourceName, sqlUnitPartitions.size(), connectionMode);
        int count = 0;
        for (List<SQLUnit> each : sqlUnitPartitions) {
            result.add(generateSQLExecuteGroup(dataSourceName, each, connections.get(count++), connectionMode));
        }
        return result;
    }
    
    private InputGroup<U> generateSQLExecuteGroup(final String dataSourceName, final List<SQLUnit> sqlUnitGroup,
                                                  final C connection, final ConnectionMode connectionMode) throws SQLException {
        List<U> result = new LinkedList<>();
        for (SQLUnit each : sqlUnitGroup) {
            result.add(createStorageResourceExecuteUnit(new ExecutionUnit(dataSourceName, each), executionConnection, connection, connectionMode, option));
        }
        return new InputGroup<>(result);
    }
    
    protected abstract U createStorageResourceExecuteUnit(ExecutionUnit executionUnit, E executionConnection, C connection, ConnectionMode connectionMode, O option) throws SQLException;
}
