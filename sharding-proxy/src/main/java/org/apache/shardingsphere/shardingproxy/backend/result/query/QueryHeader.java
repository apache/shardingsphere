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

package org.apache.shardingsphere.shardingproxy.backend.result.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Query header.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class QueryHeader {
    
    private final String schema;
    
    private final String table;
    
    private final String columnLabel;
    
    private final String columnName;
    
    private final int columnLength;
    
    private final Integer columnType;
    
    private final int decimals;
    
    public QueryHeader(final ResultSetMetaData resultSetMetaData, final LogicSchema logicSchema, final int columnIndex) throws SQLException {
        this.schema = logicSchema.getName();
        if (logicSchema instanceof ShardingSchema) {
            Collection<String> tableNames = ((ShardingSchema) logicSchema).getShardingRule().getLogicTableNames(resultSetMetaData.getTableName(columnIndex));
            this.table = tableNames.isEmpty() ? "" : tableNames.iterator().next();
        } else {
            this.table = resultSetMetaData.getTableName(columnIndex);
        }
        this.columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
        this.columnName = resultSetMetaData.getColumnName(columnIndex);
        this.columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        this.columnType = resultSetMetaData.getColumnType(columnIndex);
        this.decimals = resultSetMetaData.getScale(columnIndex);
    }
}
