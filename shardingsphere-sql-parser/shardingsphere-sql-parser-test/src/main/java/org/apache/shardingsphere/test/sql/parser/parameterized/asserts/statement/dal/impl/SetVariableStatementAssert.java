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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.assignment.ExpectedVariable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.SetVariableStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Set variable statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SetVariableStatementAssert {
    
    /**
     * Assert set variable statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual set variable statement
     * @param expected expected variable statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SetStatement actual, final SetVariableStatementTestCase expected) {
        assertThat(assertContext.getText("variableAssign size assertion error: "), actual.getVariableAssigns().size(), is(expected.getValueAssigns().size()));
        if (!expected.getValueAssigns().isEmpty()) {
            for (int i = 0; i < expected.getValueAssigns().size(); i++) {
                assertVariable(assertContext, actual.getVariableAssigns().get(i).getVariable(), expected.getValueAssigns().get(i).getVariable());
                assertThat(assertContext.getText("variableAssign assert error."), actual.getVariableAssigns().get(i).getAssignValue(), is(expected.getValueAssigns().get(i).getValue()));
            }
        }
    }
    
    public static void assertVariable(final SQLCaseAssertContext assertContext, final VariableSegment actual, final ExpectedVariable expected) {
        assertThat(assertContext.getText("variable assertion error: "), actual.getVariable(), is(expected.getName()));
        assertThat(assertContext.getText("scope assertion error: "), actual.getScope(), is(expected.getScope()));
    }
}
