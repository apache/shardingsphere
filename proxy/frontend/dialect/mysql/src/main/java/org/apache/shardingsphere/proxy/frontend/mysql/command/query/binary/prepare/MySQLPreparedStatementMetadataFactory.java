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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.PreparedStatementMetadataResolutionException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Metadata factory for MySQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLPreparedStatementMetadataFactory {
    
    /**
     * Load actual prepared statement for metadata access.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @return actual prepared statement
     * @throws SQLException SQL exception
     */
    public static PreparedStatement load(final ConnectionSession connectionSession, final MySQLServerPreparedStatement preparedStatement) throws SQLException {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        QueryContext queryContext = new QueryContext(preparedStatement.getSqlStatementContext(), preparedStatement.getSql(),
                Collections.emptyList(), preparedStatement.getHintValueContext(), connectionSession.getConnectionContext(), metaData);
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps());
        ShardingSpherePreconditions.checkNotEmpty(executionContext.getExecutionUnits(),
                () -> new PreparedStatementMetadataResolutionException("no execution unit was generated"));
        ExecutionUnit executionUnit = executionContext.getExecutionUnits().iterator().next();
        ProxyDatabaseConnectionManager databaseConnectionManager = connectionSession.getDatabaseConnectionManager();
        List<Connection> connections = databaseConnectionManager.getConnections(
                queryContext.getUsedDatabase().getName(), executionUnit.getDataSourceName(), 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        ShardingSpherePreconditions.checkNotEmpty(connections,
                () -> new PreparedStatementMetadataResolutionException("no backend connection was acquired"));
        return connections.iterator().next().prepareStatement(executionUnit.getSqlUnit().getSql());
    }
}
