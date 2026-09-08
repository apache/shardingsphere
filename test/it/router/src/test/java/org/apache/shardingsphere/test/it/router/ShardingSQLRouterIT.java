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

package org.apache.shardingsphere.test.it.router;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class ShardingSQLRouterIT {
    
    private static final String DATABASE_NAME = "foo_db";
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestArguments")
    void assertRoute(final String name, final String sql, final List<Object> params, final Set<String> expectedRouteUnits,
                     final List<Set<String>> expectedOriginalDataNodes) {
        RouteContext actual = route(sql, params);
        assertThat(getRouteUnitSignatures(actual), is(expectedRouteUnits));
        assertThat(getOriginalDataNodeSignatures(actual), is(expectedOriginalDataNodes));
    }
    
    private static Stream<Arguments> getTestArguments() {
        return Stream.of(
                Arguments.of("oneTableDifferentConditionWithFederation",
                        "SELECT (SELECT MAX(id) FROM t_order b WHERE b.user_id =? ) FROM t_order a WHERE user_id = ? ", Arrays.asList(3, 2),
                        createRouteUnits("ds_0:t_order=t_order_0", "ds_1:t_order=t_order_1"),
                        createOriginalDataNodes("ds_1.t_order_1", "ds_0.t_order_0")),
                Arguments.of("oneTableSameConditionWithFederation",
                        "SELECT (SELECT MAX(id) FROM t_order b WHERE b.user_id = ? AND b.user_id = a.user_id) FROM t_order a WHERE user_id = ? ", Arrays.asList(1, 1),
                        createRouteUnits("ds_1:t_order=t_order_1"), createOriginalDataNodes("ds_1.t_order_1")),
                Arguments.of("bindingTableWithFederation",
                        "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id = ?) FROM t_order a WHERE user_id = ? ", Arrays.asList(1, 1),
                        createRouteUnits("ds_1:t_order_item=t_order_item_1"), createOriginalDataNodes("ds_1.t_order_item_1")),
                Arguments.of("subqueryInSubqueryError",
                        "SELECT (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?)) as c FROM t_order a "
                                + "WHERE status = (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?))",
                        Arrays.asList(11, 2, 1, 1), createRouteUnits("ds_0:t_order=t_order_0", "ds_1:t_order=t_order_1"),
                        createOriginalDataNodes("ds_1.t_order_1", "ds_0.t_order_0", "ds_1.t_order_1")),
                Arguments.of("subqueryInSubquery",
                        "SELECT (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?)) as c FROM t_order a "
                                + "WHERE status = (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?))",
                        Arrays.asList(1, 1, 1, 1), createRouteUnits("ds_1:t_order=t_order_1"), createOriginalDataNodes("ds_1.t_order_1")),
                Arguments.of("subqueryInFromError",
                        "SELECT b.status FROM t_order b join (SELECT user_id,status FROM t_order b WHERE b.user_id =?) c ON b.user_id = c.user_id WHERE b.user_id =? ",
                        Arrays.asList(11, 1), createRouteUnits("ds_1:t_order=t_order_1"), createOriginalDataNodes("ds_1.t_order_1", "ds_1.t_order_1")),
                Arguments.of("subqueryInFrom",
                        "SELECT b.status FROM t_order b join (SELECT user_id,status FROM t_order b WHERE b.user_id =?) c ON b.user_id = c.user_id WHERE b.user_id =? ",
                        Arrays.asList(1, 1), createRouteUnits("ds_1:t_order=t_order_1"), createOriginalDataNodes("ds_1.t_order_1")),
                Arguments.of("subqueryForAggregation",
                        "SELECT count(*) FROM t_order WHERE user_id = (SELECT user_id FROM t_order WHERE user_id =?) ", Collections.singletonList(1),
                        createRouteUnits("ds_1:t_order=t_order_1"), createOriginalDataNodes("ds_1.t_order_1")),
                Arguments.of("subqueryForBinding",
                        "SELECT count(*) FROM t_order WHERE user_id = (SELECT user_id FROM t_order_item WHERE user_id =?) ", Collections.singletonList(1),
                        createRouteUnits("ds_1:t_order_item=t_order_item_1"), createOriginalDataNodes("ds_1.t_order_item_1")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSmokeTestArguments")
    void assertRouteWithoutException(final String name, final String sql, final List<Object> params) {
        RouteContext actual = assertDoesNotThrow(() -> route(sql, params));
        assertFalse(actual.getRouteUnits().isEmpty());
    }
    
    private static Stream<Arguments> getSmokeTestArguments() {
        return Stream.of(
                Arguments.of("bindingTableWithDifferentValueWithFederation",
                        "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id = ? ) FROM t_order a WHERE user_id = ? ", Arrays.asList(2, 3)),
                Arguments.of("twoTableWithDifferentOperatorWithFederation",
                        "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id in(?,?)) FROM t_order a WHERE user_id = ? ", Arrays.asList(1, 2, 1)),
                Arguments.of("twoTableWithInWithFederation",
                        "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id in(?,?)) FROM t_order a WHERE user_id in(?,?) ", Arrays.asList(1, 2, 1, 3)));
    }
    
    @Test
    void assertRouteWithHint() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("t_hint_test", 1);
            hintManager.addTableShardingValue("t_hint_test", 1);
            String sql = "SELECT COUNT(*) FROM t_hint_test WHERE user_id = (SELECT user_id FROM t_hint_test WHERE user_id IN (?,?,?)) ";
            RouteContext actual = route(sql, Arrays.asList(1, 3, 5));
            assertThat(getRouteUnitSignatures(actual), is(createRouteUnits("ds_1:t_hint_test=t_hint_test_1")));
            assertThat(getOriginalDataNodeSignatures(actual), is(Collections.emptyList()));
        }
    }
    
    private static RouteContext route(final String sql, final List<Object> params) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingRule shardingRule = createShardingRule();
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ShardingSphereDatabase database = createDatabase(databaseType, shardingRule, props);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), globalRuleMetaData, props);
        SQLParserEngine parserEngine = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()).getSQLParserEngine(databaseType);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, DATABASE_NAME, new HintValueContext()).bind(parserEngine.parse(sql, false));
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.setCurrentDatabaseName(DATABASE_NAME);
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, params, new HintValueContext(), connectionContext, metaData);
        return new SQLRouteEngine(Collections.singleton(shardingRule), props).route(queryContext, globalRuleMetaData, database);
    }
    
    private static ShardingSphereDatabase createDatabase(final DatabaseType databaseType, final ShardingRule shardingRule, final ConfigurationProperties props) {
        return new ShardingSphereDatabase(DATABASE_NAME, databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS),
                new RuleMetaData(Collections.singleton(shardingRule)), buildSchemas(databaseType), props);
    }
    
    private static Collection<ShardingSphereSchema> buildSchemas(final DatabaseType databaseType) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_hint_test", Collections.singleton(
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema(DATABASE_NAME, databaseType, tables, Collections.emptyList()));
    }
    
    private static ShardingRule createShardingRule() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();
        config.getTables().add(createInlineTableRule("t_order"));
        config.getTables().add(createInlineTableRule("t_order_item"));
        config.getTables().add(createHintTableRule());
        config.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", "t_order,t_order_item"));
        config.getShardingAlgorithms().put("ds_inline", createAlgorithm("INLINE", "ds_${user_id % 2}"));
        config.getShardingAlgorithms().put("t_order_inline", createAlgorithm("INLINE", "t_order_${user_id % 2}"));
        config.getShardingAlgorithms().put("t_order_item_inline", createAlgorithm("INLINE", "t_order_item_${user_id % 2}"));
        config.getShardingAlgorithms().put("ds_hint_inline", createAlgorithm("HINT_INLINE", "ds_${value % 2}"));
        config.getShardingAlgorithms().put("t_hint_test_hint_inline", createAlgorithm("HINT_INLINE", "t_hint_test_${value % 2}"));
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put("ds_0", new MockedDataSource());
        dataSources.put("ds_1", new MockedDataSource());
        return new ShardingRule(config, dataSources, mock(ComputeNodeInstanceContext.class), Collections.emptyList());
    }
    
    private static ShardingTableRuleConfiguration createInlineTableRule(final String tableName) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(tableName, String.format("ds_${0..1}.%s_${0..1}", tableName));
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "ds_inline"));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("user_id", tableName + "_inline"));
        return result;
    }
    
    private static ShardingTableRuleConfiguration createHintTableRule() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_hint_test", "ds_${0..1}.t_hint_test_${0..1}");
        result.setDatabaseShardingStrategy(new HintShardingStrategyConfiguration("ds_hint_inline"));
        result.setTableShardingStrategy(new HintShardingStrategyConfiguration("t_hint_test_hint_inline"));
        return result;
    }
    
    private static AlgorithmConfiguration createAlgorithm(final String type, final String expression) {
        return new AlgorithmConfiguration(type, PropertiesBuilder.build(new Property("algorithm-expression", expression)));
    }
    
    private static Set<String> getRouteUnitSignatures(final RouteContext routeContext) {
        Set<String> result = new TreeSet<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            Collection<String> tableMappers = new TreeSet<>();
            for (RouteMapper tableMapper : each.getTableMappers()) {
                tableMappers.add(tableMapper.getLogicName() + "=" + tableMapper.getActualName());
            }
            result.add(each.getDataSourceMapper().getActualName() + ":" + String.join(",", tableMappers));
        }
        return result;
    }
    
    private static List<Set<String>> getOriginalDataNodeSignatures(final RouteContext routeContext) {
        List<Set<String>> result = new LinkedList<>();
        for (Collection<DataNode> each : routeContext.getOriginalDataNodes()) {
            Set<String> dataNodes = new TreeSet<>();
            for (DataNode dataNode : each) {
                dataNodes.add(dataNode.getDataSourceName() + "." + dataNode.getTableName());
            }
            result.add(dataNodes);
        }
        return result;
    }
    
    private static List<Set<String>> createOriginalDataNodes(final String... dataNodes) {
        List<Set<String>> result = new LinkedList<>();
        for (String each : dataNodes) {
            result.add(Collections.singleton(each));
        }
        return result;
    }
    
    private static Set<String> createRouteUnits(final String... routeUnits) {
        return new TreeSet<>(Arrays.asList(routeUnits));
    }
}
