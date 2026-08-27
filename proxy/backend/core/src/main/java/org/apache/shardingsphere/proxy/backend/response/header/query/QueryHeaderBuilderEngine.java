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

package org.apache.shardingsphere.proxy.backend.response.header.query;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Query header builder engine.
 */
public final class QueryHeaderBuilderEngine {
    
    private final QueryHeaderBuilder queryHeaderBuilder;
    
    public QueryHeaderBuilderEngine(final DatabaseType databaseType) {
        queryHeaderBuilder = DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, databaseType);
    }
    
    /**
     * Build query header.
     *
     * @param resultSetMetaData result set meta data
     * @param database database
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public QueryHeader build(final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database, final int columnIndex) throws SQLException {
        String columnName = resultSetMetaData.getColumnName(columnIndex);
        String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
        return queryHeaderBuilder.build(resultSetMetaData, database, columnName, columnLabel, columnIndex);
    }
    
    /**
     * Build query header.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData result set meta data
     * @param database current database
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public QueryHeader build(final SQLStatementContext sqlStatementContext, final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database,
                             final int columnIndex) throws SQLException {
        return build(resultSetMetaData, database, columnIndex);
    }
    
    /**
     * Build query header.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData result set meta data
     * @param resultSet JDBC result set
     * @param database current database
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public QueryHeader build(final SQLStatementContext sqlStatementContext, final ShardingSphereResultSetMetaData resultSetMetaData, final ResultSet resultSet,
                             final ShardingSphereDatabase database, final int columnIndex) throws SQLException {
        QueryHeader result = build(resultSetMetaData, database, columnIndex);
        queryHeaderBuilder.appendProtocolAttributes(result, resultSet);
        return result;
    }
}
