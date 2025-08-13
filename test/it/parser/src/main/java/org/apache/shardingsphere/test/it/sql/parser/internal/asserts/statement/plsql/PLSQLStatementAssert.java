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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.plsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureBodyEndNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.function.OracleCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.ddl.procedure.OracleCreateProcedureStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedDynamicSqlStatementExpressionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedProcedureBodyEndNameSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedProcedureCallNameSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedSQLStatementSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.plsql.CreateFunctionTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.plsql.CreateProcedureTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PL/SQL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PLSQLStatementAssert {
    
    /**
     * Assert PL/SQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual PL/SQL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof OracleCreateProcedureStatement && expected instanceof CreateProcedureTestCase) {
            OracleCreateProcedureStatement actualStatement = (OracleCreateProcedureStatement) actual;
            CreateProcedureTestCase expectedTestCase = (CreateProcedureTestCase) expected;
            assertProcedureName(assertContext, actualStatement, expectedTestCase);
            assertSQLStatements(assertContext, actualStatement.getSqlStatements(), expectedTestCase.getSqlStatements());
            assertProcedureCallNames(assertContext, actualStatement.getProcedureCallNames(), expectedTestCase.getProcedureCalls());
            assertDynamicSqlStatementExpressions(assertContext, actualStatement.getDynamicSqlStatementExpressions(), expectedTestCase.getDynamicSqlStatementExpressions());
        }
        if (actual instanceof OracleCreateFunctionStatement && expected instanceof CreateFunctionTestCase) {
            OracleCreateFunctionStatement actualStatement = (OracleCreateFunctionStatement) actual;
            CreateFunctionTestCase expectedTestCase = (CreateFunctionTestCase) expected;
            assertFunctionName(assertContext, actualStatement, expectedTestCase);
            assertSQLStatements(assertContext, actualStatement.getSqlStatements(), expectedTestCase.getSqlStatements());
            assertProcedureCallNames(assertContext, actualStatement.getProcedureCallNames(), expectedTestCase.getProcedureCalls());
            assertDynamicSqlStatementExpressions(assertContext, actualStatement.getDynamicSqlStatementExpressions(), expectedTestCase.getDynamicSqlStatementExpressions());
        }
    }
    
    private static void assertProcedureName(final SQLCaseAssertContext assertContext, final OracleCreateProcedureStatement actual, final CreateProcedureTestCase expected) {
        if (null == expected.getProcedureName()) {
            assertFalse(actual.getProcedureName().isPresent(), assertContext.getText("Procedure name should not be exist."));
        } else {
            assertTrue(actual.getProcedureName().isPresent(), assertContext.getText("Procedure name should be exist."));
            assertThat(assertContext.getText("Procedure name mismatched:"), actual.getProcedureName().get().getIdentifier().getValue(), is(expected.getProcedureName().getName()));
        }
        if (null == expected.getProcedureBodyEndNameSegments()) {
            assertThat(assertContext.getText("Procedure body end names size mismatched:"), actual.getProcedureBodyEndNameSegments().isEmpty());
        } else {
            assertThat(assertContext.getText("Procedure body end names size mismatched:"), actual.getProcedureBodyEndNameSegments().size(), is(expected.getProcedureBodyEndNameSegments().size()));
            for (int i = 0; i < expected.getProcedureBodyEndNameSegments().size(); i++) {
                ProcedureBodyEndNameSegment actualSegment = actual.getProcedureBodyEndNameSegments().get(i);
                ExpectedProcedureBodyEndNameSegment expectedSegment = expected.getProcedureBodyEndNameSegments().get(i);
                assertThat(assertContext.getText("Procedure body end name mismatched:"), actualSegment.toString(), is(expectedSegment.getName()));
            }
        }
    }
    
    private static void assertFunctionName(final SQLCaseAssertContext assertContext, final OracleCreateFunctionStatement actual, final CreateFunctionTestCase expected) {
        if (null == expected.getFunctionName()) {
            assertFalse(actual.getFunctionName().isPresent(), assertContext.getText("Function name should not be exist."));
        } else {
            assertTrue(actual.getFunctionName().isPresent(), assertContext.getText("Function name should be exist."));
            assertThat(assertContext.getText("Function name mismatched:"), actual.getFunctionName().get().getIdentifier().getValue(), is(expected.getFunctionName().getName()));
        }
    }
    
    private static void assertSQLStatements(final SQLCaseAssertContext assertContext, final List<SQLStatementSegment> actual, final List<ExpectedSQLStatementSegment> expectedSQLStatementSegments) {
        assertThat(assertContext.getText("SQL statements size mismatched: "), actual.size(), is(expectedSQLStatementSegments.size()));
        List<SQLStatementSegment> actualSegments = new ArrayList<>(actual);
        List<ExpectedSQLStatementSegment> expectedSegments = new ArrayList<>(expectedSQLStatementSegments);
        for (int i = 0; i < actualSegments.size(); i++) {
            SQLStatementSegment actualSegment = actualSegments.get(i);
            ExpectedSQLStatementSegment expectedSegment = expectedSegments.get(i);
            assertThat(assertContext.getText("Start index mismatched:"), actualSegment.getStartIndex(), is(expectedSegment.getStartIndex()));
            assertThat(assertContext.getText("End index mismatched:"), actualSegment.getStopIndex(), is(expectedSegment.getStopIndex()));
            assertThat(assertContext.getText("SQL statement mismatched:"), actualSegment.getSqlStatement().getClass().getSimpleName(), is(expectedSegment.getStatementClassSimpleName()));
        }
    }
    
    private static void assertProcedureCallNames(final SQLCaseAssertContext assertContext, final List<ProcedureCallNameSegment> actual,
                                                 final List<ExpectedProcedureCallNameSegment> expectedProcedureCallSegments) {
        assertThat(assertContext.getText("Procedure call names size mismatched: "), actual.size(), is(expectedProcedureCallSegments.size()));
        List<ProcedureCallNameSegment> actualSegments = new ArrayList<>(actual);
        List<ExpectedProcedureCallNameSegment> expectedSegments = new ArrayList<>(expectedProcedureCallSegments);
        for (int i = 0; i < actualSegments.size(); i++) {
            ProcedureCallNameSegment actualSegment = actualSegments.get(i);
            ExpectedProcedureCallNameSegment expectedSegment = expectedSegments.get(i);
            assertThat(assertContext.getText("Procedure call name mismatched:"), actualSegment.toString(), is(expectedSegment.getName()));
        }
    }
    
    private static void assertDynamicSqlStatementExpressions(final SQLCaseAssertContext assertContext, final List<ExpressionSegment> actual,
                                                             final List<ExpectedDynamicSqlStatementExpressionSegment> expectedExpressionSegments) {
        assertThat(assertContext.getText("Dynamic SQL statement expressions size mismatched: "), actual.size(), is(expectedExpressionSegments.size()));
        List<ExpressionSegment> actualSegments = new ArrayList<>(actual);
        List<ExpectedDynamicSqlStatementExpressionSegment> expectedSegments = new ArrayList<>(expectedExpressionSegments);
        for (int i = 0; i < actualSegments.size(); i++) {
            ExpressionSegment actualSegment = actualSegments.get(i);
            ExpectedDynamicSqlStatementExpressionSegment expectedSegment = expectedSegments.get(i);
            assertThat(assertContext.getText("Dynamic SQL statement start index mismatched: "), actualSegment.getStartIndex(), is(expectedSegment.getStartIndex()));
            assertThat(assertContext.getText("Dynamic SQL statement stop index mismatched: "), actualSegment.getStopIndex(), is(expectedSegment.getStopIndex()));
        }
    }
}
