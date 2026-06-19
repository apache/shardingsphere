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

package org.apache.shardingsphere.proxy.backend.mysql.response.header.query;

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Query header builder for MySQL.
 */
public final class MySQLQueryHeaderBuilder implements QueryHeaderBuilder {
    
    @Override
    public QueryHeader build(final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database, final String columnName, final String columnLabel,
                             final int columnIndex) throws SQLException {
        String schemaName = null == database ? "" : database.getName();
        String tableName = resultSetMetaData.getTableName(columnIndex);
        boolean primaryKey = isPrimaryKey(database, schemaName, tableName, columnName, resultSetMetaData, columnIndex);
        int columnType = resultSetMetaData.getColumnType(columnIndex);
        String columnTypeName = resultSetMetaData.getColumnTypeName(columnIndex);
        int columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        int decimals = resultSetMetaData.getScale(columnIndex);
        boolean signed = resultSetMetaData.isSigned(columnIndex);
        boolean notNull = ResultSetMetaData.columnNoNulls == resultSetMetaData.isNullable(columnIndex);
        boolean autoIncrement = resultSetMetaData.isAutoIncrement(columnIndex);
        return new QueryHeader(schemaName, tableName, columnLabel, columnName, columnType, columnTypeName, columnLength, decimals, signed, primaryKey, notNull, autoIncrement);
    }
    
    private boolean isPrimaryKey(final ShardingSphereDatabase database, final String schemaName, final String tableName, final String columnName,
                                 final ShardingSphereResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        if (null == database || null == tableName) {
            return false;
        }
        ShardingSphereSchema schema = database.getSchema(schemaName);
        return isPrimaryKey(schema, tableName, columnName) || isPrimaryKey(schema, tableName, resultSetMetaData.getColumnName(columnIndex));
    }
    
    private boolean isPrimaryKey(final ShardingSphereSchema schema, final String tableName, final String columnName) {
        return null != schema && null != columnName
                && Optional.ofNullable(schema.getTable(tableName)).map(optional -> optional.getColumn(columnName)).map(ShardingSphereColumn::isPrimaryKey).orElse(false);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    // TODO to be confirmed, QueryHeaderBuilder should not has default value, just throw unsupported exception if database type missing
    @Override
    public boolean isDefault() {
        return true;
    }
}
