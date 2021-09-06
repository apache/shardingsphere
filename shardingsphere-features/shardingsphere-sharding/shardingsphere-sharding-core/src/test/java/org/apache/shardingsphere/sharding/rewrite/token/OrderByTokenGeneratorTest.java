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

import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.OrderByTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OrderByTokenGeneratorTest {

    private static final int TEST_STOP_INDEX = 2;

    private static final String TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL = "TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL";

    private static final String TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL = "TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL";

    private static final int TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX = 5;

    private OrderDirection orderDirection = mock(OrderDirection.class);

    @Test
    public void assertIsGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        OrderByTokenGenerator orderByTokenGenerator = new OrderByTokenGenerator();
        assertFalse(orderByTokenGenerator.isGenerateSQLToken(insertStatementContext));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getOrderByContext().isGenerated()).thenReturn(Boolean.FALSE);
        assertFalse(orderByTokenGenerator.isGenerateSQLToken(selectStatementContext));
        when(selectStatementContext.getOrderByContext().isGenerated()).thenReturn(Boolean.TRUE);
        assertTrue(orderByTokenGenerator.isGenerateSQLToken(selectStatementContext));
    }

    @Test
    public void assertGenerateSQLToken() {
        WindowSegment windowSegment = mock(WindowSegment.class);
        when(windowSegment.getStopIndex()).thenReturn(TEST_STOP_INDEX);
        MySQLSelectStatement mySQLSelectStatement = mock(MySQLSelectStatement.class);
        when(mySQLSelectStatement.getWindow()).thenReturn(Optional.of(windowSegment));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(mySQLSelectStatement);
        Collection<OrderByItem> orderByItemCollection = getOrderByItemCollection();
        when(selectStatementContext.getOrderByContext().getItems()).thenReturn(orderByItemCollection);
        OrderByTokenGenerator orderByTokenGenerator = new OrderByTokenGenerator();
        OrderByToken orderByToken = orderByTokenGenerator.generateSQLToken(selectStatementContext);
        assertThat(orderByToken.getColumnLabels().get(0), is(TEST_COLUMN_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(orderByToken.getColumnLabels().get(1), is(TEST_EXPRESSION_ORDER_BY_ITEM_SEGMENT_COLUMN_LABEL));
        assertThat(orderByToken.getColumnLabels().get(2), is(String.valueOf(TEST_OTHER_CLASS_ORDER_BY_ITEM_INDEX)));
        assertThat(orderByToken.getOrderDirections().get(0), is(orderDirection));
    }

    private Collection<OrderByItem> getOrderByItemCollection() {
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
        Collection<OrderByItem> result = new LinkedList<>();
        result.add(columnOrderByItem);
        result.add(expressionOrderByItem);
        result.add(otherClassOrderByItem);
        return result;
    }
}
