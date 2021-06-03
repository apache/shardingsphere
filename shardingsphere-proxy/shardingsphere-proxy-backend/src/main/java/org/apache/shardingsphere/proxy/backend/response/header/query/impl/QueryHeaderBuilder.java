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

package org.apache.shardingsphere.proxy.backend.response.header.query.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Query header builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryHeaderBuilder {
    
    /**
     * Build query header builder.
     *
     * @param queryResultMetaData query result meta data
     * @param metaData ShardingSphere meta data
     * @param columnIndex column index 
     * @return query header
     * @throws SQLException SQL exception
     */
    public static QueryHeader build(final QueryResultMetaData queryResultMetaData, final ShardingSphereMetaData metaData, final int columnIndex) throws SQLException {
        return build(queryResultMetaData, metaData, queryResultMetaData.getColumnName(columnIndex), columnIndex);
    }
    
    /**
     * Build query header builder.
     *
     * @param projectionsContext projections context
     * @param queryResultMetaData query result meta data
     * @param metaData ShardingSphere meta data
     * @param columnIndex column index
     * @return query header
     * @throws SQLException SQL exception
     */
    public static QueryHeader build(final ProjectionsContext projectionsContext, 
                                    final QueryResultMetaData queryResultMetaData, final ShardingSphereMetaData metaData, final int columnIndex) throws SQLException {
        return build(queryResultMetaData, metaData, getColumnName(projectionsContext, queryResultMetaData, columnIndex), columnIndex);
    }
    
    private static QueryHeader build(final QueryResultMetaData queryResultMetaData, final ShardingSphereMetaData metaData, final String columnName, final int columnIndex) throws SQLException {
        String schemaName = null == metaData ? "" : metaData.getName();
        String actualTableName = queryResultMetaData.getTableName(columnIndex);
        Optional<DataNodeContainedRule> dataNodeContainedRule = null == metaData
                ? Optional.empty() : metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule).findFirst().map(rule -> (DataNodeContainedRule) rule);
        String tableName;
        boolean primaryKey;
        if (null != actualTableName && dataNodeContainedRule.isPresent()) {
            tableName = dataNodeContainedRule.get().findLogicTableByActualTable(actualTableName).orElse("");
            TableMetaData tableMetaData = metaData.getSchema().get(tableName);
            primaryKey = null != tableMetaData && Optional.ofNullable(tableMetaData.getColumns().get(columnName.toLowerCase())).map(ColumnMetaData::isPrimaryKey).orElse(false);
        } else {
            tableName = actualTableName;
            primaryKey = false;
        }
        String columnLabel = queryResultMetaData.getColumnLabel(columnIndex);
        int columnType = queryResultMetaData.getColumnType(columnIndex);
        String columnTypeName = queryResultMetaData.getColumnTypeName(columnIndex);
        int columnLength = queryResultMetaData.getColumnLength(columnIndex);
        int decimals = queryResultMetaData.getDecimals(columnIndex);
        boolean signed = queryResultMetaData.isSigned(columnIndex);
        boolean notNull = queryResultMetaData.isNotNull(columnIndex);
        boolean autoIncrement = queryResultMetaData.isAutoIncrement(columnIndex);
        return new QueryHeader(schemaName, tableName, columnLabel, columnName, columnType, columnTypeName, columnLength, decimals, signed, primaryKey, notNull, autoIncrement);
    }
    
    private static String getColumnName(final ProjectionsContext projectionsContext, final QueryResultMetaData queryResultMetaData, final int columnIndex) throws SQLException {
        Projection projection = projectionsContext.getExpandProjections().get(columnIndex - 1);
        return projection instanceof ColumnProjection ? ((ColumnProjection) projection).getName() : queryResultMetaData.getColumnName(columnIndex);
    }
}
