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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingAggregationDistinctTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingDistinctProjectionPrefixTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingRemoveTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingTableTokenGenerator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingTokenGenerateBuilderTest {
    
    private ShardingRule shardingRule;
    
    private RouteContext routeContext;
    
    @BeforeEach
    void setup() {
        shardingRule = mock(ShardingRule.class);
        routeContext = mock(RouteContext.class);
    }
    
    @Test
    void assertGetSQLTokenGenerators() throws ReflectiveOperationException {
        when(routeContext.containsTableSharding()).thenReturn(true);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getAggregationProjections().isEmpty()).thenReturn(false);
        when(sqlStatementContext.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes());
        ShardingTokenGenerateBuilder shardingTokenGenerateBuilder = new ShardingTokenGenerateBuilder(shardingRule, routeContext, sqlStatementContext);
        Collection<SQLTokenGenerator> sqlTokenGenerators = shardingTokenGenerateBuilder.getSQLTokenGenerators();
        assertThat(sqlTokenGenerators.size(), is(4));
        Iterator<SQLTokenGenerator> iterator = sqlTokenGenerators.iterator();
        SQLTokenGenerator tableTokenGenerator = iterator.next();
        assertThat(tableTokenGenerator, isA(ShardingTableTokenGenerator.class));
        assertSqlTokenGenerator(tableTokenGenerator);
        SQLTokenGenerator distinctProjectionPrefixTokenGenerator = iterator.next();
        assertThat(distinctProjectionPrefixTokenGenerator, isA(ShardingDistinctProjectionPrefixTokenGenerator.class));
        assertSqlTokenGenerator(distinctProjectionPrefixTokenGenerator);
        SQLTokenGenerator aggregationDistinctTokenGenerator = iterator.next();
        assertThat(aggregationDistinctTokenGenerator, isA(ShardingAggregationDistinctTokenGenerator.class));
        assertSqlTokenGenerator(aggregationDistinctTokenGenerator);
        SQLTokenGenerator shardingRemoveTokenGenerator = iterator.next();
        assertThat(shardingRemoveTokenGenerator, isA(ShardingRemoveTokenGenerator.class));
        assertSqlTokenGenerator(shardingRemoveTokenGenerator);
    }
    
    private void assertSqlTokenGenerator(final SQLTokenGenerator sqlTokenGenerator) throws ReflectiveOperationException {
        if (sqlTokenGenerator instanceof RouteContextAware) {
            assertField(sqlTokenGenerator, routeContext, "routeContext");
        }
    }
    
    private void assertField(final SQLTokenGenerator sqlTokenGenerator, final Object filedInstance, final String fieldName) throws ReflectiveOperationException {
        assertThat(Plugins.getMemberAccessor().get(sqlTokenGenerator.getClass().getDeclaredField(fieldName), sqlTokenGenerator), is(filedInstance));
    }
}
