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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingTokenGenerateBuilderTest {
    
    @Test
    public void assertGetSQLTokenGenerators() throws Exception {
        ShardingRule shardingRule = mock(ShardingRule.class);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.containsTableSharding()).thenReturn(true);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getAggregationProjections().isEmpty()).thenReturn(false);
        ShardingTokenGenerateBuilder shardingTokenGenerateBuilder = new ShardingTokenGenerateBuilder(shardingRule, routeContext, sqlStatementContext);
        Collection<SQLTokenGenerator> sqlTokenGenerators = shardingTokenGenerateBuilder.getSQLTokenGenerators();
        assertThat(sqlTokenGenerators.size(), is(4));
        for (SQLTokenGenerator sqlTokenGenerator : sqlTokenGenerators) {
            if (sqlTokenGenerator instanceof ShardingRuleAware) {
                Field shardingRuleField = sqlTokenGenerator.getClass().getDeclaredField("shardingRule");
                shardingRuleField.setAccessible(true);
                assertNotNull(shardingRuleField.get(sqlTokenGenerator));
                assertThat(shardingRuleField.get(sqlTokenGenerator), is(shardingRule));
            }
            if (sqlTokenGenerator instanceof RouteContextAware) {
                Field routeContextField = sqlTokenGenerator.getClass().getDeclaredField("routeContext");
                routeContextField.setAccessible(true);
                assertNotNull(routeContextField.get(sqlTokenGenerator));
                assertThat(routeContextField.get(sqlTokenGenerator), is(routeContext));
            }
        }
    }
}
