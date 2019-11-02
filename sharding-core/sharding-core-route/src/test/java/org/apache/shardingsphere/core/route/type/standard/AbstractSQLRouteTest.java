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

package org.apache.shardingsphere.core.route.type.standard;

import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.core.route.PreparedStatementRoutingEngine;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractSQLRouteTest extends AbstractRoutingEngineTest {
    
    protected final SQLRouteResult assertRoute(final String sql, final List<Object> parameters) {
        ShardingRule shardingRule = createAllShardingRule();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(buildDataSourceMetas(), buildTableMetas());
        SQLParseEngine parseEngine = new SQLParseEngine(DatabaseTypes.getActualDatabaseType("MySQL"));
        PreparedStatementRoutingEngine engine = new PreparedStatementRoutingEngine(sql, shardingRule, metaData, parseEngine);
        SQLRouteResult result = engine.route(parameters);
        assertThat(result.getRoutingResult().getRoutingUnits().size(), is(1));
        return result;
    }
    
    private DataSourceMetas buildDataSourceMetas() {
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<>();
        shardingDataSourceURLs.put("main", "jdbc:mysql://127.0.0.1:3306/actual_db");
        shardingDataSourceURLs.put("ds_0", "jdbc:mysql://127.0.0.1:3306/actual_db");
        shardingDataSourceURLs.put("ds_1", "jdbc:mysql://127.0.0.1:3306/actual_db");
        return new DataSourceMetas(shardingDataSourceURLs, DatabaseTypes.getActualDatabaseType("MySQL"));
    }
    
    private TableMetas buildTableMetas() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true, true, true), 
                new ColumnMetaData("user_id", "int", false, false, false), 
                new ColumnMetaData("status", "int", false, false, false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("item_id", "int", true, true, true), 
                new ColumnMetaData("order_id", "int", false, false, false),
                new ColumnMetaData("user_id", "int", false, false, false), 
                new ColumnMetaData("status", "varchar", false, false, false), 
                new ColumnMetaData("c_date", "timestamp", false, false, false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_other", new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true, true, true)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_category", new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true, true, true)), Collections.<String>emptySet()));
        return new TableMetas(tableMetaDataMap);
    }
}
