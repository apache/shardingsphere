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

package org.apache.shardingsphere.driver.executor.callback.replica;

import org.apache.shardingsphere.driver.executor.callback.RuleExecuteQueryExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.queryresult.MemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.queryresult.StreamQueryResult;
import org.apache.shardingsphere.replica.execute.executor.ReplicaSQLExecutorCallback;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL executor callback for execute query for replica.
 */
public final class ReplicaExecuteQueryExecutorCallback extends ReplicaSQLExecutorCallback<QueryResult> implements RuleExecuteQueryExecutorCallback<ReplicaRule> {
    
    @Override
    public Collection<QueryResult> execute(final Collection<StatementExecuteUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        Collection<QueryResult> result = new LinkedList<>();
        for (StatementExecuteUnit each : inputs) {
            if (each.getStorageResource() instanceof PreparedStatement) {
                result.add(createQueryResult(each.getStorageResource(), each.getConnectionMode()));
            } else {
                result.add(createQueryResult(each.getExecutionUnit().getSqlUnit().getSql(), each.getStorageResource(), each.getConnectionMode()));
            }
        }
        return result;
    }
    
    private QueryResult createQueryResult(final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        ResultSet resultSet = preparedStatement.executeQuery();
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
    }
    
    private QueryResult createQueryResult(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
    }
}
