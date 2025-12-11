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

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ColumnConverter.class)
class ColumnOrderByItemConverterTest {
    
    @Test
    void assertConvertReturnsEmptyWhenColumnConverterReturnsEmpty() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        when(ColumnConverter.convert(columnSegment)).thenReturn(Optional.empty());
        assertFalse(ColumnOrderByItemConverter.convert(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, null)).isPresent());
    }
    
    @Test
    void assertConvertReturnsOriginalNodeWhenAscAndNullsAbsent() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        SqlNode expectedNode = mock(SqlNode.class);
        when(ColumnConverter.convert(columnSegment)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = ColumnOrderByItemConverter.convert(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, null));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expectedNode));
    }
    
    @Test
    void assertConvertWrapsDescAndNullsFirst() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        SqlNode expectedNode = mock(SqlNode.class);
        when(ColumnConverter.convert(columnSegment)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = ColumnOrderByItemConverter.convert(new ColumnOrderByItemSegment(columnSegment, OrderDirection.DESC, NullsOrderType.FIRST));
        assertTrue(actual.isPresent());
        SqlBasicCall nullsFirstCall = (SqlBasicCall) actual.get();
        assertThat(nullsFirstCall.getOperator(), is(SqlStdOperatorTable.NULLS_FIRST));
        SqlBasicCall descCall = (SqlBasicCall) nullsFirstCall.getOperandList().get(0);
        assertThat(descCall.getOperator(), is(SqlStdOperatorTable.DESC));
        assertThat(descCall.getOperandList().get(0), is(expectedNode));
    }
    
    @Test
    void assertConvertWrapsNullsLastWithoutDesc() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        ColumnOrderByItemSegment segment = new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.LAST);
        SqlNode expectedNode = mock(SqlNode.class);
        when(ColumnConverter.convert(columnSegment)).thenReturn(Optional.of(expectedNode));
        Optional<SqlNode> actual = ColumnOrderByItemConverter.convert(segment);
        assertTrue(actual.isPresent());
        SqlBasicCall nullsLastCall = (SqlBasicCall) actual.get();
        assertThat(nullsLastCall, isA(SqlBasicCall.class));
        assertThat(nullsLastCall.getOperator(), is(SqlStdOperatorTable.NULLS_LAST));
        assertThat(nullsLastCall.getOperandList().get(0), is(expectedNode));
    }
}
