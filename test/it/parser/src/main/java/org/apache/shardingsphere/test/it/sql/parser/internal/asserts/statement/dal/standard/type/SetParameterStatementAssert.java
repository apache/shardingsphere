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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.assignment.ExpectedVariable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.SetParameterStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Set parameter statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SetParameterStatementAssert {
    
    /**
     * Assert set parameter statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual set parameter statement
     * @param expected expected parameter statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SetStatement actual, final SetParameterStatementTestCase expected) {
        assertThat(assertContext.getText("variableAssign size assertion error: "), actual.getVariableAssigns().size(), is(expected.getValueAssigns().size()));
        if (!expected.getValueAssigns().isEmpty()) {
            for (int i = 0; i < expected.getValueAssigns().size(); i++) {
                assertVariable(assertContext, actual.getVariableAssigns().get(i).getVariable(), expected.getValueAssigns().get(i).getParameter());
                assertThat(assertContext.getText("variableAssign assert error."), actual.getVariableAssigns().get(i).getAssignValue(), is(expected.getValueAssigns().get(i).getValue()));
            }
        }
    }
    
    private static void assertVariable(final SQLCaseAssertContext assertContext, final VariableSegment actual, final ExpectedVariable expected) {
        assertThat(assertContext.getText("variable assertion error: "), actual.getVariable(), is(expected.getName()));
        assertThat(assertContext.getText("scope assertion error: "), actual.getScope().orElse(null), is(expected.getScope()));
    }
}
