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
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionConverter.class)
class NotExpressionConverterTest {
    
    @Test
    void assertConvertNotSignWithBinaryExpression() {
        LiteralExpressionSegment expression = new LiteralExpressionSegment(0, 0, 1);
        SqlNode expressionNode = SqlStdOperatorTable.MINUS.createCall(SqlParserPos.ZERO, new SqlIdentifier("order_id", SqlParserPos.ZERO), SqlLiteral.createExactNumeric("1000", SqlParserPos.ZERO));
        when(ExpressionConverter.convert(expression)).thenReturn(Optional.of(expressionNode));
        SqlBasicCall actual = NotExpressionConverter.convert(new NotExpression(0, 0, expression, true));
        assertThat(actual.toSqlString(config -> config.withQuoteAllIdentifiers(false)).getSql(), is("! (order_id - 1000)"));
    }
    
    @Test
    void assertConvertNotSignWithSimpleExpression() {
        LiteralExpressionSegment expression = new LiteralExpressionSegment(0, 0, 1);
        SqlNode expressionNode = new SqlIdentifier("order_id", SqlParserPos.ZERO);
        when(ExpressionConverter.convert(expression)).thenReturn(Optional.of(expressionNode));
        SqlBasicCall actual = NotExpressionConverter.convert(new NotExpression(0, 0, expression, true));
        assertThat(actual.toSqlString(config -> config.withQuoteAllIdentifiers(false)).getSql(), is("! order_id"));
    }
    
    @Test
    void assertConvertNotWithBinaryExpression() {
        LiteralExpressionSegment expression = new LiteralExpressionSegment(0, 0, 1);
        SqlNode expressionNode = SqlStdOperatorTable.MINUS.createCall(SqlParserPos.ZERO, new SqlIdentifier("order_id", SqlParserPos.ZERO), SqlLiteral.createExactNumeric("1000", SqlParserPos.ZERO));
        when(ExpressionConverter.convert(expression)).thenReturn(Optional.of(expressionNode));
        SqlBasicCall actual = NotExpressionConverter.convert(new NotExpression(0, 0, expression, false));
        assertThat(actual.toSqlString(config -> config.withQuoteAllIdentifiers(false)).getSql(), is("NOT order_id - 1000"));
    }
}
