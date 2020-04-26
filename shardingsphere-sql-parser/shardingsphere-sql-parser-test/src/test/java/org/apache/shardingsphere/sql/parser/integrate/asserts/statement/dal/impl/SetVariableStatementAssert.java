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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.cases.domain.statement.dal.SetVariableStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        if (null != expected.getVariable()) {
            assertNotNull(assertContext.getText("Actual variable expression should exist."), actual.getVariable());
            assertThat(assertContext.getText("variable expression assertion error: "), actual.getVariable().getVariable(), is(expected.getVariable()));
        } else {
            assertNull(assertContext.getText("Actual variable expression should not exist."), actual.getVariable());
        }
    }
}
