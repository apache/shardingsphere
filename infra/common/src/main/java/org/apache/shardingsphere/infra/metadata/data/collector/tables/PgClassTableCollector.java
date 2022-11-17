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

package org.apache.shardingsphere.infra.metadata.data.collector.tables;

import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Table pg_class data collector.
 */
public final class PgClassTableCollector implements ShardingSphereDataCollector {
    
    private static final String PG_CLASS = "pg_class";
    
    private static final String SELECT_SQL = "SELECT oid, relname, relnamespace, reltype, reloftype, relowner, relam, relfilenode, reltablespace, relpages, reltuples, relallvisible, reltoastrelid,"
            + "relhasindex, relisshared, relpersistence, relkind, relnatts, relchecks, relhasrules, relhastriggers, relhassubclass, relrowsecurity, relforcerowsecurity, relispopulated, relreplident,"
            + "relispartition, relrewrite, relfrozenxid, relminmxid, relacl, reloptions, relpartbound FROM pg_catalog.pg_class "
            + "WHERE relkind IN ('r','v','m','S','L','f','e','o','') AND relname NOT LIKE 'matviewmap\\_%' AND relname NOT LIKE 'mlog\\_%' "
            + "AND pg_catalog.pg_table_is_visible(oid);";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(PG_CLASS, new ArrayList<>(table.getColumns().values()));
        for (DataSource each : shardingSphereDatabases.get(databaseName).getResourceMetaData().getDataSources().values()) {
            try (
                    Connection connection = each.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(SELECT_SQL)) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    List<Object> rows = new ArrayList<>(metaData.getColumnCount());
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        rows.add(convertIfNecessary(resultSet.getObject(i), metaData.getColumnType(i)));
                    }
                    ShardingSphereRowData rowData = new ShardingSphereRowData(rows);
                    result.getRows().add(rowData);
                }
            }
        }
        return Optional.of(result);
    }
    
    // TODO extract to util
    private Object convertIfNecessary(final Object data, final int dataType) {
        if (Types.ARRAY == dataType || Types.OTHER == dataType) {
            return null == data ? null : data.toString();
        }
        if (Types.BIGINT == dataType) {
            return null == data ? null : new BigInteger(data.toString());
        }
        return data;
    }
    
    @Override
    public String getType() {
        return PG_CLASS;
    }
}
