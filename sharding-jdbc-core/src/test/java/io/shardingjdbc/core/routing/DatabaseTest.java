/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing;

import io.shardingjdbc.core.api.HintManager;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.fixture.OrderDatabaseHintShardingAlgorithm;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setRouteRuleContext() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(OrderDatabaseHintShardingAlgorithm.class.getName()));
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        shardingRule = shardingRuleConfig.build(dataSourceMap);
    }
    
    @Test
    public void assertSQL() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTarget("select * from tesT", "ds_1");
            assertTarget("insert into test values (1,2)", "ds_1");
            assertTarget("update test set a = 1", "ds_1");
            assertTarget("delete from test where id = 2", "ds_1");
            hintManager.setDatabaseShardingValue(2);
            assertTarget("select * from tesT", "ds_0");
        }
    }
    
    private void assertTarget(final String originSql, final String targetDataSource) {
        ShardingContext shardingContext = new ShardingContext(shardingRule, DatabaseType.MySQL, null, false);
        SQLRouteResult actual = new StatementRoutingEngine(shardingContext).route(originSql);
        assertThat(actual.getExecutionUnits().size(), is(1));
        Set<String> actualDataSources = new HashSet<>(Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {
            
            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getDataSource();
            }
        }));
        assertThat(actualDataSources.size(), is(1));
        assertThat(actualDataSources, hasItems(targetDataSource));
        Collection<String> actualSQLs = Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {
            
            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getSql();
            }
        });
        assertThat(originSql, is(actualSQLs.iterator().next()));
    }
}
