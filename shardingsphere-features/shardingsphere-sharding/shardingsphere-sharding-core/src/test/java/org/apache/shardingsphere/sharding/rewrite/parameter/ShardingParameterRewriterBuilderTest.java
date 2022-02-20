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

package org.apache.shardingsphere.sharding.rewrite.parameter;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.parameter.impl.ShardingPaginationParameterRewriter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingParameterRewriterBuilderTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetParameterRewritersWhenPaginationIsNeedRewrite() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isSingleRouting()).thenReturn(false);
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getPaginationContext().isHasPagination()).thenReturn(true);
        ShardingParameterRewriterBuilder shardingParameterRewriterBuilder 
                = new ShardingParameterRewriterBuilder(shardingRule, routeContext, mock(ShardingSphereSchema.class), statementContext);
        Collection<ParameterRewriter> actual = shardingParameterRewriterBuilder.getParameterRewriters();
        assertThat(actual.size(), is(1));
        ParameterRewriter parameterRewriter = actual.iterator().next();
        assertThat(parameterRewriter, instanceOf(ShardingPaginationParameterRewriter.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetParameterRewritersWhenPaginationIsNotNeedRewrite() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isSingleRouting()).thenReturn(true);
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getPaginationContext().isHasPagination()).thenReturn(true);
        ShardingParameterRewriterBuilder shardingParameterRewriterBuilder
                = new ShardingParameterRewriterBuilder(shardingRule, routeContext, mock(ShardingSphereSchema.class), statementContext);
        Collection<ParameterRewriter> actual = shardingParameterRewriterBuilder.getParameterRewriters();
        assertThat(actual.size(), is(0));
    }
}
