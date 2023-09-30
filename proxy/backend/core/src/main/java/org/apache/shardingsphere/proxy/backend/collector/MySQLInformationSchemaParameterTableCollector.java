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

package org.apache.shardingsphere.proxy.backend.collector;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereTableDataCollectorUtils;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * MySQLInformationSchemaParameterTableCollector.
 */
public final class MySQLInformationSchemaParameterTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String PARAMETER_TABLE_NAME = "PARAMETERS";
    
    private static final String COLLECT_SQL = "select * from information_schema.PARAMETERS";

    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName,final ShardingSphereTable table,final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        Optional<String> databaseWithDatasource = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(MySQLInformationSchemaParameterTableCollector::hasDataSource).findFirst();
        if (databaseWithDatasource.isPresent()) {
            Collection<ShardingSphereRowData> rows = ShardingSphereTableDataCollectorUtils.collectRowData(shardingSphereDatabases.get(databaseWithDatasource.get()),
                    table, table.getColumnNames(), COLLECT_SQL);
            ShardingSphereTableData result = new ShardingSphereTableData(PARAMETER_TABLE_NAME);
            result.getRows().addAll(rows);
            return Optional.of(result);
        }
        return Optional.empty();
    }
    
    private static Boolean hasDataSource(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName).containsDataSource();
    }
    
    @Override
    public Object getType() {
        return PARAMETER_TABLE_NAME;
    }
    
}
