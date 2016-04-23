/**
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

package com.dangdang.ddframe.rdb.sharding.router;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.router.fixture.OrderAttrShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.router.fixture.OrderShardingAlgorithm;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractBaseRouteSqlTest {
    
    @Getter(AccessLevel.PROTECTED)
    private ShardingRule shardingRule;
    
    @Before
    public void setRouteRuleContext() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule orderTableRule = new TableRule("order", Lists.newArrayList("order_0", "order_1"), dataSourceRule);
        TableRule orderItemTableRule = new TableRule("order_item", Lists.newArrayList("order_item_0", "order_item_1"), dataSourceRule);
        TableRule orderAttrTableRule = new TableRule("order_attr", Lists.newArrayList("ds_0.order_attr_a", "ds_1.order_attr_b"), dataSourceRule, 
                new TableShardingStrategy("order_id", new OrderAttrShardingAlgorithm()));
        shardingRule = new ShardingRule(dataSourceRule, Lists.newArrayList(orderTableRule, orderItemTableRule, orderAttrTableRule), 
                Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))), 
                new DatabaseShardingStrategy("order_id", new OrderShardingAlgorithm()), new TableShardingStrategy("order_id", new OrderShardingAlgorithm()));
    }
    
    protected void assertSingleTarget(final String originSql, final String targetDataSource, final String targetSQL) throws SQLParserException {
        assertSingleTarget(originSql, Collections.emptyList(), targetDataSource, targetSQL);
    }
    
    protected void assertSingleTarget(final String originSql, final List<Object> parameters, final String targetDataSource, final String targetSQL) throws SQLParserException {
        assertMultipleTargets(originSql, parameters, 1, Collections.singletonList(targetDataSource), Collections.singletonList(targetSQL));
    }
    
    protected void assertMultipleTargets(final String originSql, final int expectedSize, 
            final Collection<String> targetDataSources, final Collection<String> targetSQLs) throws SQLParserException {
        assertMultipleTargets(originSql, Collections.emptyList(), expectedSize, targetDataSources, targetSQLs);
    }
    
    protected void assertMultipleTargets(final String originSql, final List<Object> parameters, final int expectedSize, 
            final Collection<String> targetDataSources, final Collection<String> targetSQLs) throws SQLParserException {
        SQLRouteResult actual = new SQLRouteEngine(getShardingRule(), DatabaseType.MySQL).route(originSql, parameters);
        assertThat(actual.getExecutionUnits().size(), is(expectedSize));
        Set<String> actualDataSources = new HashSet<>(Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {
            
            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getDataSource();
            }
        }));
        assertThat(actualDataSources, hasItems(targetDataSources.toArray(new String[targetDataSources.size()])));
        Collection<String> actualSQLs = Collections2.transform(actual.getExecutionUnits(), new Function<SQLExecutionUnit, String>() {
            
            @Override
            public String apply(final SQLExecutionUnit input) {
                return input.getSql();
            }
        });
        assertThat(actualSQLs, hasItems(targetSQLs.toArray(new String[targetSQLs.size()])));
    }
}
