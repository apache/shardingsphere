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
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.parameter.impl.ShardingPaginationParameterRewriter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingParameterRewriterBuilderTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetParameterRewritersWhenPaginationIsNeedRewrite() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getPaginationContext().isHasPagination()).thenReturn(true);
        Collection<ParameterRewriter> actual = new ShardingParameterRewriterBuilder(
                mock(ShardingRule.class), mock(RouteContext.class), Collections.singletonMap("test", mock(ShardingSphereSchema.class)), statementContext).getParameterRewriters();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(ShardingPaginationParameterRewriter.class));
    }
    
    @Test
    public void assertGetParameterRewritersWhenPaginationIsNotNeedRewrite() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isSingleRouting()).thenReturn(true);
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getPaginationContext().isHasPagination()).thenReturn(true);
        assertTrue(new ShardingParameterRewriterBuilder(
                mock(ShardingRule.class), routeContext, Collections.singletonMap("test", mock(ShardingSphereSchema.class)), statementContext).getParameterRewriters().isEmpty());
    }
}
