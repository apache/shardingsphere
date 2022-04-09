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

import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;

import java.sql.SQLException;
import java.util.Optional;

/**
 * QueryHeaderBuilder for MySQL.
 */
public final class MySQLQueryHeaderBuilder implements QueryHeaderBuilder {
    
    @SneakyThrows(ConcurrentException.class)
    @Override
    public QueryHeader build(final QueryResultMetaData queryResultMetaData, final ShardingSphereMetaData metaData, final String columnName, final String columnLabel,
                             final int columnIndex, final LazyInitializer<DataNodeContainedRule> dataNodeContainedRule) throws SQLException {
        String schemaName = null == metaData ? "" : metaData.getName();
        String actualTableName = queryResultMetaData.getTableName(columnIndex);
        String tableName;
        boolean primaryKey;
        if (null != actualTableName && null != dataNodeContainedRule.get()) {
            tableName = dataNodeContainedRule.get().findLogicTableByActualTable(actualTableName).orElse("");
            TableMetaData tableMetaData = metaData.getDefaultSchema().get(tableName);
            primaryKey = null != tableMetaData && Optional.ofNullable(tableMetaData.getColumns().get(columnName.toLowerCase())).map(ColumnMetaData::isPrimaryKey).orElse(false);
        } else {
            tableName = actualTableName;
            primaryKey = false;
        }
        int columnType = queryResultMetaData.getColumnType(columnIndex);
        String columnTypeName = queryResultMetaData.getColumnTypeName(columnIndex);
        int columnLength = queryResultMetaData.getColumnLength(columnIndex);
        int decimals = queryResultMetaData.getDecimals(columnIndex);
        boolean signed = queryResultMetaData.isSigned(columnIndex);
        boolean notNull = queryResultMetaData.isNotNull(columnIndex);
        boolean autoIncrement = queryResultMetaData.isAutoIncrement(columnIndex);
        return new QueryHeader(schemaName, tableName, columnLabel, columnName, columnType, columnTypeName, columnLength, decimals, signed, primaryKey, notNull, autoIncrement);
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
