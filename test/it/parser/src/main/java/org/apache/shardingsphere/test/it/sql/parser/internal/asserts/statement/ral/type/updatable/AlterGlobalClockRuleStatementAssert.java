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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterGlobalClockRuleStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Alter global clock rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterGlobalClockRuleStatementAssert {
    
    /**
     * Assert alter global clock rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter global clock rule statement
     * @param expected expected alter global clock rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterGlobalClockRuleStatement actual, final AlterGlobalClockRuleStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertGlobalClockRule(assertContext, actual, expected);
        }
    }
    
    private static void assertGlobalClockRule(final SQLCaseAssertContext assertContext, final AlterGlobalClockRuleStatement actual, final AlterGlobalClockRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual global clock rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual global clock rule should exist."));
            assertThat(assertContext.getText(String.format("Actual type should be %s , but it was %s", expected.getType(), actual.getType())),
                    actual.getType(), is(expected.getType()));
            assertThat(assertContext.getText(String.format("Actual provider should be %s , but it was %s", expected.getProvider(), actual.getProvider())),
                    actual.getProvider(), is(expected.getProvider()));
            assertThat(assertContext.getText(String.format("Actual enabled should be %s , but it was %s", expected.isEnabled(), actual.isEnabled())),
                    actual.isEnabled(), is(expected.isEnabled()));
            assertThat(assertContext.getText(String.format("Actual properties size should be %s , but it was %s", expected.getProperties().size(), actual.getProps().size())),
                    actual.getProps().size(), is(expected.getProperties().size()));
        }
    }
}
