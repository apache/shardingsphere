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

package org.apache.shardingsphere.infra.metadata.statistics.collector.tables;

import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereTableDataCollectorUtils;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Table pg_namespace data collector.
 */
public final class PgNamespaceTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String PG_NAMESPACE = "pg_namespace";
    
    private static final String COLUMN_NAMES = "oid, nspname, nspowner, nspacl";
    
    private static final String SELECT_SQL = "SELECT " + COLUMN_NAMES + " FROM pg_catalog.pg_namespace";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        Collection<ShardingSphereRowData> rows = ShardingSphereTableDataCollectorUtils.collectRowData(shardingSphereDatabases.get(databaseName),
                SELECT_SQL, table, Arrays.stream(COLUMN_NAMES.split(",")).map(String::trim).collect(Collectors.toList()));
        ShardingSphereTableData result = new ShardingSphereTableData(PG_NAMESPACE);
        result.getRows().addAll(rows);
        return Optional.of(result);
    }
    
    @Override
    public String getType() {
        return PG_NAMESPACE;
    }
}
