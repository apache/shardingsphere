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

package org.apache.shardingsphere.shadow.route.determiner;

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.exception.syntax.UnsupportedShadowInsertValueException;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.column.PreciseColumnShadowValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowInsertValueRouteDeciderTest {
    
    @Test
    void assertDecide() {
        ShadowRule rule = mockShadowRule();
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(sqlStatementContext.getInsertColumnNames()).thenReturn(Arrays.asList("order_id", "user_id"));
        List<InsertValueContext> insertValueContexts = Arrays.asList(createInsertValueContext(1, 0), createInsertValueContext(2, 1), createInsertValueContext(3, 0));
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(insertValueContexts);
        List<Boolean> actual = new ShadowInsertValueRouteDecider(rule, sqlStatementContext).decide();
        assertThat(actual, is(Arrays.asList(false, true, false)));
    }
    
    @Test
    void assertDecideWithoutShadowColumn() {
        ShadowRule rule = mockShadowRule();
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(sqlStatementContext.getInsertColumnNames()).thenReturn(Collections.singletonList("order_id"));
        InsertValueContext insertValueContext = createInsertValueContext(1, 0);
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        List<Boolean> actual = new ShadowInsertValueRouteDecider(rule, sqlStatementContext).decide();
        assertThat(actual, is(Collections.singletonList(false)));
    }
    
    @Test
    void assertDecideWithUnsupportedInsertValue() {
        ShadowRule rule = mockShadowRule();
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(sqlStatementContext.getInsertColumnNames()).thenReturn(Arrays.asList("order_id", "user_id"));
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        when(insertValueContext.getLiteralValue(1)).thenReturn(Optional.empty());
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        ShadowInsertValueRouteDecider decider = new ShadowInsertValueRouteDecider(rule, sqlStatementContext);
        assertThrows(UnsupportedShadowInsertValueException.class, decider::decide);
    }
    
    @SuppressWarnings("unchecked")
    private ShadowRule mockShadowRule() {
        ShadowRule result = mock(ShadowRule.class);
        when(result.getShadowColumnNames(ShadowOperationType.INSERT, "t_order")).thenReturn(Collections.singletonList("user_id"));
        ColumnShadowAlgorithm<Comparable<?>> algorithm = mock(ColumnShadowAlgorithm.class);
        when(algorithm.isShadow(any())).thenAnswer(invocation -> {
            PreciseColumnShadowValue<Comparable<?>> shadowValue = invocation.getArgument(0);
            return Integer.valueOf(1).equals(shadowValue.getValue());
        });
        when(result.getColumnShadowAlgorithms(ShadowOperationType.INSERT, "t_order", "user_id")).thenReturn(Collections.singletonList(algorithm));
        return result;
    }
    
    private InsertValueContext createInsertValueContext(final Object orderId, final Object userId) {
        InsertValueContext result = mock(InsertValueContext.class);
        when(result.getLiteralValue(0)).thenReturn(Optional.of(orderId));
        when(result.getLiteralValue(1)).thenReturn(Optional.of(userId));
        return result;
    }
}
