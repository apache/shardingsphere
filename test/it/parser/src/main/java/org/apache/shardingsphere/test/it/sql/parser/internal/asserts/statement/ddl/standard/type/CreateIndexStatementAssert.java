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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.CreateIndexStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateIndexStatementAssert {
    
    /**
     * Assert create index statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create index statement
     * @param expected expected create index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
        assertLockTable(assertContext, actual, expected);
        assertAlgorithm(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        IndexAssert.assertIs(assertContext, actual.getIndex(), expected.getIndex());
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        if (null == expected.getColumns()) {
            assertTrue(actual.getColumns().isEmpty(), assertContext.getText("Actual columns segments should not exist."));
        } else {
            ColumnAssert.assertIs(assertContext, actual.getColumns(), expected.getColumns());
        }
    }
    
    private static void assertLockTable(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        if (null == expected.getLockOption()) {
            assertFalse(actual.getLockTable().isPresent(), assertContext.getText("Actual lock table segments should not exist."));
        } else {
            assertTrue(actual.getLockTable().isPresent(), assertContext.getText("Actual lock table segments should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s lock table assertion error: ", actual.getClass().getSimpleName())),
                    actual.getLockTable().get().getLockTableOption().name(), is(expected.getLockOption().getType()));
        }
    }
    
    private static void assertAlgorithm(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        if (null == expected.getAlgorithmOption()) {
            assertFalse(actual.getAlgorithmType().isPresent(), assertContext.getText("Actual algorithm segments should not exist."));
        } else {
            assertTrue(actual.getAlgorithmType().isPresent(), assertContext.getText("Actual algorithm segments should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s algorithm assertion error: ", actual.getClass().getSimpleName())),
                    actual.getAlgorithmType().get().getAlgorithmOption().name(), is(expected.getAlgorithmOption().getType()));
        }
    }
}
