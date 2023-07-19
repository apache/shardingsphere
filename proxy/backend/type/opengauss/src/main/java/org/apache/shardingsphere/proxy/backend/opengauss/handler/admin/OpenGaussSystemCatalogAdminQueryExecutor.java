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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.executor.SQLFederationExecutorContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Select database executor for openGauss.
 */
@RequiredArgsConstructor
public final class OpenGaussSystemCatalogAdminQueryExecutor implements DatabaseAdminQueryExecutor {
    
    private static final String PG_CATALOG = "pg_catalog";
    
    private final SQLStatementContext sqlStatementContext;
    
    private final String sql;
    
    private final String databaseName;
    
    private final List<Object> parameters;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext());
        try (SQLFederationEngine sqlFederationEngine = new SQLFederationEngine(databaseName, PG_CATALOG, metaDataContexts.getMetaData(), metaDataContexts.getStatistics(), jdbcExecutor)) {
            DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(metaDataContexts, connectionSession);
            SQLFederationExecutorContext context = new SQLFederationExecutorContext(false, new QueryContext(sqlStatementContext, sql, parameters), metaDataContexts.getMetaData());
            ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
            ResultSet resultSet = sqlFederationEngine.executeQuery(prepareEngine,
                    createOpenGaussSystemCatalogAdminQueryCallback(database.getProtocolType(), database.getResourceMetaData(), sqlStatementContext.getSqlStatement()), context);
            queryResultMetaData = new JDBCQueryResultMetaData(resultSet.getMetaData());
            mergedResult = new IteratorStreamMergedResult(Collections.singletonList(new JDBCMemoryQueryResult(resultSet, connectionSession.getProtocolType())));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final MetaDataContexts metaDataContexts, final ConnectionSession connectionSession) {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connectionSession.getDatabaseConnectionManager(),
                connectionSession.getStatementManager(), new StatementOption(false),
                metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageTypes());
    }
    
    private JDBCExecutorCallback<ExecuteResult> createOpenGaussSystemCatalogAdminQueryCallback(final DatabaseType protocolType, final ShardingSphereResourceMetaData resourceMetaData,
                                                                                               final SQLStatement sqlStatement) {
        return new JDBCExecutorCallback<ExecuteResult>(protocolType, resourceMetaData, sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown()) {
            
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
