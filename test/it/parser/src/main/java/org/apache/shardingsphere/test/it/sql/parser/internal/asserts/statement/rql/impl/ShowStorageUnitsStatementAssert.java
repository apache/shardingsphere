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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.storage.unit.ShowStorageUnitsStatementTestCase;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Show storage units statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowStorageUnitsStatementAssert {
    
    /**
     * Assert show storage units statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show storage units statement
     * @param expected expected show storage units statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowStorageUnitsStatement actual, final SQLParserTestCase expected) {
        assertThat("Expected value should be ShowStorageUnitsStatementTestCase", expected, instanceOf(ShowStorageUnitsStatementTestCase.class));
        assertIs(assertContext, actual, (ShowStorageUnitsStatementTestCase) expected);
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final ShowStorageUnitsStatement actual, final ShowStorageUnitsStatementTestCase expected) {
        if (null == expected.getDatabase()) {
            assertFalse(actual.getDatabase().isPresent(), assertContext.getText("Actual database should not exist."));
        } else {
            assertTrue(actual.getDatabase().isPresent(), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getDatabase());
        }
        if (null == expected.getUsageCount()) {
            assertFalse(actual.getUsageCount().isPresent(), assertContext.getText("Actual usage count should not exist."));
        } else {
            assertTrue(actual.getUsageCount().isPresent(), assertContext.getText("Actual usage count should exist."));
            assertThat(assertContext.getText("Usage count assertion error"), actual.getUsageCount().get(), is(expected.getUsageCount()));
        }
    }
}
