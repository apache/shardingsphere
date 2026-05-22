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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.ColumnIndexOutOfRangeException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.sql.SQLException;

/**
 * Query header builder engine.
 */
public final class QueryHeaderBuilderEngine {
    
    private final QueryHeaderBuilder queryHeaderBuilder;
    
    /**
     * Create query header builder engine.
     *
     * @param databaseType database type
     */
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
     * @param projectionsContext projections context
     * @param resultSetMetaData result set meta data
     * @param database database
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public QueryHeader build(final ProjectionsContext projectionsContext, final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database,
                             final int columnIndex) throws SQLException {
        ShardingSpherePreconditions.checkState(columnIndex <= projectionsContext.getExpandProjections().size(), () -> new ColumnIndexOutOfRangeException(columnIndex));
        Projection projection = projectionsContext.getExpandProjections().get(columnIndex - 1);
        return queryHeaderBuilder.build(resultSetMetaData, database, projection.getColumnName(), projection.getColumnLabel(), columnIndex);
    }
}
