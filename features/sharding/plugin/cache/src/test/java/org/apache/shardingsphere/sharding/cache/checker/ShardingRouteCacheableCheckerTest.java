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

package org.apache.shardingsphere.sharding.cache.checker;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheOptions;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.cache.rule.ShardingCacheRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class ShardingRouteCacheableCheckerTest {
    
    private static final String DATABASE_NAME = "sharding_db";
    
    private static final String SCHEMA_NAME = "public";
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final boolean expectedProbablyCacheable;
    
    private final List<Integer> expectedShardingConditionParameterMarkerIndexes;
    
    @Parameters(name = "probably cacheable: {2}, SQL: {0}")
    public static Iterable<Object[]> parameters() {
        Collection<Object[]> probablyCacheableCases = Arrays.asList(
                new Object[]{"insert into t_broadcast_table (broadcast_table_id, broadcast_table_col1) values (?, ?)", Arrays.asList(1, "foo"), true, Collections.emptyList()},
                new Object[]{"insert into t_warehouse (id) values (?)", Collections.singletonList(1), true, Collections.singletonList(0)},
                new Object[]{"select * from t_warehouse where id = ?", Collections.singletonList(1), true, Collections.singletonList(0)},
                new Object[]{"select * from t_warehouse where id in (?, ?, ?)", Arrays.asList(1, 2, 3), true, Arrays.asList(0, 1, 2)},
                new Object[]{"select * from t_warehouse where id between ? and ?", Arrays.asList(1, 10), true, Arrays.asList(0, 1)},
                new Object[]{"select * from t_warehouse where id between ? and ? limit ? offset ?", Arrays.asList(1, 10, 100, 50), true, Arrays.asList(0, 1)},
                new Object[]{"update t_broadcast_table set broadcast_table_col1 = ?", Collections.singletonList(0), true, Collections.emptyList()},
                new Object[]{"update t_broadcast_table set broadcast_table_col1 = ? where broadcast_table_id = ?", Arrays.asList(0, 1), true, Collections.emptyList()},
                new Object[]{"update t_warehouse set warehouse_name = ? where id = ?", Arrays.asList("foo", 1), true, Collections.singletonList(1)},
                new Object[]{"delete from t_broadcast_table", Collections.emptyList(), true, Collections.emptyList()},
                new Object[]{"delete from t_warehouse where id = ?", Collections.singletonList(1), true, Collections.singletonList(0)});
        Collection<Object[]> nonCacheableCases = Arrays.asList(
                new Object[]{"create table t_warehouse (id int4 not null primary key)", Collections.emptyList(), false, Collections.emptyList()},
                new Object[]{"insert into t_warehouse (id) select warehouse_id from t_order", Collections.emptyList(), false, Collections.emptyList()},
                new Object[]{"insert into t_broadcast_table (broadcast_table_id, broadcast_table_col1) values (?, ?), (?, ?)", Arrays.asList(1, "foo", 2, "bar"), false, Collections.emptyList()},
                new Object[]{"insert into t_warehouse (id) values (?), (?)", Arrays.asList(1, 2), false, Collections.emptyList()},
                new Object[]{"insert into t_non_sharding_table (id) values (?)", Collections.singletonList(1), false, Collections.emptyList()},
                new Object[]{"insert into t_non_cacheable_database_sharding (id) values (?)", Collections.singletonList(1), false, Collections.emptyList()},
                new Object[]{"insert into t_non_cacheable_table_sharding (id) values (?)", Collections.singletonList(1), false, Collections.emptyList()},
                new Object[]{"insert into t_warehouse (id) values (now())", Collections.emptyList(), false, Collections.emptyList()},
                new Object[]{"select * from t_broadcast_table where broadcast_table_id = ?", Collections.singletonList(1), false, Collections.emptyList()},
                new Object[]{"select * from t_warehouse w join t_order o on w.id = o.warehouse_id where w.id = ?", Collections.singletonList(1), false, Collections.emptyList()},
                new Object[]{"update t_warehouse set warehouse_name = ? where id = (select max(warehouse_id) from t_order)", Collections.singletonList("foo"), false, Collections.emptyList()},
                new Object[]{"delete from t_order where warehouse_id in (1, 2, now())", Collections.emptyList(), false, Collections.emptyList()},
                new Object[]{"delete from t_order where warehouse_id between now() and now()", Collections.emptyList(), false, Collections.emptyList()},
                new Object[]{"delete from t_order o where o.warehouse_id in (select w.id from t_warehouse w)", Collections.emptyList(), false, Collections.emptyList()});
        return Stream.of(probablyCacheableCases.stream(), nonCacheableCases.stream()).flatMap(Function.identity()).collect(Collectors.toList());
    }
    
    @Test
    public void assertCheckCacheable() {
        ShardingRule shardingRule = prepareShardingRule();
        ShardingCacheRule shardingCacheRule = prepareShardingCacheRule(shardingRule);
        ShardingSphereDatabase database = prepareDatabase(shardingRule, shardingCacheRule);
        ShardingRouteCacheableCheckResult actual = new ShardingRouteCacheableChecker(shardingCacheRule).check(database, prepareQueryContext(database, sql, parameters));
        assertThat(actual.isProbablyCacheable(), is(expectedProbablyCacheable));
        assertThat(actual.getShardingConditionParameterMarkerIndexes(), is(expectedShardingConditionParameterMarkerIndexes));
    }
    
    private ShardingRule prepareShardingRule() {
        ShardingRuleConfiguration configuration = new ShardingRuleConfiguration();
        configuration.getBroadcastTables().add("t_broadcast_table");
        configuration.getBindingTableGroups().add("t_order,t_order_item");
        Properties modShardingAlgorithmProps = new Properties();
        modShardingAlgorithmProps.setProperty("sharding-count", "2");
        configuration.getShardingAlgorithms().put("mod", new AlgorithmConfiguration("MOD", modShardingAlgorithmProps));
        Properties inlineShardingAlgorithmProps = new Properties();
        inlineShardingAlgorithmProps.setProperty("algorithm-expression", "ds_${id % 2}");
        configuration.getShardingAlgorithms().put("inline", new AlgorithmConfiguration("INLINE", inlineShardingAlgorithmProps));
        configuration.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("warehouse_id", "mod"));
        ShardingTableRuleConfiguration warehouse = new ShardingTableRuleConfiguration("t_warehouse", "ds_${0..1}.t_warehouse");
        warehouse.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("id", "mod"));
        configuration.getTables().add(warehouse);
        configuration.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order"));
        configuration.getTables().add(new ShardingTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item"));
        ShardingTableRuleConfiguration nonCacheableDatabaseSharding = new ShardingTableRuleConfiguration("t_non_cacheable_database_sharding", "ds_${0..1}.t_non_cacheable_database_sharding");
        nonCacheableDatabaseSharding.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("id", "inline"));
        configuration.getTables().add(nonCacheableDatabaseSharding);
        ShardingTableRuleConfiguration nonCacheableTableSharding = new ShardingTableRuleConfiguration("t_non_cacheable_table_sharding", "ds_0.t_non_cacheable_table_sharding_${0..1}");
        nonCacheableTableSharding.setTableShardingStrategy(new StandardShardingStrategyConfiguration("id", "inline"));
        configuration.getTables().add(nonCacheableTableSharding);
        return new ShardingRule(configuration, Arrays.asList("ds_0", "ds_1"), new InstanceContext(mock(ComputeNodeInstance.class), props -> 0, null, null, null));
    }
    
    private ShardingCacheRule prepareShardingCacheRule(final ShardingRule shardingRule) {
        return new ShardingCacheRule(new ShardingCacheRuleConfiguration(100, new ShardingCacheOptions(true, 0, 0)), shardingRule);
    }
    
    private ShardingSphereDatabase prepareDatabase(final ShardingRule shardingRule, final ShardingCacheRule shardingCacheRule) {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_broadcast_table", new ShardingSphereTable("t_broadcast_table", Arrays.asList(
                new ShardingSphereColumn("broadcast_table_id", Types.INTEGER, true, false, false, true),
                new ShardingSphereColumn("broadcast_table_col1", Types.VARCHAR, false, false, false, true)),
                Collections.emptyList(), Collections.emptyList()));
        schema.getTables().put("t_warehouse", new ShardingSphereTable("t_warehouse",
                Collections.singletonList(new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true)),
                Collections.emptyList(), Collections.emptyList()));
        schema.getTables().put("t_order", new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("warehouse_id", Types.INTEGER, false, false, false, true),
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true)),
                Collections.emptyList(), Collections.emptyList()));
        schema.getTables().put("t_order_item", new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("warehouse_id", Types.INTEGER, false, false, false, true),
                new ShardingSphereColumn("order_broadcast_table_id", Types.INTEGER, true, false, false, true)),
                Collections.emptyList(), Collections.emptyList()));
        return new ShardingSphereDatabase(DATABASE_NAME, DatabaseTypeFactory.getInstance("PostgreSQL"), new ShardingSphereResourceMetaData(DATABASE_NAME, Collections.emptyMap()),
                new ShardingSphereRuleMetaData(Arrays.asList(shardingRule, shardingCacheRule)), Collections.singletonMap(SCHEMA_NAME, schema));
    }
    
    private QueryContext prepareQueryContext(final ShardingSphereDatabase database, final String sql, final List<Object> parameters) {
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(Collections.singletonMap(DATABASE_NAME, database), parameters, parse(sql), DATABASE_NAME);
        return new QueryContext(sqlStatementContext, sql, parameters);
    }
    
    private SQLStatement parse(final String sql) {
        CacheOption cacheOption = new CacheOption(0, 0);
        return new SQLStatementParserEngine("PostgreSQL", cacheOption, cacheOption, false).parse(sql, false);
    }
}
