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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.item;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ColumnOrderByItemConverter.class, ExpressionOrderByItemConverter.class, IndexOrderByItemConverter.class})
class OrderByItemConverterUtilsTest {
    
    @Test
    void assertConvertCoversSupportedBranches() {
        ColumnOrderByItemSegment columnItem = new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("col")), OrderDirection.ASC, null);
        when(ColumnOrderByItemConverter.convert(columnItem)).thenReturn(Optional.empty());
        ExpressionOrderByItemSegment expressionItem = new ExpressionOrderByItemSegment(0, 0, "expr", OrderDirection.ASC, null, mock(ExpressionSegment.class));
        SqlNode expectedExpressionNode = mock(SqlNode.class);
        when(ExpressionOrderByItemConverter.convert(expressionItem)).thenReturn(Optional.of(expectedExpressionNode));
        IndexOrderByItemSegment indexItem = new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, null);
        SqlNode expectedIndexNode = mock(SqlNode.class);
        when(IndexOrderByItemConverter.convert(indexItem)).thenReturn(Optional.of(expectedIndexNode));
        OrderByItemSegment unsupportedItem = new OrderByItemSegment(0, 0, OrderDirection.ASC, null) {
        };
        Collection<OrderByItemSegment> orderByItems = new ArrayList<>(4);
        orderByItems.add(columnItem);
        orderByItems.add(expressionItem);
        orderByItems.add(indexItem);
        orderByItems.add(unsupportedItem);
        Collection<SqlNode> actual = OrderByItemConverterUtils.convert(orderByItems);
        assertThat(actual.size(), is(2));
        Iterator<SqlNode> iterator = actual.iterator();
        assertThat(iterator.next(), is(expectedExpressionNode));
        assertThat(iterator.next(), is(expectedIndexNode));
    }
    
    @Test
    void assertConvertThrowsForTextOrderByItem() {
        TextOrderByItemSegment textOrderByItemSegment = new TextOrderByItemSegment(0, 0, OrderDirection.ASC, NullsOrderType.FIRST) {
            
            @Override
            public String getText() {
                return "text";
            }
        };
        UnsupportedSQLOperationException ex = assertThrows(UnsupportedSQLOperationException.class, () -> OrderByItemConverterUtils.convert(Collections.singleton(textOrderByItemSegment)));
        assertThat(ex.getMessage(), is("Unsupported SQL operation: unsupported TextOrderByItemSegment."));
    }
}
