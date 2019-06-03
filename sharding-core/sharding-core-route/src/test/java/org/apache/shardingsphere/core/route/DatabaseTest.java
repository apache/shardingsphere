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
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setRouteRuleContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        shardingRule = new ShardingRule(shardingRuleConfig, dataSourceMap.keySet());
    }
    
    @Test
    public void assertHintSQL() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTarget("select * from tesT", "ds_1");
            assertTarget("insert into test values (1,2)", "ds_1");
            assertTarget("update test set a = 1", "ds_1");
            assertTarget("delete from test where id = 2", "ds_1");
            hintManager.setDatabaseShardingValue(2);
            assertTarget("select * from tesT", "ds_0");
            hintManager.close();
        }
    }
    
    @Test
    public void assertDatabaseAllRoutingSQL() {
        String originSQL = "select * from tesT";
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache()).route(originSQL);
        assertThat(actual.getRoutingResult().getRoutingUnits().size(), is(1));
        Collection<String> actualDataSources = actual.getRoutingResult().getDataSourceNames();
        assertThat(actualDataSources.size(), is(1));
    }
    
    @Test
    public void assertDatabaseSelectSQLPagination() {
        String originSQL = "select user_id from tbl_pagination limit 0,5";
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache()).route(originSQL);
        SelectStatement selectStatement = (SelectStatement) actual.getSqlStatement();
        assertTrue(selectStatement.getLimit().getOffset().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getOffset().get()).getValue(), is(0));
        assertTrue(selectStatement.getLimit().getRowCount().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getRowCount().get()).getValue(), is(5));
        originSQL = "select user_id from tbl_pagination limit 5,5";
        actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache()).route(originSQL);
        selectStatement = (SelectStatement) actual.getSqlStatement();
        assertTrue(selectStatement.getLimit().getOffset().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getOffset().get()).getValue(), is(5));
        assertTrue(selectStatement.getLimit().getRowCount().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getRowCount().get()).getValue(), is(5));
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
        ShardingMetaData shardingMetaData = mock(ShardingMetaData.class);
        when(shardingMetaData.getTable()).thenReturn(mock(ShardingTableMetaData.class));
        SQLRouteResult actual = new PreparedStatementRoutingEngine(originSQL, rule, shardingMetaData, DatabaseType.MySQL, new ParsingResultCache()).route(Lists.<Object>newArrayList(13, 173));
        SelectStatement selectStatement = (SelectStatement) actual.getSqlStatement();
        assertTrue(selectStatement.getLimit().getOffset().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getOffset().get()).getValue(), is(5));
        assertTrue(selectStatement.getLimit().getRowCount().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getRowCount().get()).getValue(), is(10));
        assertThat(actual.getRoutingResult().getRoutingUnits().size(), is(1));
        originSQL = "select city_id from t_user where city_id in (?,?) limit 5,10";
        actual = new PreparedStatementRoutingEngine(originSQL, rule, shardingMetaData, DatabaseType.MySQL, new ParsingResultCache()).route(Lists.<Object>newArrayList(89, 84));
        selectStatement = (SelectStatement) actual.getSqlStatement();
        assertTrue(selectStatement.getLimit().getOffset().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getOffset().get()).getValue(), is(5));
        assertTrue(selectStatement.getLimit().getRowCount().isPresent());
        assertThat(((NumberLiteralLimitValueSegment) selectStatement.getLimit().getRowCount().get()).getValue(), is(10));
        assertThat(actual.getRoutingResult().getRoutingUnits().size(), is(2));
    }
    
    private void assertTarget(final String originalSQL, final String targetDataSource) {
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache()).route(originalSQL);
        assertThat(actual.getRoutingResult().getRoutingUnits().size(), is(1));
        assertThat(actual.getRoutingResult().getDataSourceNames(), hasItems(targetDataSource));
    }
}
