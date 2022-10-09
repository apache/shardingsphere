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

import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.parameter.impl.ShardingPaginationParameterRewriter;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingPaginationParameterRewriterTest {
    
    private static final int TEST_OFFSET_PARAMETER_INDEX = 3;
    
    private static final int TEST_ROW_COUNT_PARAMETER_INDEX = 5;
    
    private static final long TEST_REVISED_OFFSET = 4;
    
    private static final long TEST_REVISED_ROW_COUNT = 6;
    
    private static boolean addOffsetParametersFlag = Boolean.FALSE;
    
    private static boolean addRowCountParameterFlag = Boolean.FALSE;
    
    @Test
    public void assertIsNeedRewrite() {
        ShardingPaginationParameterRewriter shardingPaginationParameterRewriter = new ShardingPaginationParameterRewriter();
        RouteContext routeContext = mock(RouteContext.class);
        shardingPaginationParameterRewriter.setRouteContext(routeContext);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        assertFalse(shardingPaginationParameterRewriter.isNeedRewrite(insertStatementContext));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getPaginationContext().isHasPagination()).thenReturn(Boolean.FALSE);
        assertFalse(shardingPaginationParameterRewriter.isNeedRewrite(selectStatementContext));
        when(selectStatementContext.getPaginationContext().isHasPagination()).thenReturn(Boolean.TRUE);
        when(routeContext.isSingleRouting()).thenReturn(Boolean.TRUE);
        assertFalse(shardingPaginationParameterRewriter.isNeedRewrite(selectStatementContext));
        when(routeContext.isSingleRouting()).thenReturn(Boolean.FALSE);
        assertTrue(shardingPaginationParameterRewriter.isNeedRewrite(selectStatementContext));
    }
    
    @Test
    public void assertRewrite() {
        addOffsetParametersFlag = false;
        addRowCountParameterFlag = false;
        StandardParameterBuilder standardParameterBuilder = mock(StandardParameterBuilder.class);
        doAnswer((Answer<Void>) invocation -> {
            int index = invocation.getArgument(0);
            long parameter = invocation.getArgument(1);
            if (index == TEST_OFFSET_PARAMETER_INDEX && parameter == TEST_REVISED_OFFSET) {
                addOffsetParametersFlag = true;
            }
            if (index == TEST_ROW_COUNT_PARAMETER_INDEX && parameter == TEST_REVISED_ROW_COUNT) {
                addRowCountParameterFlag = true;
            }
            return null;
        }).when(standardParameterBuilder).addReplacedParameters(anyInt(), anyLong());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        PaginationContext pagination = mock(PaginationContext.class);
        when(pagination.getOffsetParameterIndex()).thenReturn(Optional.of(TEST_OFFSET_PARAMETER_INDEX));
        when(pagination.getRowCountParameterIndex()).thenReturn(Optional.of(TEST_ROW_COUNT_PARAMETER_INDEX));
        when(pagination.getRevisedOffset()).thenReturn(TEST_REVISED_OFFSET);
        when(pagination.getRevisedRowCount(selectStatementContext)).thenReturn(TEST_REVISED_ROW_COUNT);
        when(selectStatementContext.getPaginationContext()).thenReturn(pagination);
        ShardingPaginationParameterRewriter shardingPaginationParameterRewriter = new ShardingPaginationParameterRewriter();
        shardingPaginationParameterRewriter.rewrite(standardParameterBuilder, selectStatementContext, null);
        assertTrue(addOffsetParametersFlag);
        assertTrue(addRowCountParameterFlag);
    }
}
