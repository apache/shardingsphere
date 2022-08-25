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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.federation.executor.FederationContext;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.common.table.CommonTableScanExecutorContext;
import org.apache.shardingsphere.infra.federation.executor.original.table.FilterableTableScanExecutor;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.filter.FilterableDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Original federation executor.
 */
@RequiredArgsConstructor
public final class OriginalFederationExecutor implements FederationExecutor {
    
    public static final String CONNECTION_URL = "jdbc:calcite:";
    
    public static final String DRIVER_NAME = "org.apache.calcite.jdbc.Driver";
    
    private final String databaseName;
    
    private final String schemaName;
    
    private final OptimizerContext optimizerContext;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final EventBusContext eventBusContext;
    
    private Statement statement;
    
    static {
        try {
            Class.forName(DRIVER_NAME);
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final FederationContext federationContext) throws SQLException {
        Connection connection = createConnection(prepareEngine, callback, federationContext);
        PreparedStatement preparedStatement = connection.prepareStatement(SQLUtil.trimSemicolon(federationContext.getQueryContext().getSql()));
        setParameters(preparedStatement, federationContext.getQueryContext().getParameters());
        this.statement = preparedStatement;
        return preparedStatement.executeQuery();
    }
    
    private Connection createConnection(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                        final JDBCExecutorCallback<? extends ExecuteResult> callback, final FederationContext federationContext) throws SQLException {
        Connection result = DriverManager.getConnection(CONNECTION_URL, optimizerContext.getParserContexts().get(databaseName).getDialectProps());
        addSchema(result.unwrap(CalciteConnection.class), prepareEngine, callback, federationContext);
        return result;
    }
    
    private void addSchema(final CalciteConnection connection, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           final JDBCExecutorCallback<? extends ExecuteResult> callback, final FederationContext federationContext) throws SQLException {
        CommonTableScanExecutorContext executorContext = new CommonTableScanExecutorContext(databaseName, schemaName, props, federationContext);
        FilterableTableScanExecutor executor = new FilterableTableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, globalRuleMetaData, executorContext, eventBusContext);
        FilterableDatabase database = new FilterableDatabase(federationContext.getDatabases().get(databaseName.toLowerCase()), executor);
        // TODO support database.schema.table query when switch to CustomizedFilterableExecutor, calcite jdbc just support schema.table query now
        connection.getRootSchema().add(schemaName, database.getSubSchema(schemaName));
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
