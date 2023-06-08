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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.impl.table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.table.ShowSingleTableStatementTestCase;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Show single tables statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowSingleTablesStatementAssert {
    
    /**
     * Assert show single tables statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show single tables statement
     * @param expected expected show single tables statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowSingleTableStatement actual, final SQLParserTestCase expected) {
        assertThat("Expected value should be ShowSingleTableStatementTestCase", expected, instanceOf(ShowSingleTableStatementTestCase.class));
        assertIs(assertContext, actual, (ShowSingleTableStatementTestCase) expected);
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final ShowSingleTableStatement actual, final ShowSingleTableStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertFalse(actual.getTableName().isPresent(), assertContext.getText("Actual table name should not exist."));
        } else {
            assertTrue(actual.getTableName().isPresent(), assertContext.getText("Actual table name should exist."));
            assertThat(assertContext.getText("Table name assertion error"), actual.getTableName().get(), is(expected.getTable()));
        }
    }
}
