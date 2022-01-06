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

package org.apache.shardingsphere.infra.federation.executor.original;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.original.table.FilterableTableScanExecutor;
import org.apache.shardingsphere.infra.federation.executor.original.table.FilterableTableScanExecutorContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Original filterable executor.
 */
@RequiredArgsConstructor
public final class OriginalFilterableExecutor implements FederationExecutor {
    
    public static final String CONNECTION_URL = "jdbc:calcite:";
    
    public static final String DRIVER_NAME = "org.apache.calcite.jdbc.Driver";
    
    private final String schemaName;
    
    private final OptimizerContext optimizerContext;
    
    private final ConfigurationProperties props;
    
    private final JDBCExecutor jdbcExecutor;
    
    private Statement statement;
    
    static {
        try {
            Class.forName(DRIVER_NAME);
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final JDBCExecutorCallback<? extends ExecuteResult> callback, 
                                  final LogicSQL logicSQL, final Map<String, ShardingSphereMetaData> metaDataMap) throws SQLException {
        PreparedStatement preparedStatement = createConnection(prepareEngine, callback, logicSQL.getParameters(), metaDataMap).prepareStatement(SQLUtil.trimSemicolon(logicSQL.getSql()));
        setParameters(preparedStatement, logicSQL.getParameters());
        this.statement = preparedStatement;
        return preparedStatement.executeQuery();
    }
    
    private Connection createConnection(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final JDBCExecutorCallback<? extends ExecuteResult> callback, 
                                        final List<Object> parameters, final Map<String, ShardingSphereMetaData> metaDataMap) throws SQLException {
        Connection result = DriverManager.getConnection(CONNECTION_URL, optimizerContext.getParserContexts().get(schemaName).getDialectProps());
        addSchema(result.unwrap(CalciteConnection.class), prepareEngine, callback, parameters, metaDataMap);
        return result;
    }
    
    private void addSchema(final CalciteConnection connection, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, 
                           final JDBCExecutorCallback<? extends ExecuteResult> callback, final List<Object> parameters, final Map<String, ShardingSphereMetaData> metaDataMap) throws SQLException {
        FilterableTableScanExecutorContext executorContext = new FilterableTableScanExecutorContext(schemaName, parameters, props, metaDataMap);
        FilterableTableScanExecutor executor = new FilterableTableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, executorContext);
        FilterableSchema schema = new FilterableSchema(optimizerContext.getFederationMetaData().getSchemas().get(schemaName), executor);
        connection.getRootSchema().add(schemaName, schema);
        connection.setSchema(schemaName);
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        int count = 1;
        for (Object each : parameters) {
            preparedStatement.setObject(count, each);
            count++;
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != statement && !statement.isClosed()) {
            Connection connection = statement.getConnection();
            statement.close();
            connection.close();
        }
    }
}
