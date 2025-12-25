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
package org.apache.shardingsphere.infra.route.engine.tableless.router;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TablelessSQLRouterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private QueryContext queryContext;
    
    @Mock
    private RuleMetaData ruleMetaData;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertRouteWhenTableNameRouteUnitIsAllEmpty() {
        AggregatedDataSourceRuleAttribute ruleAttribute = mock(AggregatedDataSourceRuleAttribute.class);
        when(ruleAttribute.getAggregatedDataSources()).thenReturn(Collections.singletonMap("ds_0", mock(DataSource.class)));
        when(database.getRuleMetaData().getAttributes(AggregatedDataSourceRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        RouteContext actual = new TablelessSQLRouter().route(queryContext, ruleMetaData, database, Collections.emptyList(), new RouteContext());
        assertTrue(actual.getOriginalDataNodes().isEmpty());
        assertThat(actual.getRouteUnits().size(), is(1));
        assertTrue(actual.getRouteStageContexts().isEmpty());
    }
    
    @Test
    void assertRouteWhenTableNameIsNotEmpty() {
        RouteContext routeContext = new RouteContext();
        RouteContext actual = new TablelessSQLRouter().route(queryContext, ruleMetaData, database, Collections.singleton("foo_table"), routeContext);
        assertThat(actual, is(routeContext));
    }
    
    @Test
    void assertRouteWhenRouteUnitIsNotEmpty() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.emptyList()));
        RouteContext actual = new TablelessSQLRouter().route(queryContext, ruleMetaData, database, Collections.emptyList(), routeContext);
        assertThat(actual, is(routeContext));
    }
}
