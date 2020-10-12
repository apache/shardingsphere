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

package org.apache.shardingsphere.proxy.backend.response.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.table.TableMetaData;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Query header.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryHeaderBuilder {
    
    /**
     * Build query header builder.
     * 
     * @param resultSetMetaData result set meta data
     * @param schema schema name
     * @param columnIndex column index 
     * @return query header
     * @throws SQLException SQL exception
     */
    public static QueryHeader build(final ResultSetMetaData resultSetMetaData, final ShardingSphereSchema schema, final int columnIndex) throws SQLException {
        return build(resultSetMetaData, schema, resultSetMetaData.getColumnName(columnIndex), columnIndex);
    }
    
    /**
     * Build query header builder.
     * 
     * @param projectionsContext projections context
     * @param resultSetMetaData result set meta data
     * @param schema schema name
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public static QueryHeader build(final ProjectionsContext projectionsContext,
                                    final ResultSetMetaData resultSetMetaData, final ShardingSphereSchema schema, final int columnIndex) throws SQLException {
        return build(resultSetMetaData, schema, getColumnName(projectionsContext, resultSetMetaData, columnIndex), columnIndex);
    }
    
    private static QueryHeader build(final ResultSetMetaData resultSetMetaData, final ShardingSphereSchema schema, final String columnName, final int columnIndex) throws SQLException {
        String schemaName = schema.getName();
        String actualTableName = resultSetMetaData.getTableName(columnIndex);
        Optional<DataNodeRoutedRule> dataNodeRoutedRule = schema.getRules().stream().filter(each -> each instanceof DataNodeRoutedRule).findFirst().map(rule -> (DataNodeRoutedRule) rule);
        String tableName;
        boolean primaryKey;
        if (null != actualTableName && dataNodeRoutedRule.isPresent()) {
            tableName = dataNodeRoutedRule.get().findLogicTableByActualTable(actualTableName).orElse("");
            TableMetaData tableMetaData = schema.getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get(tableName);
            primaryKey = null != tableMetaData && tableMetaData.getColumns().get(columnName.toLowerCase()).isPrimaryKey();
        } else {
            tableName = actualTableName;
            primaryKey = false;
        }
        String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
        int columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        Integer columnType = resultSetMetaData.getColumnType(columnIndex);
        int decimals = resultSetMetaData.getScale(columnIndex);
        boolean signed = resultSetMetaData.isSigned(columnIndex);
        boolean notNull = resultSetMetaData.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls;
        boolean autoIncrement = resultSetMetaData.isAutoIncrement(columnIndex);
        return new QueryHeader(schemaName, tableName, columnLabel, columnName, columnLength, columnType, decimals, signed, primaryKey, notNull, autoIncrement);
    }
    
    private static String getColumnName(final ProjectionsContext projectionsContext, final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        Projection projection = projectionsContext.getExpandProjections().get(columnIndex - 1);
        return projection instanceof ColumnProjection ? ((ColumnProjection) projection).getName() : resultSetMetaData.getColumnName(columnIndex);
    }
}
