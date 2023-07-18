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

import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.timeservice.api.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ShardingRouteCacheableCheckerTest {
    
    private static final String DATABASE_NAME = "sharding_db";
    
    private static final String SCHEMA_NAME = "public";
    
    @ParameterizedTest(name = "probably cacheable: {2}, SQL: {0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertCheckCacheable(final String sql, final List<Object> parameters, final boolean expectedProbablyCacheable, final List<Integer> expectedShardingConditionParameterMarkerIndexes) {
        ShardingRule shardingRule = createShardingRule();
        TimestampServiceRule timestampServiceRule = createTimeServiceRule();
        ShardingSphereDatabase database = createDatabase(shardingRule, timestampServiceRule);
        ShardingRouteCacheableCheckResult actual = new ShardingRouteCacheableChecker(shardingRule.getShardingCache()).check(database, createQueryContext(database, sql, parameters));
        assertThat(actual.isProbablyCacheable(), is(expectedProbablyCacheable));
        assertThat(actual.getShardingConditionParameterMarkerIndexes(), is(expectedShardingConditionParameterMarkerIndexes));
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", "t_order,t_order_item"));
        ruleConfig.getShardingAlgorithms().put("mod", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        ruleConfig.getShardingAlgorithms().put("inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${id % 2}"))));
        ruleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("warehouse_id", "inline"));
        ShardingAutoTableRuleConfiguration warehouse = new ShardingAutoTableRuleConfiguration("t_warehouse", "ds_${0..1}");
        warehouse.setShardingStrategy(new StandardShardingStrategyConfiguration("id", "mod"));
        ruleConfig.getAutoTables().add(warehouse);
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order"));
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item"));
        ShardingTableRuleConfiguration nonCacheableDatabaseSharding = new ShardingTableRuleConfiguration("t_non_cacheable_database_sharding", "ds_${0..1}.t_non_cacheable_database_sharding");
        nonCacheableDatabaseSharding.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("id", "inline"));
        ruleConfig.getTables().add(nonCacheableDatabaseSharding);
        ShardingTableRuleConfiguration nonCacheableTableSharding = new ShardingTableRuleConfiguration("t_non_cacheable_table_sharding", "ds_0.t_non_cacheable_table_sharding_${0..1}");
        nonCacheableTableSharding.setTableShardingStrategy(new StandardShardingStrategyConfiguration("id", "inline"));
        ruleConfig.getTables().add(nonCacheableTableSharding);
        ruleConfig.setShardingCache(new ShardingCacheConfiguration(100, new ShardingCacheOptionsConfiguration(true, 0, 0)));
        return new ShardingRule(ruleConfig, Arrays.asList("ds_0", "ds_1"), new InstanceContext(mock(ComputeNodeInstance.class), props -> 0, null, null, null, null));
    }
    
    private TimestampServiceRule createTimeServiceRule() {
        return new TimestampServiceRule(new TimestampServiceRuleConfiguration("System", new Properties()));
    }
    
    private ShardingSphereDatabase createDatabase(final ShardingRule shardingRule, final TimestampServiceRule timestampServiceRule) {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_warehouse", new ShardingSphereTable("t_warehouse",
                Collections.singletonList(new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.getTables().put("t_order", new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("warehouse_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.getTables().put("t_order_item", new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("warehouse_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("order_broadcast_table_id", Types.INTEGER, true, false, false, true, false)),
                Collections.emptyList(), Collections.emptyList()));
        return new ShardingSphereDatabase(DATABASE_NAME, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"),
                new ShardingSphereResourceMetaData(DATABASE_NAME, Collections.emptyMap()), new ShardingSphereRuleMetaData(Arrays.asList(shardingRule, timestampServiceRule)),
                Collections.singletonMap(SCHEMA_NAME, schema));
    }
    
    private QueryContext createQueryContext(final ShardingSphereDatabase database, final String sql, final List<Object> params) {
        return new QueryContext(SQLStatementContextFactory.newInstance(createShardingSphereMetaData(database), params, parse(sql), DATABASE_NAME), sql, params);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DATABASE_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private SQLStatement parse(final String sql) {
        CacheOption cacheOption = new CacheOption(0, 0);
        return new SQLStatementParserEngine("PostgreSQL", cacheOption, cacheOption, false).parse(sql, false);
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<? extends Arguments> probablyCacheableCases = Arrays.asList(
                    Arguments.of("insert into t_warehouse (id) values (?)", Collections.singletonList(1), true, Collections.singletonList(0)),
                    Arguments.of("select * from t_warehouse where id = ?", Collections.singletonList(1), true, Collections.singletonList(0)),
                    Arguments.of("select * from t_warehouse where id in (?, ?, ?)", Arrays.asList(1, 2, 3), true, Arrays.asList(0, 1, 2)),
                    Arguments.of("select * from t_warehouse where id between ? and ?", Arrays.asList(1, 10), true, Arrays.asList(0, 1)),
                    Arguments.of("select * from t_warehouse where id between ? and ? limit ? offset ?", Arrays.asList(1, 10, 100, 50), true, Arrays.asList(0, 1)),
                    Arguments.of("update t_warehouse set warehouse_name = ? where id = ?", Arrays.asList("foo", 1), true, Collections.singletonList(1)),
                    Arguments.of("delete from t_warehouse where id = ?", Collections.singletonList(1), true, Collections.singletonList(0)));
            Collection<? extends Arguments> nonCacheableCases = Arrays.asList(
                    Arguments.of("create table t_warehouse (id int4 not null primary key)", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("insert into t_warehouse (id) select warehouse_id from t_order", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("insert into t_warehouse (id) values (?), (?)", Arrays.asList(1, 2), false, Collections.emptyList()),
                    Arguments.of("insert into t_non_sharding_table (id) values (?)", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("insert into t_non_cacheable_database_sharding (id) values (?)", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("insert into t_non_cacheable_table_sharding (id) values (?)", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("insert into t_warehouse (id) values (now())", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("select * from t_warehouse w join t_order o on w.id = o.warehouse_id where w.id = ?", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("update t_warehouse set warehouse_name = ? where id = (select max(warehouse_id) from t_order)", Collections.singletonList("foo"), false, Collections.emptyList()),
                    Arguments.of("delete from t_order where warehouse_id in (1, 2, now())", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("delete from t_order where warehouse_id between now() and now()", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("delete from t_order o where o.warehouse_id in (select w.id from t_warehouse w)", Collections.emptyList(), false, Collections.emptyList()));
            return Stream.of(probablyCacheableCases.stream(), nonCacheableCases.stream()).flatMap(Function.identity());
        }
    }
}
