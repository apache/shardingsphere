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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLCallStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.call.ExpectedCallParameter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.CallStatementTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Call statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CallStatementAssert {

    /**
     * Assert call statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual call statement
     * @param expected      expected call statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CallStatement actual, final CallStatementTestCase expected) {
        assertProcedureName(assertContext, actual, expected);
        assertProcedureParameters(assertContext, actual, expected);
    }

    private static void assertProcedureParameters(final SQLCaseAssertContext assertContext, final CallStatement actual, final CallStatementTestCase expected) {
        if (actual instanceof MySQLCallStatement) {
            MySQLCallStatement actualStatement = (MySQLCallStatement) actual;
            if (actualStatement.getParameters() != null && expected.getProcedureParameters() != null) {
                assertThat(assertContext.getText("Procedure parameters assertion error: "), actualStatement.getParameters().size(), equalTo(expected.getProcedureParameters().getParameters().size()));
                int count = 0;
                for (ExpressionSegment each : actualStatement.getParameters()) {
                    assertParameter(assertContext, each, expected.getProcedureParameters().getParameters().get(count));
                    count++;
                }
            }
        }
    }

    private static void assertParameter(final SQLCaseAssertContext assertContext, final ExpressionSegment actual, final ExpectedCallParameter expected) {
        if (actual instanceof ParameterMarkerExpressionSegment) {
            ExpressionAssert.assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) actual, expected.getParameterMarkerExpression());
        } else if (actual instanceof LiteralExpressionSegment) {
            ExpressionAssert.assertLiteralExpression(assertContext, (LiteralExpressionSegment) actual, expected.getLiteralExpression());
        } else if (actual instanceof CommonExpressionSegment) {
            ExpressionAssert.assertCommonExpression(assertContext, (CommonExpressionSegment) actual, expected.getCommonExpression());
        }
    }

    private static void assertProcedureName(final SQLCaseAssertContext assertContext, final CallStatement actual, final CallStatementTestCase expected) {
        if (actual instanceof MySQLCallStatement) {
            assertThat(assertContext.getText("Procedure name assertion error: "), ((MySQLCallStatement) actual).getProcedureName(), equalTo(expected.getProcedureName().getName()));
        }
    }
}
