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
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Query header.
 *
 * @author zhangliang
 */
@AllArgsConstructor
@Getter
public final class QueryHeader {
    
    private final String schema;
    
    private final String table;
    
    private String columnLabel;
    
    private String columnName;
    
    private final int columnLength;
    
    private final Integer columnType;
    
    private final int decimals;

    private final boolean signed;

    private final boolean primaryKey;

    private final boolean notNull;

    private final boolean autoIncrement;
    
    public QueryHeader(final ResultSetMetaData resultSetMetaData, final LogicSchema logicSchema, final int columnIndex) throws SQLException {
        schema = logicSchema.getName();
        columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
        columnName = resultSetMetaData.getColumnName(columnIndex);
        columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        columnType = resultSetMetaData.getColumnType(columnIndex);
        decimals = resultSetMetaData.getScale(columnIndex);
        signed = resultSetMetaData.isSigned(columnIndex);
        notNull = resultSetMetaData.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls;
        autoIncrement = resultSetMetaData.isAutoIncrement(columnIndex);
        String actualTableName = resultSetMetaData.getTableName(columnIndex);
        if (null != actualTableName && logicSchema instanceof ShardingSchema) {
            Collection<String> logicTableNames = logicSchema.getShardingRule().getLogicTableNames(actualTableName);
            table = logicTableNames.isEmpty() ? "" : logicTableNames.iterator().next();
            TableMetaData tableMetaData = logicSchema.getMetaData().getTables().get(table);
            primaryKey = null != tableMetaData && tableMetaData.getColumns().get(resultSetMetaData.getColumnName(columnIndex).toLowerCase())
                    .isPrimaryKey();
        } else {
            table = actualTableName;
            primaryKey = false;
        }
    }
    
    /**
     * Set column label and column name.
     * 
     * @param logicColumnName logic column name
     */
    public void setColumnLabelAndName(final String logicColumnName) {
        if (columnLabel.equals(columnName)) {
            columnLabel = logicColumnName;
            columnName = logicColumnName;
        } else {
            columnName = logicColumnName;
        }
    }
}
