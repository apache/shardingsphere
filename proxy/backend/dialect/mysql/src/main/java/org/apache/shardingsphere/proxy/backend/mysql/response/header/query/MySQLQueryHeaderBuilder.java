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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Query header builder for MySQL.
 */
public final class MySQLQueryHeaderBuilder implements QueryHeaderBuilder {
    
    public static final String MYSQL_DATETIME = "mysqlDatetime";
    
    @Override
    @HighFrequencyInvocation
    public QueryHeader build(final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database, final String columnName, final String columnLabel,
                             final int columnIndex) throws SQLException {
        String schemaName = null == database ? "" : database.getName();
        String tableName = resultSetMetaData.getTableName(columnIndex);
        boolean primaryKey = isPrimaryKey(database, schemaName, tableName, columnName, resultSetMetaData, columnIndex);
        int columnType = resultSetMetaData.getColumnType(columnIndex);
        String columnTypeName = resultSetMetaData.getColumnTypeName(columnIndex);
        int columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
        int decimals = getDecimals(resultSetMetaData.getScale(columnIndex), columnType, columnLength);
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
    
    private int getDecimals(final int decimals, final int columnType, final int columnLength) {
        if (Types.TIME != columnType || 0 != decimals) {
            return decimals;
        }
        int result = columnLength - 11;
        return result >= 1 && result <= 6 ? result : decimals;
    }
    
    @Override
    @HighFrequencyInvocation
    public void appendProtocolAttributes(final QueryHeader queryHeader, final ResultSet resultSet) throws SQLException {
        appendProtocolAttributes(Collections.singleton(queryHeader), resultSet);
    }
    
    @Override
    @HighFrequencyInvocation
    public void appendProtocolAttributes(final Collection<QueryHeader> queryHeaders, final ResultSet resultSet) throws SQLException {
        if (ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class).isSqlFederationEnabled()) {
            return;
        }
        DatabaseType storageType = null;
        for (QueryHeader each : queryHeaders) {
            if (Types.TIMESTAMP != each.getColumnType() || !"DATETIME".equalsIgnoreCase(each.getColumnTypeName())) {
                continue;
            }
            if (null == storageType) {
                storageType = DatabaseTypeFactory.get(resultSet.getStatement().getConnection().getMetaData());
            }
            if ("MySQL".equals(storageType.getType())) {
                each.getProtocolAttributes().put(MYSQL_DATETIME, true);
            }
        }
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
