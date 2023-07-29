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

import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.OrderByTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderByTokenGeneratorTest {
    
    private static final String TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL = "TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL";
    
    private static final String TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL = "TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL";
    
    private static final int TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX = 5;
    
    @Mock
    private OrderDirection orderDirection;
    
    @Test
    void assertIsGenerateSQLToken() {
        OrderByTokenGenerator generator = new OrderByTokenGenerator();
        assertFalse(generator.isGenerateSQLToken(mock(InsertStatementContext.class)));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getOrderByContext().isGenerated()).thenReturn(Boolean.FALSE);
        assertFalse(generator.isGenerateSQLToken(selectStatementContext));
        when(selectStatementContext.getOrderByContext().isGenerated()).thenReturn(Boolean.TRUE);
        assertTrue(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertGenerateSQLToken() {
        WindowSegment windowSegment = mock(WindowSegment.class);
        when(windowSegment.getStopIndex()).thenReturn(2);
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getWindow()).thenReturn(Optional.of(windowSegment));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Collection<OrderByItem> orderByItems = getOrderByItems();
        when(selectStatementContext.getOrderByContext().getItems()).thenReturn(orderByItems);
        OrderByTokenGenerator generator = new OrderByTokenGenerator();
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(actual.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(actual.getOrderDirections().get(0), is(orderDirection));
    }
    
    private Collection<OrderByItem> getOrderByItems() {
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
