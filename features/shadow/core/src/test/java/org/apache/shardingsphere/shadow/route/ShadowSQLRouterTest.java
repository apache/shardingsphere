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

package org.apache.shardingsphere.shadow.route;

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.SQLRouter.Type;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.shadow.route.retriever.ShadowDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.ShadowDataSourceMappingsRetrieverFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.column.PreciseColumnShadowValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShadowDataSourceMappingsRetrieverFactory.class)
class ShadowSQLRouterTest {
    
    @Mock
    private ShadowRule rule;
    
    private ShadowSQLRouter sqlRouter;
    
    @BeforeEach
    void setUp() {
        sqlRouter = (ShadowSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decorateRouteContextArguments")
    void assertDecorateRouteContext(final String name, final String routeActualDataSourceName, final String productionDataSourceName,
                                    final Map<String, String> shadowDataSourceMappings, final String expectedActualDataSourceName) {
        QueryContext queryContext = mock(QueryContext.class);
        ShadowDataSourceMappingsRetriever retriever = mock(ShadowDataSourceMappingsRetriever.class);
        when(retriever.retrieve(rule)).thenReturn(shadowDataSourceMappings);
        when(ShadowDataSourceMappingsRetrieverFactory.newInstance(queryContext)).thenReturn(retriever);
        when(rule.findProductionDataSourceName(routeActualDataSourceName)).thenReturn(null == productionDataSourceName ? Optional.empty() : Optional.of(productionDataSourceName));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("logic_ds", routeActualDataSourceName), Collections.singleton(new RouteMapper("t_order", "t_order_0"))));
        sqlRouter.decorateRouteContext(routeContext, queryContext, mock(), rule, Collections.singleton("t_order"), new ConfigurationProperties(new Properties()));
        RouteUnit actualRouteUnit = routeContext.getRouteUnits().iterator().next();
        assertThat(name, actualRouteUnit.getDataSourceMapper().getActualName(), is(expectedActualDataSourceName));
        assertThat(actualRouteUnit.getDataSourceMapper().getLogicName(), is("logic_ds"));
        assertThat(actualRouteUnit.getTableMappers().size(), is(1));
        assertThat(sqlRouter.getType(), is(Type.DATA_SOURCE));
    }
    
    private static Stream<Arguments> decorateRouteContextArguments() {
        return Stream.of(
                Arguments.of("skip route unit when production data source is absent", "foo_ds", null, Collections.emptyMap(), "foo_ds"),
                Arguments.of("replace route unit with shadow data source", "foo_route_ds", "foo_prod_ds", Collections.singletonMap("foo_prod_ds", "foo_shadow_ds"), "foo_shadow_ds"),
                Arguments.of("replace route unit with production data source when shadow mapping is absent", "foo_route_ds", "foo_prod_ds", Collections.emptyMap(), "foo_prod_ds"));
    }
    
    @Test
    void assertDecorateRouteContextForMixedInsertValues() {
        QueryContext queryContext = mock(QueryContext.class);
        ShadowDataSourceMappingsRetriever retriever = mock(ShadowDataSourceMappingsRetriever.class);
        when(retriever.retrieve(rule)).thenReturn(Collections.emptyMap());
        when(ShadowDataSourceMappingsRetrieverFactory.newInstance(queryContext)).thenReturn(retriever);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        when(insertStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("foo_tbl"));
        when(insertStatementContext.getInsertColumnNames()).thenReturn(Arrays.asList("foo_id", "foo_type"));
        List<InsertValueContext> insertValueContexts = Arrays.asList(createInsertValueContext(1), createInsertValueContext(0), createInsertValueContext(1));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(insertValueContexts);
        mockShadowRuleForInsert();
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_logic_ds", "foo_logic_ds"), Collections.singleton(new RouteMapper("foo_tbl", "foo_tbl"))));
        sqlRouter.decorateRouteContext(routeContext, queryContext, mock(ShardingSphereDatabase.class), rule, Collections.singleton("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getRouteUnits().size(), is(2));
        Map<String, RouteUnit> actualRouteUnits = new LinkedHashMap<>(2, 1F);
        for (RouteUnit each : routeContext.getRouteUnits()) {
            actualRouteUnits.put(each.getDataSourceMapper().getActualName(), each);
        }
        assertThat(actualRouteUnits.keySet(), is(new LinkedHashSet<>(Arrays.asList("foo_prod_ds", "foo_shadow_ds"))));
        assertThat(routeContext.getRouteUnitInsertValueIndexes().get(actualRouteUnits.get("foo_prod_ds")), is(Collections.singletonList(1)));
        assertThat(routeContext.getRouteUnitInsertValueIndexes().get(actualRouteUnits.get("foo_shadow_ds")), is(Arrays.asList(0, 2)));
    }
    
    @SuppressWarnings("unchecked")
    private void mockShadowRuleForInsert() {
        when(rule.filterShadowTables(Collections.singleton("foo_tbl"))).thenReturn(Collections.singletonList("foo_tbl"));
        when(rule.getShadowColumnNames(ShadowOperationType.INSERT, "foo_tbl")).thenReturn(Collections.singletonList("foo_type"));
        ColumnShadowAlgorithm<Comparable<?>> algorithm = mock(ColumnShadowAlgorithm.class);
        when(algorithm.isShadow(any())).thenAnswer(invocation -> {
            PreciseColumnShadowValue<Comparable<?>> shadowValue = invocation.getArgument(0);
            return Integer.valueOf(1).equals(shadowValue.getValue());
        });
        when(rule.getColumnShadowAlgorithms(ShadowOperationType.INSERT, "foo_tbl", "foo_type")).thenReturn(Collections.singletonList(algorithm));
        when(rule.findProductionDataSourceName("foo_logic_ds")).thenReturn(Optional.of("foo_prod_ds"));
        when(rule.getShadowDataSourceMappings("foo_tbl")).thenReturn(Collections.singletonMap("foo_prod_ds", "foo_shadow_ds"));
    }
    
    private InsertValueContext createInsertValueContext(final Object fooType) {
        InsertValueContext result = mock(InsertValueContext.class);
        when(result.getLiteralValue(1)).thenReturn(Optional.of(fooType));
        return result;
    }
}
