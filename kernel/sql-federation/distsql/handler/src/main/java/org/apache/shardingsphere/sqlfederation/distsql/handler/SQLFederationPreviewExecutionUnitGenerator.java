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

package org.apache.shardingsphere.sqlfederation.distsql.handler;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.executor.rul.PreviewExecutionUnitGenerator;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

/**
 * Generates PREVIEW execution units for SQL federation.
 */
public final class SQLFederationPreviewExecutionUnitGenerator implements PreviewExecutionUnitGenerator {
    
    @Override
    public Optional<Collection<ExecutionUnit>> generate(final ShardingSphereDatabase database, final QueryContext queryContext, final ContextManager contextManager,
                                                        final DistSQLConnectionContext connectionContext) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        String schemaName = queryContext.getSqlStatementContext().getTablesContext().getSchemaName().orElseGet(database::getDefaultSchemaName);
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connectionContext.getExecutorEngineSupplier().get(), connectionContext.getQueryContext().getConnectionContext());
        SQLFederationEngine federationEngine = new SQLFederationEngine(database.getName(), schemaName, metaData, contextManager.getMetaDataContexts().getStatistics(), jdbcExecutor);
        if (!federationEngine.decide(queryContext, metaData.getGlobalRuleMetaData())) {
            return Optional.empty();
        }
        return Optional.of(getFederationExecutionUnits(database, queryContext, metaData, federationEngine, connectionContext));
    }
    
    private Collection<ExecutionUnit> getFederationExecutionUnits(final ShardingSphereDatabase database, final QueryContext queryContext, final ShardingSphereMetaData metaData,
                                                                  final SQLFederationEngine federationEngine, final DistSQLConnectionContext connectionContext) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(database, metaData, connectionContext);
        SQLFederationContext context = new SQLFederationContext(true, queryContext, metaData, connectionContext.getProcessId());
        federationEngine.executeQuery(prepareEngine, createPreviewCallback(database, sqlStatement), context);
        return context.getPreviewExecutionUnits();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData,
                                                                                                           final DistSQLConnectionContext connectionContext) {
        int maxConnectionsSizePerQuery = metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connectionContext.getDatabaseConnectionManager(),
                connectionContext.getExecutorStatementManager(), new StatementOption(false), database.getRuleMetaData().getRules(), metaData);
    }
    
    private JDBCExecutorCallback<ExecuteResult> createPreviewCallback(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        return new JDBCExecutorCallback<ExecuteResult>(database.getProtocolType(), database.getResourceMetaData(), sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown()) {
            
            @Override
            protected ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return new JDBCStreamQueryResult(statement.executeQuery(sql));
            }
            
            @Override
            protected Optional<ExecuteResult> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
}
