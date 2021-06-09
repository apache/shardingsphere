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

package org.apache.shardingsphere.infra.executor.sql.federate.execute;

import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.federate.schema.FederateLogicSchema;
import org.apache.shardingsphere.infra.executor.sql.federate.schema.row.FederateRowExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Federate JDBC executor.
 */
public final class FederateJDBCExecutor implements FederateExecutor {
    
    public static final String CONNECTION_URL = "jdbc:calcite:";
    
    public static final String DRIVER_NAME = "org.apache.calcite.jdbc.Driver";
    
    private final String schema;
    
    private final OptimizeContextFactory factory;
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    private final ExecutorJDBCManager jdbcManager;
    
    private final JDBCExecutor jdbcExecutor;
    
    private Statement statement;
    
    static {
        try {
            Class.forName(DRIVER_NAME);
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public FederateJDBCExecutor(final String schema, final OptimizeContextFactory factory, final Collection<ShardingSphereRule> rules, 
                                final ConfigurationProperties props, final ExecutorJDBCManager jdbcManager, final JDBCExecutor jdbcExecutor) {
        this.schema = schema;
        this.factory = factory;
        this.rules = rules;
        this.props = props;
        this.jdbcManager = jdbcManager;
        this.jdbcExecutor = jdbcExecutor;
    }
    
    @Override
    public List<QueryResult> executeQuery(final ExecutionContext executionContext, final JDBCExecutorCallback<? extends ExecuteResult> callback) throws SQLException {
        QueryResult result = new JDBCStreamQueryResult(execute(executionContext, callback));
        return Collections.singletonList(result);
    }
    
    @Override
    public void close() throws SQLException {
        if (null != statement) {
            Connection connection = statement.getConnection();
            connection.close();
            statement.close();
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }
    
    private ResultSet execute(final ExecutionContext executionContext, final JDBCExecutorCallback<? extends ExecuteResult> callback) throws SQLException {
        SQLUnit sqlUnit = executionContext.getExecutionUnits().iterator().next().getSqlUnit();
        PreparedStatement statement = getConnection(executionContext, callback).prepareStatement(SQLUtil.trimSemicolon(sqlUnit.getSql()));
        setParameters(statement, sqlUnit.getParameters());
        this.statement = statement;
        return statement.executeQuery();
    }
    
    private Connection getConnection(final ExecutionContext executionContext, final JDBCExecutorCallback<? extends ExecuteResult> callback) throws SQLException {
        Connection result = DriverManager.getConnection(CONNECTION_URL, getProperties());
        CalciteConnection calciteConnection = result.unwrap(CalciteConnection.class);
        addSchema(calciteConnection, executionContext, callback);
        return result;
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.LEX.camelName(), factory.getProperties().getProperty(CalciteConnectionProperty.LEX.camelName()));
        result.setProperty(CalciteConnectionProperty.CONFORMANCE.camelName(), factory.getProperties().getProperty(CalciteConnectionProperty.CONFORMANCE.camelName()));
        return result;
    }
    
    private void addSchema(final CalciteConnection calciteConnection, final ExecutionContext executionContext, final JDBCExecutorCallback<? extends ExecuteResult> callback) throws SQLException {
        FederateRowExecutor executor = new FederateRowExecutor(rules, props, jdbcManager, jdbcExecutor, executionContext, callback);
        FederateLogicSchema logicSchema = new FederateLogicSchema(factory.getSchemaMetadatas().getSchemaMetadataBySchemaName(schema), executor);
        calciteConnection.getRootSchema().add(schema, logicSchema);
        calciteConnection.setSchema(schema);
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        int count = 1;
        for (Object each : parameters) {
            preparedStatement.setObject(count, each);
            count++;
        }
    }
}
