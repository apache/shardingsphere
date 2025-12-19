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
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexOrderByItemConverterTest {
    
    @Test
    void assertConvertReturnsLiteralForAscWithoutNulls() {
        Optional<SqlNode> actual = IndexOrderByItemConverter.convert(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, null));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(SqlLiteral.class));
        assertThat(((SqlLiteral) actual.get()).toValue(), is("2"));
    }
    
    @Test
    void assertConvertWrapsDescWithNullsFirst() {
        Optional<SqlNode> actual = IndexOrderByItemConverter.convert(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST));
        assertTrue(actual.isPresent());
        SqlBasicCall nullsFirstCall = (SqlBasicCall) actual.get();
        assertThat(nullsFirstCall.getOperator(), is(SqlStdOperatorTable.NULLS_FIRST));
        SqlBasicCall descCall = (SqlBasicCall) nullsFirstCall.getOperandList().get(0);
        assertThat(descCall.getOperator(), is(SqlStdOperatorTable.DESC));
        assertThat(((SqlLiteral) descCall.getOperandList().get(0)).toValue(), is("3"));
    }
    
    @Test
    void assertConvertWrapsNullsLastWithoutDesc() {
        Optional<SqlNode> actual = IndexOrderByItemConverter.convert(new IndexOrderByItemSegment(0, 0, 4, OrderDirection.ASC, NullsOrderType.LAST));
        assertTrue(actual.isPresent());
        SqlBasicCall nullsLastCall = (SqlBasicCall) actual.get();
        assertThat(nullsLastCall.getOperator(), is(SqlStdOperatorTable.NULLS_LAST));
        assertThat(((SqlLiteral) nullsLastCall.getOperandList().get(0)).toValue(), is("4"));
    }
}
