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

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.timeservice.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRouteCacheableCheckerTest {
    
    private static final String DATABASE_NAME = "sharding_db";
    
    private static final String SCHEMA_NAME = "public";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
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
        ruleConfig.getShardingAlgorithms().put("table-inline",
                new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_non_cacheable_table_sharding_${id % 2}"))));
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
        nonCacheableTableSharding.setTableShardingStrategy(new StandardShardingStrategyConfiguration("id", "table-inline"));
        ruleConfig.getTables().add(nonCacheableTableSharding);
        ruleConfig.setShardingCache(new ShardingCacheConfiguration(100, new ShardingCacheOptionsConfiguration(true, 0, 0)));
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(mock(ComputeNodeInstance.class), null, null);
        instanceContext.init(props -> 0);
        return new ShardingRule(ruleConfig, Maps.of("ds_0", new MockedDataSource(), "ds_1", new MockedDataSource()), instanceContext, Collections.emptyList());
    }
    
    private TimestampServiceRule createTimeServiceRule() {
        return new TimestampServiceRule(new TimestampServiceRuleConfiguration("System", new Properties()));
    }
    
    private ShardingSphereDatabase createDatabase(final ShardingRule shardingRule, final TimestampServiceRule timestampServiceRule) {
        ShardingSphereSchema schema = new ShardingSphereSchema(SCHEMA_NAME, databaseType);
        schema.putTable(new ShardingSphereTable("t_warehouse", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("warehouse_name", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.putTable(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("warehouse_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.putTable(new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("warehouse_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("order_broadcast_table_id", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.putTable(new ShardingSphereTable("t_non_sharding_table", Collections.singleton(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.putTable(new ShardingSphereTable("t_non_cacheable_database_sharding", Collections.singleton(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        schema.putTable(new ShardingSphereTable("t_non_cacheable_table_sharding", Collections.singleton(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        return new ShardingSphereDatabase(DATABASE_NAME, databaseType,
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Arrays.asList(shardingRule, timestampServiceRule)), Collections.singleton(schema));
    }
    
    private QueryContext createQueryContext(final ShardingSphereDatabase database, final String sql, final List<Object> params) {
        SQLStatementContext sqlStatementContext = new SQLBindEngine(
                new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class)),
                DATABASE_NAME, new HintValueContext()).bind(parse(sql));
        return new QueryContext(sqlStatementContext, sql, params, new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
        return result;
    }
    
    private SQLStatement parse(final String sql) {
        SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(0, 0L), new CacheOption(0, 0L)));
        return sqlParserRule.getSQLParserEngine(databaseType).parse(sql, false);
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            Collection<? extends Arguments> probablyCacheableCases = Arrays.asList(
                    Arguments.of("INSERT INTO t_warehouse (id) VALUES (?)", Collections.singletonList(1), true, Collections.singletonList(0)),
                    Arguments.of("SELECT * FROM t_warehouse WHERE id = ?", Collections.singletonList(1), true, Collections.singletonList(0)),
                    Arguments.of("SELECT * FROM t_warehouse WHERE id in (?, ?, ?)", Arrays.asList(1, 2, 3), true, Arrays.asList(0, 1, 2)),
                    Arguments.of("SELECT * FROM t_warehouse WHERE id BETWEEN ? AND ?", Arrays.asList(1, 10), true, Arrays.asList(0, 1)),
                    Arguments.of("SELECT * FROM t_warehouse WHERE id BETWEEN ? AND ? LIMIT ? OFFSET ?", Arrays.asList(1, 10, 100, 50), true, Arrays.asList(0, 1)),
                    Arguments.of("UPDATE t_warehouse SET warehouse_name = ? WHERE id = ?", Arrays.asList("foo", 1), true, Collections.singletonList(1)),
                    Arguments.of("DELETE FROM t_warehouse WHERE id = ?", Collections.singletonList(1), true, Collections.singletonList(0)));
            Collection<? extends Arguments> nonCacheableCases = Arrays.asList(
                    Arguments.of("CREATE TABLE t_warehouse_for_create (id int4 not null primary key)", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("INSERT INTO t_warehouse (id) SELECT warehouse_id FROM t_order", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("INSERT INTO t_warehouse (id) VALUES (?), (?)", Arrays.asList(1, 2), false, Collections.emptyList()),
                    Arguments.of("INSERT INTO t_non_sharding_table (id) VALUES (?)", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("INSERT INTO t_non_cacheable_database_sharding (id) VALUES (?)", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("INSERT INTO t_non_cacheable_table_sharding (id) VALUES (?)", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("INSERT INTO t_warehouse (id) VALUES (now())", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("SELECT * FROM t_warehouse w JOIN t_order o on w.id = o.warehouse_id WHERE w.id = ?", Collections.singletonList(1), false, Collections.emptyList()),
                    Arguments.of("UPDATE t_warehouse SET warehouse_name = ? WHERE id = (SELECT max(warehouse_id) FROM t_order)", Collections.singletonList("foo"), false, Collections.emptyList()),
                    Arguments.of("DELETE FROM t_order WHERE warehouse_id in (1, 2, now())", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("DELETE FROM t_order WHERE warehouse_id BETWEEN now() AND now()", Collections.emptyList(), false, Collections.emptyList()),
                    Arguments.of("DELETE FROM t_order o WHERE o.warehouse_id IN (SELECT w.id FROM t_warehouse w)", Collections.emptyList(), false, Collections.emptyList()));
            return Stream.of(probablyCacheableCases.stream(), nonCacheableCases.stream()).flatMap(Function.identity());
        }
    }
}
