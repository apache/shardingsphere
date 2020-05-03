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

package org.apache.shardingsphere.shardingproxy.backend.response.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.underlying.common.rule.DataNodeRoutedRule;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Query header.
 */
@AllArgsConstructor
@Getter
public final class QueryHeader {
    
    private final String schema;
    
    private final String table;
    
    private final String columnLabel;
    
    private final String columnName;
    
    private final int columnLength;
    
    private final Integer columnType;
    
    private final int decimals;

    private final boolean signed;

    private final boolean primaryKey;

    private final boolean notNull;

    private final boolean autoIncrement;
    
    public QueryHeader(final ResultSetMetaData resultSetMetaData, final LogicSchema logicSchema, final int columnIndex) throws SQLException {
        this(resultSetMetaData, logicSchema, resultSetMetaData.getColumnName(columnIndex), columnIndex);
    }
    
    public QueryHeader(final ProjectionsContext projectionsContext, final ResultSetMetaData resultSetMetaData, final LogicSchema logicSchema, final int columnIndex) throws SQLException {
        this(resultSetMetaData, logicSchema, getColumnName(projectionsContext, resultSetMetaData, columnIndex), columnIndex);
    }
    
    private QueryHeader(final ResultSetMetaData resultSetMetaData, final LogicSchema logicSchema, final String columnName, final int columnIndex) throws SQLException {
        this.columnName = columnName;
        schema = logicSchema.getName();
        columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
        columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        columnType = resultSetMetaData.getColumnType(columnIndex);
        decimals = resultSetMetaData.getScale(columnIndex);
        signed = resultSetMetaData.isSigned(columnIndex);
        notNull = resultSetMetaData.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls;
        autoIncrement = resultSetMetaData.isAutoIncrement(columnIndex);
        String actualTableName = resultSetMetaData.getTableName(columnIndex);
        Optional<DataNodeRoutedRule> dataNodeRoutedRule = logicSchema.getRules().stream().filter(each -> each instanceof DataNodeRoutedRule).findFirst().map(rule -> (DataNodeRoutedRule) rule);
        if (null != actualTableName && dataNodeRoutedRule.isPresent()) {
            table = dataNodeRoutedRule.get().findLogicTableByActualTable(actualTableName).orElse("");
            TableMetaData tableMetaData = logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData().get(table);
            primaryKey = null != tableMetaData && tableMetaData.getColumns().get(resultSetMetaData.getColumnName(columnIndex).toLowerCase()).isPrimaryKey();
        } else {
            table = actualTableName;
            primaryKey = false;
        }
    }
    
    private static String getColumnName(final ProjectionsContext projectionsContext, final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        Projection projection = projectionsContext.getExpandProjections().get(columnIndex - 1);
        return projection instanceof ColumnProjection ? ((ColumnProjection) projection).getName() : resultSetMetaData.getColumnName(columnIndex);
    }
}
