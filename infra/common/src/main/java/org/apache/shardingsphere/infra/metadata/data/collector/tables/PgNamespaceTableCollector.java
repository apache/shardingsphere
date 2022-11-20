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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Table pg_namespace data collector.
 */
public final class PgNamespaceTableCollector implements ShardingSphereDataCollector {
    
    private static final String PG_NAMESPACE = "pg_namespace";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        Set<ShardingSphereRowData> rows = new LinkedHashSet<>();
        for (DataSource each : shardingSphereDatabases.get(databaseName).getResourceMetaData().getDataSources().values()) {
            try (
                    Connection connection = each.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT oid, nspname, nspowner, nspacl FROM pg_catalog.pg_namespace")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    List<Object> row = new ArrayList<>(metaData.getColumnCount());
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        row.add(convertIfNecessary(resultSet.getObject(i), metaData.getColumnType(i)));
                    }
                    ShardingSphereRowData rowData = new ShardingSphereRowData(row);
                    rows.add(rowData);
                }
            }
        }
        ShardingSphereTableData result = new ShardingSphereTableData(PG_NAMESPACE, new ArrayList<>(table.getColumns().values()));
        result.getRows().addAll(rows);
        return Optional.of(result);
    }
    
    // TODO extract to util
    private Object convertIfNecessary(final Object data, final int dataType) {
        if (Types.ARRAY == dataType) {
            return null == data ? null : data.toString();
        }
        if (Types.BIGINT == dataType) {
            return null == data ? null : Long.valueOf(data.toString());
        }
        return data;
    }
    
    @Override
    public String getType() {
        return PG_NAMESPACE;
    }
}
