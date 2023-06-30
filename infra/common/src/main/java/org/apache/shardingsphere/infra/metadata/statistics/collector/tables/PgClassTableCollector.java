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
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Table pg_class data collector.
 */
public final class PgClassTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String PG_CLASS = "pg_class";
    
    private static final String COLUMN_NAMES = "relname, relnamespace, relkind, reloptions";
    
    private static final String SELECT_SQL = "SELECT " + COLUMN_NAMES + " FROM pg_catalog.pg_class WHERE relkind IN ('r','v','m','S','L','f','e','o','') "
            + "AND relname NOT LIKE 'matviewmap\\_%' AND relname NOT LIKE 'mlog\\_%' AND pg_catalog.pg_table_is_visible(oid);";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        Collection<ShardingSphereRowData> rows = ShardingSphereTableDataCollectorUtils.collectRowData(shardingSphereDatabases.get(databaseName),
                SELECT_SQL, table, Arrays.stream(COLUMN_NAMES.split(",")).map(String::trim).collect(Collectors.toList()));
        Collection<ShardingSphereRowData> rowData = decorateTableName(rows, table, shardingSphereDatabases.get(databaseName).getRuleMetaData().getRules());
        ShardingSphereTableData result = new ShardingSphereTableData(PG_CLASS);
        result.getRows().addAll(rowData);
        return Optional.of(result);
    }
    
    private Collection<ShardingSphereRowData> decorateTableName(final Collection<ShardingSphereRowData> rows, final ShardingSphereTable table, final Collection<ShardingSphereRule> rules) {
        Collection<DataNodeContainedRule> dataNodeContainedRules = rules.stream().filter(DataNodeContainedRule.class::isInstance).map(DataNodeContainedRule.class::cast).collect(Collectors.toList());
        if (dataNodeContainedRules.isEmpty()) {
            return rows;
        }
        int tableNameIndex = table.getColumnNames().indexOf("relname");
        Collection<ShardingSphereRowData> result = new LinkedList<>();
        for (ShardingSphereRowData each : rows) {
            String tableName = (String) each.getRows().get(tableNameIndex);
            String logicTableName = decorateTableName(dataNodeContainedRules, tableName);
            List<Object> decoratedRow = new ArrayList<>(each.getRows());
            decoratedRow.set(tableNameIndex, logicTableName);
            result.add(new ShardingSphereRowData(decoratedRow));
        }
        return result;
    }
    
    private String decorateTableName(final Collection<DataNodeContainedRule> dataNodeContainedRules, final String actualTableName) {
        for (DataNodeContainedRule each : dataNodeContainedRules) {
            if (each.findLogicTableByActualTable(actualTableName).isPresent()) {
                return each.findLogicTableByActualTable(actualTableName).get();
            }
        }
        return actualTableName;
    }
    
    @Override
    public String getType() {
        return PG_CLASS;
    }
}
