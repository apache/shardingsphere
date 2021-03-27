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

package org.apache.shardingsphere.infra.executor.exec.meta;

import org.apache.calcite.rel.core.JoinRelType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public final class JoinColumnMetaData implements QueryResultMetaData {
    
    private final QueryResultMetaData left;
    
    private final QueryResultMetaData right;
    
    private final JoinRelType joinRelType;
    
    public JoinColumnMetaData(final QueryResultMetaData left, final QueryResultMetaData right, final JoinRelType joinRelType) {
        this.left = left;
        this.right = right;
        this.joinRelType = joinRelType;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return left.getColumnCount() + right.getColumnCount();
    }
    
    @Override
    public String getTableName(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getTableName(entry.getKey());
    }
    
    @Override
    public String getColumnName(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnName(entry.getKey());
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnLabel(entry.getKey());
    }
    
    @Override
    public int getColumnType(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnType(entry.getKey());
    }
    
    @Override
    public String getColumnTypeName(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnTypeName(entry.getKey());
    }
    
    @Override
    public int getColumnLength(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnLength(entry.getKey());
    }
    
    @Override
    public int getDecimals(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getDecimals(entry.getKey());
    }
    
    @Override
    public boolean isSigned(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().isSigned(entry.getKey());
    }
    
    @Override
    public boolean isNotNull(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().isNotNull(entry.getKey());
    }
    
    @Override
    public boolean isAutoIncrement(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().isAutoIncrement(entry.getKey());
    }
    
    private Map.Entry<Integer, QueryResultMetaData> getColumnMeta(final int columnIndex) throws SQLException {
        if (columnIndex <= left.getColumnCount()) {
            return new SimpleEntry<>(columnIndex, left);
        }
        return new SimpleEntry<>(columnIndex - left.getColumnCount(), right);
    }
}
