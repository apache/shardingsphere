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

package org.apache.shardingsphere.proxy.backend.postgresql.response.header.query;

import org.apache.shardingsphere.database.protocol.postgresql.type.PostgreSQLColumnTypeOIDLoader;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Query header builder for PostgreSQL.
 */
public final class PostgreSQLQueryHeaderBuilder implements QueryHeaderBuilder {
    
    public static final String TYPE_OID = "typeOID";
    
    private static final int UNUSED_INT_FIELD = 0;
    
    private static final String UNUSED_STRING_FIELD = "";
    
    private static final boolean UNUSED_BOOLEAN_FIELD = false;
    
    @Override
    public QueryHeader build(final ShardingSphereResultSetMetaData resultSetMetaData, final ShardingSphereDatabase database, final String columnName, final String columnLabel,
                             final int columnIndex) throws SQLException {
        return createQueryHeader(columnLabel, resultSetMetaData.getColumnType(columnIndex), resultSetMetaData.getColumnTypeName(columnIndex),
                resultSetMetaData.getColumnDisplaySize(columnIndex), Collections.emptyMap());
    }
    
    @Override
    public QueryHeader build(final ShardingSphereResultSetMetaData resultSetMetaData, final ResultSet resultSet, final ShardingSphereDatabase database, final String columnName,
                             final String columnLabel, final int columnIndex) throws SQLException {
        int columnType = resultSetMetaData.getColumnType(columnIndex);
        String columnTypeName = resultSetMetaData.getColumnTypeName(columnIndex);
        return createQueryHeader(columnLabel, columnType, columnTypeName, resultSetMetaData.getColumnDisplaySize(columnIndex),
                getProtocolAttributes(resultSet, columnType, columnTypeName));
    }
    
    private QueryHeader createQueryHeader(final String columnLabel, final int columnType, final String columnTypeName, final int columnLength,
                                          final Map<String, Object> protocolAttributes) {
        return new QueryHeader(UNUSED_STRING_FIELD, UNUSED_STRING_FIELD, columnLabel, UNUSED_STRING_FIELD, columnType, columnTypeName, columnLength,
                UNUSED_INT_FIELD, UNUSED_BOOLEAN_FIELD, UNUSED_BOOLEAN_FIELD, UNUSED_BOOLEAN_FIELD, UNUSED_BOOLEAN_FIELD, protocolAttributes);
    }
    
    private Map<String, Object> getProtocolAttributes(final ResultSet resultSet, final int columnType, final String columnTypeName) throws SQLException {
        if (Types.STRUCT != columnType) {
            return Collections.emptyMap();
        }
        Optional<Integer> typeOID = PostgreSQLColumnTypeOIDLoader.findTypeOID(resultSet.getStatement().getConnection(), columnTypeName);
        return typeOID.<Map<String, Object>>map(integer -> Collections.singletonMap(TYPE_OID, integer)).orElse(Collections.emptyMap());
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
