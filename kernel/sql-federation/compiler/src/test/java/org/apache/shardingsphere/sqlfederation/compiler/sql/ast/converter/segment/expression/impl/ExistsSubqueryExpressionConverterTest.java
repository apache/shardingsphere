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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type.SelectStatementConverter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class ExistsSubqueryExpressionConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertReturnsEmptyForNullExpression() {
        assertFalse(ExistsSubqueryExpressionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertExistsExpression() {
        SqlNode expected = mock(SqlNode.class);
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(any(SelectStatement.class))).thenReturn(expected))) {
            ExistsSubqueryExpression expression = new ExistsSubqueryExpression(0, 0, new SubquerySegment(0, 0, new SelectStatement(databaseType), "text"));
            SqlBasicCall actual = (SqlBasicCall) ExistsSubqueryExpressionConverter.convert(expression).orElse(null);
            assertNotNull(actual);
            assertThat(actual.getOperator(), is(SqlStdOperatorTable.EXISTS));
            assertThat(actual.getOperandList(), is(Collections.singletonList(expected)));
        }
    }
    
    @Test
    void assertConvertNotExistsExpression() {
        SqlNode expected = mock(SqlNode.class);
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(any(SelectStatement.class))).thenReturn(expected))) {
            ExistsSubqueryExpression expression = new ExistsSubqueryExpression(0, 0, new SubquerySegment(0, 0, new SelectStatement(databaseType), "text"));
            expression.setNot(true);
            SqlBasicCall actual = (SqlBasicCall) ExistsSubqueryExpressionConverter.convert(expression).orElse(null);
            assertNotNull(actual);
            assertThat(actual.getOperator(), is(SqlStdOperatorTable.NOT));
            SqlBasicCall existsCall = (SqlBasicCall) actual.getOperandList().get(0);
            assertThat(existsCall.getOperator(), is(SqlStdOperatorTable.EXISTS));
            assertThat(existsCall.getOperandList(), is(Collections.singletonList(expected)));
        }
    }
}
