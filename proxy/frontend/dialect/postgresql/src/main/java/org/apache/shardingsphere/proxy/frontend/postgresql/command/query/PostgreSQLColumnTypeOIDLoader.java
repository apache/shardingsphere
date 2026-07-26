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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Oid;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loader for PostgreSQL column type OIDs.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLColumnTypeOIDLoader {
    
    /**
     * Load composite column type OIDs from the routed backend connection.
     *
     * @param connectionSession connection session
     * @param queryContext query context
     * @param queryHeaders query headers
     * @return column indexes to type OIDs, or an empty map if no composite column type can be resolved
     * @throws SQLException SQL exception
     */
    public static Map<Integer, Integer> load(final ConnectionSession connectionSession, final QueryContext queryContext, final List<QueryHeader> queryHeaders) throws SQLException {
        if (!containsCompositeType(queryHeaders)) {
            return Collections.emptyMap();
        }
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, queryContext.getMetaData().getGlobalRuleMetaData(), queryContext.getMetaData().getProps());
        if (executionContext.getExecutionUnits().isEmpty()) {
            return Collections.emptyMap();
        }
        ExecutionUnit executionUnit = executionContext.getExecutionUnits().iterator().next();
        for (ExecutionUnit each : executionContext.getExecutionUnits()) {
            if (!executionUnit.getDataSourceName().equals(each.getDataSourceName())) {
                return Collections.emptyMap();
            }
        }
        List<Connection> connections = connectionSession.getDatabaseConnectionManager().getConnections(
                connectionSession.getUsedDatabaseName(), executionUnit.getDataSourceName(), 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        return load(connections.get(0), queryHeaders);
    }
    
    private static Map<Integer, Integer> load(final Connection connection, final List<QueryHeader> queryHeaders) throws SQLException {
        return connection.isWrapperFor(BaseConnection.class) ? getTypeOIDs(connection.unwrap(BaseConnection.class), queryHeaders) : Collections.emptyMap();
    }
    
    /**
     * Load composite column type OIDs from result set metadata.
     *
     * @param connection database connection
     * @param metaData result set metadata
     * @return column indexes to type OIDs, or an empty map if no composite column type can be resolved
     * @throws SQLException SQL exception
     */
    public static Map<Integer, Integer> load(final Connection connection, final ResultSetMetaData metaData) throws SQLException {
        return connection.isWrapperFor(BaseConnection.class) ? getTypeOIDs(connection.unwrap(BaseConnection.class), metaData) : Collections.emptyMap();
    }
    
    private static boolean containsCompositeType(final List<QueryHeader> queryHeaders) {
        for (QueryHeader each : queryHeaders) {
            if (Types.STRUCT == each.getColumnType()) {
                return true;
            }
        }
        return false;
    }
    
    private static Map<Integer, Integer> getTypeOIDs(final BaseConnection connection, final List<QueryHeader> queryHeaders) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        for (int i = 0; i < queryHeaders.size(); i++) {
            QueryHeader each = queryHeaders.get(i);
            if (Types.STRUCT == each.getColumnType()) {
                Optional<Integer> typeOID = findTypeOID(connection, each.getColumnTypeName());
                if (typeOID.isPresent()) {
                    result.put(i + 1, typeOID.get());
                }
            }
        }
        return result;
    }
    
    private static Map<Integer, Integer> getTypeOIDs(final BaseConnection connection, final ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        Map<Integer, Integer> result = new HashMap<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (Types.STRUCT == metaData.getColumnType(columnIndex)) {
                Optional<Integer> typeOID = findTypeOID(connection, metaData.getColumnTypeName(columnIndex));
                if (typeOID.isPresent()) {
                    result.put(columnIndex, typeOID.get());
                }
            }
        }
        return result;
    }
    
    private static Optional<Integer> findTypeOID(final BaseConnection connection, final String columnTypeName) throws SQLException {
        int typeOID = connection.getTypeInfo().getPGType(columnTypeName);
        return Oid.UNSPECIFIED == typeOID ? Optional.empty() : Optional.of(typeOID);
    }
}
