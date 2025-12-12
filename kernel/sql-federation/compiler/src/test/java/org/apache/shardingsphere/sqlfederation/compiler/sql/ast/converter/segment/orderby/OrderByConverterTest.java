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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.item.OrderByItemConverterUtils;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(OrderByItemConverterUtils.class)
class OrderByConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(OrderByConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertReturnsSqlNodeList() {
        Collection<OrderByItemSegment> orderByItems = new ArrayList<>(Collections.singletonList(mock(OrderByItemSegment.class)));
        SqlNode expectedNode = mock(SqlNode.class);
        when(OrderByItemConverterUtils.convert(orderByItems)).thenReturn(Collections.singleton(expectedNode));
        Optional<SqlNodeList> actual = OrderByConverter.convert(new OrderBySegment(0, 0, orderByItems));
        assertTrue(actual.isPresent());
        assertThat(actual.get().size(), is(1));
        assertThat(actual.get().get(0), is(expectedNode));
    }
}
