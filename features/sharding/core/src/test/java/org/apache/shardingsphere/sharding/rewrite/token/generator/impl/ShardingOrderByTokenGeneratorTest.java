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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingOrderByTokenGeneratorTest {
    
    private static final String TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL = "TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL";
    
    private static final String TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL = "TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL";
    
    private static final int TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX = 5;
    
    private final ShardingOrderByTokenGenerator generator = new ShardingOrderByTokenGenerator();
    
    @Mock
    private OrderDirection orderDirection;
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotGeneratedOrderByContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithGeneratedOrderByContext() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getOrderByContext().isGenerated()).thenReturn(true);
        assertTrue(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokenWithWindow() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getWindow()).thenReturn(Optional.of(new WindowSegment(0, 10)));
        SelectStatementContext selectStatementContext = mockSelectStatementContext(selectStatement);
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(actual.getOrderDirections().get(0), is(orderDirection));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithHaving() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getHaving()).thenReturn(Optional.of(new HavingSegment(0, 10, mock())));
        SelectStatementContext selectStatementContext = mockSelectStatementContext(selectStatement);
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(actual.getOrderDirections().get(0), is(orderDirection));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithGroupBy() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getGroupBy()).thenReturn(Optional.of(new GroupBySegment(0, 10, Collections.emptyList())));
        SelectStatementContext selectStatementContext = mockSelectStatementContext(selectStatement);
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(actual.getOrderDirections().get(0), is(orderDirection));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithWhere() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 10, mock())));
        SelectStatementContext selectStatementContext = mockSelectStatementContext(selectStatement);
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(actual.getOrderDirections().get(0), is(orderDirection));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithNothing() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectStatementContext selectStatementContext = mockSelectStatementContext(selectStatement);
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(actual.getOrderDirections().get(0), is(orderDirection));
        assertThat(actual.getStopIndex(), is(1));
    }
    
    private SelectStatementContext mockSelectStatementContext(final SelectStatement selectStatement) {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(selectStatement);
        Collection<OrderByItem> orderByItems = createOrderByItems();
        when(result.getOrderByContext().getItems()).thenReturn(orderByItems);
        return result;
    }
    
    private Collection<OrderByItem> createOrderByItems() {
        ColumnOrderByItemSegment columnOrderByItemSegment = mock(ColumnOrderByItemSegment.class);
        when(columnOrderByItemSegment.getText()).thenReturn(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL);
        when(columnOrderByItemSegment.getOrderDirection()).thenReturn(orderDirection);
        OrderByItem columnOrderByItem = mock(OrderByItem.class);
        when(columnOrderByItem.getSegment()).thenReturn(columnOrderByItemSegment);
        ExpressionOrderByItemSegment expressionOrderByItemSegment = mock(ExpressionOrderByItemSegment.class);
        when(expressionOrderByItemSegment.getText()).thenReturn(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL);
        when(expressionOrderByItemSegment.getOrderDirection()).thenReturn(orderDirection);
        OrderByItem expressionOrderByItem = mock(OrderByItem.class);
        when(expressionOrderByItem.getSegment()).thenReturn(expressionOrderByItemSegment);
        OrderByItemSegment orderByItemSegment = mock(OrderByItemSegment.class);
        when(orderByItemSegment.getOrderDirection()).thenReturn(orderDirection);
        OrderByItem otherClassOrderByItem = mock(OrderByItem.class);
        when(otherClassOrderByItem.getSegment()).thenReturn(orderByItemSegment);
        when(otherClassOrderByItem.getIndex()).thenReturn(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX);
        return Arrays.asList(columnOrderByItem, expressionOrderByItem, otherClassOrderByItem);
    }
}
