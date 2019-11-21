/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import io.shardingsphere.api.HintManager;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.fixture.OrderDatabaseHintShardingAlgorithm;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class DatabaseTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setRouteRuleContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new OrderDatabaseHintShardingAlgorithm()));
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
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, false).route(originSQL);
        assertThat(actual.getRouteUnits().size(), is(1));
        Set<String> actualDataSources = new HashSet<>(Collections2.transform(actual.getRouteUnits(), new Function<RouteUnit, String>() {
        
            @Override
            public String apply(final RouteUnit input) {
                return input.getDataSourceName();
            }
        }));
        assertThat(actualDataSources.size(), is(1));
        assertThat(actualDataSources, hasItems("ds_0"));
        Collection<String> actualSQLs = Collections2.transform(actual.getRouteUnits(), new Function<RouteUnit, String>() {
        
            @Override
            public String apply(final RouteUnit input) {
                return input.getSqlUnit().getSql();
            }
        });
        assertThat(originSQL, is(actualSQLs.iterator().next()));
    }
    
    @Test
    public void assertDatabaseSelectSQLPagination() {
        String originSQL = "select user_id from tbl_pagination limit 0,5";
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, false).route(originSQL);
        SelectStatement stmt = (SelectStatement) actual.getSqlStatement();
        assertThat(stmt.getLimit().getOffsetValue(), is(0));
        assertThat(stmt.getLimit().getRowCountValue(), is(5));
        originSQL = "select user_id from tbl_pagination limit 5,5";
        actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, false).route(originSQL);
        stmt = (SelectStatement) actual.getSqlStatement();
        assertThat(stmt.getLimit().getOffsetValue(), is(5));
        assertThat(stmt.getLimit().getRowCountValue(), is(5));
    }
    
    @Test
    public void assertDatabasePrepareSelectSQLPagination() {
        String shardingPrefix = "user_db";
        String shardingTable = "user";
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put(shardingPrefix + "1", null);
        dataSourceMap.put(shardingPrefix + "2", null);
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable(shardingTable);
        tableRuleConfig.setActualDataNodes(shardingPrefix + "${1..2}." + shardingTable);
        tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("city_id", shardingPrefix + "${city_id % 2 + 1}"));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule rule = new ShardingRule(shardingRuleConfig, dataSourceMap.keySet());
        String originSQL = "select city_id from user where city_id in (?,?) limit 5,10";
        SQLRouteResult actual = new PreparedStatementRoutingEngine(originSQL, rule, mock(ShardingMetaData.class), DatabaseType.MySQL, false).route(Lists.<Object>newArrayList(13, 173));
        SelectStatement selectStatement = (SelectStatement) actual.getSqlStatement();
        assertThat(selectStatement.getLimit().getOffsetValue(), is(5));
        assertThat(selectStatement.getLimit().getRowCountValue(), is(10));
        assertThat(actual.getRouteUnits().size(), is(1));
        originSQL = "select city_id from user where city_id in (?,?) limit 5,10";
        actual = new PreparedStatementRoutingEngine(originSQL, rule, mock(ShardingMetaData.class), DatabaseType.MySQL, false).route(Lists.<Object>newArrayList(89, 84));
        selectStatement = (SelectStatement) actual.getSqlStatement();
        assertThat(selectStatement.getLimit().getOffsetValue(), is(5));
        assertThat(selectStatement.getLimit().getRowCountValue(), is(10));
        assertThat(actual.getRouteUnits().size(), is(2));
    }
    
    private void assertTarget(final String originSql, final String targetDataSource) {
        SQLRouteResult actual = new StatementRoutingEngine(shardingRule, mock(ShardingMetaData.class), DatabaseType.MySQL, false).route(originSql);
        assertThat(actual.getRouteUnits().size(), is(1));
        Set<String> actualDataSources = new HashSet<>(Collections2.transform(actual.getRouteUnits(), new Function<RouteUnit, String>() {
            
            @Override
            public String apply(final RouteUnit input) {
                return input.getDataSourceName();
            }
        }));
        assertThat(actualDataSources.size(), is(1));
        assertThat(actualDataSources, hasItems(targetDataSource));
        Collection<String> actualSQLs = Collections2.transform(actual.getRouteUnits(), new Function<RouteUnit, String>() {
            
            @Override
            public String apply(final RouteUnit input) {
                return input.getSqlUnit().getSql();
            }
        });
        assertThat(originSql, is(actualSQLs.iterator().next()));
    }
}
