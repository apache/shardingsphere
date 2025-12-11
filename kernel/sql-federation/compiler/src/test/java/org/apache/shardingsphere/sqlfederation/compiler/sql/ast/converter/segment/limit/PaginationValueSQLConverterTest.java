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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.limit;

import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationValueSQLConverterTest {
    
    @Test
    void assertConvertReturnsLiteralSqlNodeForNumberLiteral() {
        Optional<SqlNode> actual = PaginationValueSQLConverter.convert(new NumberLiteralLimitValueSegment(0, 0, 5L));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(SqlLiteral.class));
        assertThat(((SqlLiteral) actual.get()).toValue(), is("5"));
    }
    
    @Test
    void assertConvertReturnsDynamicParamForParameterMarker() {
        Optional<SqlNode> actual = PaginationValueSQLConverter.convert(new ParameterMarkerLimitValueSegment(0, 0, 2));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(SqlDynamicParam.class));
        assertThat(((SqlDynamicParam) actual.get()).getIndex(), is(2));
    }
}
