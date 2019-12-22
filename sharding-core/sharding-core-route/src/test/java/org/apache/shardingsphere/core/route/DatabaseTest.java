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

package org.apache.shardingsphere.core.route;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.core.route.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setRouteRuleContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        shardingRuleConfig.setDefaultDataSourceName("ds_0");
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        shardingRule = new ShardingRule(shardingRuleConfig, dataSourceMap.keySet());
    }
    
    @Test
    public void assertDatabaseAllRoutingSQL() {
        String originSQL = "select * from tesT";
        SQLParseEngine parseEngine = SQLParseEngineFactory.getSQLParseEngine("MySQL");
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, getMetaDataForAllRoutingSQL(), parseEngine).route(originSQL);
        assertThat(actual.getRoutingResult().getRoutingUnits().size(), is(1));
        Collection<String> actualDataSources = actual.getRoutingResult().getDataSourceNames();
        assertThat(actualDataSources.size(), is(1));
    }
    
    private ShardingSphereMetaData getMetaDataForAllRoutingSQL() {
        DataSourceMetas dataSourceMetas = mock(DataSourceMetas.class);
        when(dataSourceMetas.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        ColumnMetaData idColumnMetaData = new ColumnMetaData("id", "int", true);
        ColumnMetaData nameColumnMetaData = new ColumnMetaData("user_id", "int", false);
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.get("tesT")).thenReturn(new TableMetaData(Arrays.asList(idColumnMetaData, nameColumnMetaData), Arrays.asList("id", "user_id")));
        when(tableMetas.containsTable("tesT")).thenReturn(true);
        return new ShardingSphereMetaData(dataSourceMetas, tableMetas);
    }
    
    @Test
    public void assertDatabaseSelectSQLPagination() {
        String originSQL = "select user_id from tbl_pagination limit 0,5";
        SQLParseEngine parseEngine = SQLParseEngineFactory.getSQLParseEngine("MySQL");
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, getMetaDataForPagination(), parseEngine).route(originSQL);
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(0L));
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orNull(), is(5L));
        originSQL = "select user_id from tbl_pagination limit 5,5";
        actual = new StatementRoutingEngine(shardingRule, getMetaDataForPagination(), parseEngine).route(originSQL);
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(5L));
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orNull(), is(5L));
    }
    
    private ShardingSphereMetaData getMetaDataForPagination() {
        ColumnMetaData idColumnMetaData = new ColumnMetaData("id", "int", true);
        ColumnMetaData nameColumnMetaData = new ColumnMetaData("user_id", "int", false);
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.get("tbl_pagination")).thenReturn(new TableMetaData(Arrays.asList(idColumnMetaData, nameColumnMetaData), Arrays.asList("id", "user_id")));
        when(tableMetas.containsTable("tbl_pagination")).thenReturn(true);
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getTables()).thenReturn(tableMetas);
        DataSourceMetas dataSourceMetas = mock(DataSourceMetas.class);
        when(dataSourceMetas.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        when(result.getDataSources()).thenReturn(dataSourceMetas);
        return result;
    }
    
    @Test
    public void assertDatabasePrepareSelectSQLPagination() {
        String shardingPrefix = "user_db";
        String shardingTable = "t_user";
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put(shardingPrefix + "1", null);
        dataSourceMap.put(shardingPrefix + "2", null);
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(shardingTable, shardingPrefix + "${1..2}." + shardingTable);
        tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("city_id", shardingPrefix + "${city_id % 2 + 1}"));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule rule = new ShardingRule(shardingRuleConfig, dataSourceMap.keySet());
        String originSQL = "select city_id from t_user where city_id in (?,?) limit 5,10";
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getTables()).thenReturn(mock(TableMetas.class));
        SQLParseEngine parseEngine = SQLParseEngineFactory.getSQLParseEngine("MySQL");
        SQLRouteResult actual = new PreparedStatementRoutingEngine(originSQL, rule, metaData, parseEngine).route(Lists.<Object>newArrayList(13, 173));
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(5L));
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orNull(), is(10L));
        assertThat(actual.getRoutingResult().getRoutingUnits().size(), is(1));
        originSQL = "select city_id from t_user where city_id in (?,?) limit 5,10";
        actual = new PreparedStatementRoutingEngine(originSQL, rule, metaData, parseEngine).route(Lists.<Object>newArrayList(89, 84));
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(5L));
        assertThat(((SelectSQLStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orNull(), is(10L));
    }
}
