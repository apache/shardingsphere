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

package org.apache.shardingsphere.shardingscaling.postgresql;

import org.apache.shardingsphere.shardingscaling.core.metadata.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.util.DbMetaDataUtil;
import org.postgresql.jdbc.PgConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL metadata util.
 *
 * @author avalon566
 */
public class PostgreSQLMetadataUtil extends DbMetaDataUtil {
    
    public PostgreSQLMetadataUtil(final DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    protected final List<ColumnMetaData> getColumnNamesInternal(final String tableName) {
        try {
            try (Connection connection = getDataSource().getConnection()) {
                List<ColumnMetaData> result = new ArrayList<>();
                String sql = String.format("SELECT a.attname,a.atttypid,t.typtype\n"
                        + "FROM pg_catalog.pg_namespace n\n"
                        + "JOIN pg_catalog.pg_class c ON (c.relnamespace = n.oid)\n"
                        + "JOIN pg_catalog.pg_attribute a ON (a.attrelid=c.oid)\n"
                        + "JOIN pg_catalog.pg_type t ON (a.atttypid = t.oid)\n"
                        + "WHERE c.relkind in ('r','p','v','f','m') and a.attnum > 0 AND NOT a.attisdropped  AND n.nspname LIKE E'public' AND c.relname LIKE E't1';",
                        connection.getSchema(), tableName);
                ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableName, "%");
                while (resultSet.next()) {
                    ColumnMetaData columnMetaData = new ColumnMetaData();
                    columnMetaData.setColumnName(resultSet.getString(1));
                    int typeOid = resultSet.getInt(2);
                    String typtype = resultSet.getString(3);
                    columnMetaData.setColumnType(getSqlType(connection.unwrap(PgConnection.class), typtype, typeOid));
                    columnMetaData.setColumnTypeName(getPgType(connection.unwrap(PgConnection.class), typeOid));
                    result.add(columnMetaData);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }
    
    private int getSqlType(final PgConnection pgConnection, final String typtype, final int typeOid) throws SQLException {
        if ("c".equals(typtype)) {
            return 2002;
        }
        if ("d".equals(typtype)) {
            return 2001;
        }
        if ("e".equals(typtype)) {
            return 12;
        }
        return pgConnection.getTypeInfo().getSQLType(typeOid);
    }
    
    private String getPgType(final PgConnection pgConnection, final int typeOid) throws SQLException {
        return pgConnection.getTypeInfo().getPGType(typeOid);
    }
}
