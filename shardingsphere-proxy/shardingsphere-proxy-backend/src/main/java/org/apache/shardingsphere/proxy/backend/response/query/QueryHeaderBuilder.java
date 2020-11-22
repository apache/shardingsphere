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
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
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
     * @param metaData meta data name
     * @param columnIndex column index 
     * @return query header
     * @throws SQLException SQL exception
     */
    public static QueryHeader build(final ResultSetMetaData resultSetMetaData, final ShardingSphereMetaData metaData, final int columnIndex) throws SQLException {
        return build(resultSetMetaData, metaData, resultSetMetaData.getColumnName(columnIndex), columnIndex);
    }
    
    /**
     * Build query header builder.
     * 
     * @param projectionsContext projections context
     * @param resultSetMetaData result set meta data
     * @param metaData meta data name
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public static QueryHeader build(final ProjectionsContext projectionsContext,
                                    final ResultSetMetaData resultSetMetaData, final ShardingSphereMetaData metaData, final int columnIndex) throws SQLException {
        return build(resultSetMetaData, metaData, getColumnName(projectionsContext, resultSetMetaData, columnIndex), columnIndex);
    }
    
    private static QueryHeader build(final ResultSetMetaData resultSetMetaData, final ShardingSphereMetaData metaData, final String columnName, final int columnIndex) throws SQLException {
        String schemaName = metaData.getName();
        String actualTableName = resultSetMetaData.getTableName(columnIndex);
        Optional<DataNodeContainedRule> dataNodeContainedRule = 
                metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule).findFirst().map(rule -> (DataNodeContainedRule) rule);
        String tableName;
        boolean primaryKey;
        if (null != actualTableName && dataNodeContainedRule.isPresent()) {
            tableName = dataNodeContainedRule.get().findLogicTableByActualTable(actualTableName).orElse("");
            TableMetaData tableMetaData = metaData.getSchema().get(tableName);
            primaryKey = null != tableMetaData && tableMetaData.getColumns().get(columnName.toLowerCase()).isPrimaryKey();
        } else {
            tableName = actualTableName;
            primaryKey = false;
        }
        String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
        int columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        int columnType = resultSetMetaData.getColumnType(columnIndex);
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
