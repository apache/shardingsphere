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
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.plsql.RoutineNameAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedProcedureCallNameSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.trigger.CreateTriggerStatementTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create trigger statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateTriggerStatementAssert {
    
    /**
     * Assert create trigger statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create trigger statement
     * @param expected expected create trigger statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateTriggerStatement actual, final CreateTriggerStatementTestCase expected) {
        if (null != expected.getTriggerName()) {
            assertTrue(actual.getTriggerName().isPresent(), assertContext.getText("Actual trigger name should exist."));
            RoutineNameAssert.assertIs(assertContext, actual.getTriggerName().get(), expected.getTriggerName());
        }
        if (null != expected.getProcedureCalls()) {
            assertProcedureCallNames(assertContext, actual.getProcedureCallNames(), expected.getProcedureCalls());
        }
        if (null != expected.getSqlStatementCount()) {
            assertThat(assertContext.getText("Trigger SQL statements size assertion error: "), actual.getSqlStatements().size(), is(expected.getSqlStatementCount()));
        }
        if (null != expected.getDisableClauseStartIndex()) {
            assertThat(assertContext.getText("Trigger disable clause start index assertion error: "), actual.getDisableClauseStartIndex(), is(expected.getDisableClauseStartIndex()));
        }
        if (!expected.getTables().isEmpty()) {
            TableAssert.assertIs(assertContext, actual.getTables(), expected.getTables());
        }
        if (!expected.getPseudoColumns().isEmpty()) {
            ColumnAssert.assertIs(assertContext, actual.getTriggerPseudoColumns(), expected.getPseudoColumns());
        }
    }
    
    private static void assertProcedureCallNames(final SQLCaseAssertContext assertContext, final List<ProcedureCallNameSegment> actual,
                                                 final List<ExpectedProcedureCallNameSegment> expectedProcedureCallSegments) {
        assertThat(assertContext.getText("Procedure call names size mismatched: "), actual.size(), is(expectedProcedureCallSegments.size()));
        List<ProcedureCallNameSegment> actualSegments = new ArrayList<>(actual);
        List<ExpectedProcedureCallNameSegment> expectedSegments = new ArrayList<>(expectedProcedureCallSegments);
        for (int i = 0; i < actualSegments.size(); i++) {
            assertThat(assertContext.getText("Procedure call name mismatched:"), actualSegments.get(i).toString(), is(expectedSegments.get(i).getName()));
        }
    }
}
