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

package org.apache.shardingsphere.infra.route.engine;

import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.syntax.hint.DataSourceHintNotExistsException;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.fixture.rule.DataSourceRouteRuleFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.TableRouteRuleFixture;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SQLRouteEngineTest {
    
    private final SQLRouteEngine sqlRouteEngine =
            new SQLRouteEngine(Arrays.asList(new TableRouteRuleFixture(), new DataSourceRouteRuleFixture()), new ConfigurationProperties(new Properties()));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommonSQLStatementContext sqlStatementContext;
    
    @Mock
    private HintValueContext hintValueContext;
    
    private final ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
    
    @BeforeEach
    void setup() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mock(StorageUnit.class));
        storageUnits.put("ds_1", mock(StorageUnit.class));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        connectionContext.setCurrentDatabaseName("foo_db");
    }
    
    @Test
    void assertRouteBySQLCommentHint() {
        when(hintValueContext.findHintDataSourceName()).thenReturn(Optional.of("ds_1"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, connectionContext, metaData);
        RouteContext routeContext = sqlRouteEngine.route(queryContext, mock(RuleMetaData.class), database);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
    }
    
    @Test
    void assertRouteByHintManagerHint() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_1");
            QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
            RouteContext routeContext = sqlRouteEngine.route(queryContext, mock(RuleMetaData.class), database);
            assertThat(routeContext.getRouteUnits().size(), is(1));
            assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
        }
    }
    
    @Test
    void assertRouteBySQLCommentHintWithException() {
        when(hintValueContext.findHintDataSourceName()).thenReturn(Optional.of("ds_3"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, connectionContext, metaData);
        assertThrows(DataSourceHintNotExistsException.class, () -> sqlRouteEngine.route(queryContext, mock(RuleMetaData.class), database));
    }
    
    @Test
    void assertRouteByHintManagerHintWithException() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_3");
            QueryContext logicSQL = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
            assertThrows(DataSourceHintNotExistsException.class, () -> sqlRouteEngine.route(logicSQL, mock(RuleMetaData.class), database));
        }
    }
    
    @Test
    void assertRouteWithShardingSphereRule() {
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
        RouteContext routeContext = sqlRouteEngine.route(queryContext, mock(RuleMetaData.class), database);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_0"));
    }
    
    @Test
    void assertRouteWithEmptyRouteContext() {
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(Collections.emptyList(), new ConfigurationProperties(new Properties()));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        AggregatedDataSourceRuleAttribute ruleAttribute = mock(AggregatedDataSourceRuleAttribute.class);
        when(ruleAttribute.getAggregatedDataSources()).thenReturn(Collections.singletonMap("ds_0", mock(DataSource.class)));
        when(database.getRuleMetaData().getAttributes(AggregatedDataSourceRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        RouteContext routeContext = sqlRouteEngine.route(queryContext, mock(RuleMetaData.class), database);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_0"));
    }
}
