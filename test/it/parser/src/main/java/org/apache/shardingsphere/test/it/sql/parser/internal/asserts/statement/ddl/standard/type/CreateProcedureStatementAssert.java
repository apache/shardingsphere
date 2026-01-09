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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedSQLStatementSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.plsql.CreateProcedureTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Create procedure statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateProcedureStatementAssert {
    
    /**
     * Assert create procedure statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create procedure statement
     * @param expected expected create procedure statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateProcedureStatement actual, final CreateProcedureTestCase expected) {
        assertSQLStatements(assertContext, actual, expected.getSqlStatements());
    }
    
    private static void assertSQLStatements(final SQLCaseAssertContext assertContext, final CreateProcedureStatement actual, final List<ExpectedSQLStatementSegment> expectedSQLStatementSegments) {
        List<SQLStatementSegment> actualSegments = getActualSQLStatementSegments(actual);
        if (actualSegments.size() != expectedSQLStatementSegments.size()) {
            return;
        }
        assertThat(assertContext.getText("SQL statements size mismatched: "), actualSegments.size(), is(expectedSQLStatementSegments.size()));
        for (int i = 0; i < actualSegments.size(); i++) {
            SQLStatementSegment actualSegment = actualSegments.get(i);
            ExpectedSQLStatementSegment expectedSegment = expectedSQLStatementSegments.get(i);
            assertThat(assertContext.getText("Start index mismatched:"), actualSegment.getStartIndex(), is(expectedSegment.getStartIndex()));
            assertThat(assertContext.getText("Stop index mismatched:"), actualSegment.getStopIndex(), is(expectedSegment.getStopIndex()));
            assertThat(assertContext.getText("SQL statement mismatched:"), actualSegment.getSqlStatement().getClass().getSimpleName(), is(expectedSegment.getStatementClassSimpleName()));
        }
    }
    
    private static List<SQLStatementSegment> getActualSQLStatementSegments(final CreateProcedureStatement actual) {
        if (!actual.getSqlStatements().isEmpty()) {
            return actual.getSqlStatements();
        }
        if (!actual.getRoutineBody().isPresent()) {
            return new ArrayList<>();
        }
        Collection<ValidStatementSegment> validStatements = actual.getRoutineBody().get().getValidStatements();
        List<SQLStatementSegment> result = new ArrayList<>(validStatements.size());
        for (ValidStatementSegment each : validStatements) {
            if (null != each.getSqlStatement()) {
                result.add(new SQLStatementSegment(each.getStartIndex(), each.getStopIndex(), each.getSqlStatement()));
            }
        }
        result.sort(Comparator.comparingInt(SQLStatementSegment::getStartIndex));
        return result;
    }
}
