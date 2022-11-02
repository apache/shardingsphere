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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.updatable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.DatabaseContainedTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AlterReadwriteSplittingStorageUnitStatusStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlterReadwriteSplittingStorageUnitStatusStatementAssert {
    
    /**
     * Alter readwrite splitting storage unit status statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter readwrite splitting storage unit status statement
     * @param expected expected alter readwrite splitting storage unit status statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterReadwriteSplittingStorageUnitStatusStatement actual,
                                final AlterReadwriteSplittingStorageUnitStatusStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertThat(actual.getGroupName(), is(expected.getGroupName()));
            assertThat(actual.getStorageUnitName(), is(expected.getStorageUnitName()));
            assertThat(actual.getStatus(), is(expected.getStatus()));
            assertIs(assertContext, actual, (DatabaseContainedTestCase) expected);
        }
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final AlterReadwriteSplittingStorageUnitStatusStatement actual, final DatabaseContainedTestCase expected) {
        if (null == expected.getDatabase()) {
            assertFalse(assertContext.getText("Actual database should not exist."), actual.getDatabase().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual database should exist."), actual.getDatabase().isPresent());
            DatabaseAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getDatabase());
        }
    }
}
