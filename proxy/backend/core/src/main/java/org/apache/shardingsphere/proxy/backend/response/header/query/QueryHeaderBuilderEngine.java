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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.ColumnIndexOutOfRangeException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Query header builder engine.
 */
public final class QueryHeaderBuilderEngine {
    
    private final QueryHeaderBuilder queryHeaderBuilder;
    
    public QueryHeaderBuilderEngine(final DatabaseType databaseType) {
        queryHeaderBuilder = DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, databaseType);
    }
    
    /**
     * Build query header builder.
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
     * Build query header builder.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData result set meta data
     * @param database current database
     * @param databases available databases
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public QueryHeader build(final SQLStatementContext sqlStatementContext, final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database,
                             final Collection<ShardingSphereDatabase> databases, final int columnIndex) throws SQLException {
        Projection projection = findProjection(sqlStatementContext, columnIndex);
        return null == projection
                ? build(resultSetMetaData, database, columnIndex)
                : queryHeaderBuilder.build(resultSetMetaData, database, projection.getColumnName(), projection.getColumnLabel(), columnIndex);
    }
    
    private Projection findProjection(final SQLStatementContext sqlStatementContext, final int columnIndex) {
        if (!(sqlStatementContext instanceof SelectStatementContext) || !((SelectStatementContext) sqlStatementContext).containsDerivedProjections()) {
            return null;
        }
        checkColumnIndex(sqlStatementContext, columnIndex);
        return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().get(columnIndex - 1);
    }
    
    private void checkColumnIndex(final SQLStatementContext sqlStatementContext, final int columnIndex) {
        if (sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections()) {
            ShardingSpherePreconditions.checkState(columnIndex <= ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().size(),
                    () -> new ColumnIndexOutOfRangeException(columnIndex));
        }
    }
}
