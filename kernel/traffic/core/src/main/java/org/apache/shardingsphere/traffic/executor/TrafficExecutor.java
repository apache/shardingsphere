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

package org.apache.shardingsphere.traffic.executor;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.traffic.exception.EmptyTrafficExecutionUnitException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Traffic executor.
 */
public final class TrafficExecutor implements AutoCloseable {
    
    private Statement statement;
    
    @Getter
    private ResultSet resultSet;
    
    /**
     * Execute.
     *
     * @param processId process ID
     * @param databaseName database name
     * @param trafficInstanceId traffic instance ID
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param <T> return type
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> T execute(final String processId, final String databaseName, final String trafficInstanceId, final QueryContext queryContext,
                         final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final TrafficExecutorCallback<T> callback) throws SQLException {
        JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(processId, databaseName, trafficInstanceId, queryContext, prepareEngine);
        SQLUnit sqlUnit = executionUnit.getExecutionUnit().getSqlUnit();
        cacheStatement(sqlUnit.getParameters(), executionUnit.getStorageResource());
        T result = callback.execute(statement, sqlUnit.getSql());
        resultSet = statement.getResultSet();
        return result;
    }
    
    private JDBCExecutionUnit createTrafficExecutionUnit(final String processId, final String databaseName, final String trafficInstanceId, final QueryContext queryContext,
                                                         final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        ExecutionUnit executionUnit = new ExecutionUnit(trafficInstanceId, new SQLUnit(queryContext.getSql(), queryContext.getParameters()));
        ExecutionGroupContext<JDBCExecutionUnit> context =
                prepareEngine.prepare(new RouteContext(), Collections.singleton(executionUnit), new ExecutionGroupReportContext(processId, databaseName, new Grantee("", "")));
        return context.getInputGroups().stream().flatMap(each -> each.getInputs().stream()).findFirst().orElseThrow(EmptyTrafficExecutionUnitException::new);
    }
    
    private void cacheStatement(final List<Object> params, final Statement statement) throws SQLException {
        this.statement = statement;
        setParameters(statement, params);
    }
    
    private void setParameters(final Statement statement, final List<Object> params) throws SQLException {
        if (!(statement instanceof PreparedStatement)) {
            return;
        }
        int index = 1;
        for (Object each : params) {
            ((PreparedStatement) statement).setObject(index++, each);
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (null != statement) {
            statement.close();
        }
    }
}
