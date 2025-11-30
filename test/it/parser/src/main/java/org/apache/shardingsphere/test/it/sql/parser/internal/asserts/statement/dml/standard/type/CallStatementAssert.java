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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.call.ExpectedCallParameter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.CallStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Call statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CallStatementAssert {
    
    /**
     * Assert call statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual call statement
     * @param expected expected call statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CallStatement actual, final CallStatementTestCase expected) {
        assertProcedureName(assertContext, actual, expected);
        assertProcedureParameters(assertContext, actual, expected);
    }
    
    private static void assertProcedureName(final SQLCaseAssertContext assertContext, final CallStatement actual, final CallStatementTestCase expected) {
        assertThat(assertContext.getText("Procedure name assertion error: "), actual.getProcedureName(), is(expected.getProcedureName().getName()));
    }
    
    private static void assertProcedureParameters(final SQLCaseAssertContext assertContext, final CallStatement actual, final CallStatementTestCase expected) {
        assertThat(assertContext.getText("Procedure parameters assertion error: "), actual.getParameters().size(), is(expected.getProcedureParameters().size()));
        int count = 0;
        for (ExpressionSegment each : actual.getParameters()) {
            assertParameter(assertContext, each, expected.getProcedureParameters().get(count));
            count++;
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
}
