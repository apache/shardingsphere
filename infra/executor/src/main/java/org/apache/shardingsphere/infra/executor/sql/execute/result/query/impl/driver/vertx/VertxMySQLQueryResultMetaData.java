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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.vertx;

import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition.ColumnDefinitionFlags;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.List;

/**
 * Vert.x query result meta data for MySQL.
 */
@RequiredArgsConstructor
public final class VertxMySQLQueryResultMetaData implements QueryResultMetaData {
    
    private final List<ColumnDefinition> columnDefinitions;
    
    @Override
    public int getColumnCount() {
        return columnDefinitions.size();
    }
    
    @Override
    public String getTableName(final int columnIndex) {
        return columnDefinitions.get(columnIndex - 1).table();
    }
    
    @Override
    public String getColumnName(final int columnIndex) {
        return columnDefinitions.get(columnIndex - 1).orgName();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) {
        return columnDefinitions.get(columnIndex - 1).name();
    }
    
    @Override
    public int getColumnType(final int columnIndex) {
        return columnDefinitions.get(columnIndex - 1).jdbcType().getVendorTypeNumber();
    }
    
    @Override
    public String getColumnTypeName(final int columnIndex) {
        return columnDefinitions.get(columnIndex - 1).jdbcType().getName();
    }
    
    @Override
    public int getColumnLength(final int columnIndex) {
        return (int) columnDefinitions.get(columnIndex - 1).columnLength();
    }
    
    @Override
    public int getDecimals(final int columnIndex) {
        return columnDefinitions.get(columnIndex - 1).decimals();
    }
    
    @Override
    public boolean isSigned(final int columnIndex) {
        return false;
    }
    
    @Override
    public boolean isNotNull(final int columnIndex) {
        return (columnDefinitions.get(columnIndex - 1).flags() & ColumnDefinitionFlags.NOT_NULL_FLAG) == ColumnDefinitionFlags.NOT_NULL_FLAG;
    }
    
    @Override
    public boolean isAutoIncrement(final int columnIndex) {
        return (columnDefinitions.get(columnIndex - 1).flags() & ColumnDefinitionFlags.AUTO_INCREMENT_FLAG) == ColumnDefinitionFlags.AUTO_INCREMENT_FLAG;
    }
}
