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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateIndexStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateIndexStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        // TODO should assert index for all databases(mysql and sqlserver do not parse index right now)
        if (actual instanceof OracleCreateIndexStatement) {
            IndexAssert.assertIs(assertContext, actual.getIndex(), expected.getIndex());
        }
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
            assertFalse(CreateIndexStatementHandler.getLockTableSegment(actual).isPresent(), assertContext.getText("Actual lock table segments should not exist."));
        } else {
            assertTrue(CreateIndexStatementHandler.getLockTableSegment(actual).isPresent(), assertContext.getText("Actual lock table segments should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s lock table assertion error: ", actual.getClass().getSimpleName())),
                    CreateIndexStatementHandler.getLockTableSegment(actual).get().getLockTableOption().name(), is(expected.getLockOption().getType()));
        }
    }
    
    private static void assertAlgorithm(final SQLCaseAssertContext assertContext, final CreateIndexStatement actual, final CreateIndexStatementTestCase expected) {
        if (null == expected.getAlgorithmOption()) {
            assertFalse(CreateIndexStatementHandler.getAlgorithmTypeSegment(actual).isPresent(), assertContext.getText("Actual algorithm segments should not exist."));
        } else {
            assertTrue(CreateIndexStatementHandler.getAlgorithmTypeSegment(actual).isPresent(), assertContext.getText("Actual algorithm segments should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s algorithm assertion error: ", actual.getClass().getSimpleName())),
                    CreateIndexStatementHandler.getAlgorithmTypeSegment(actual).get().getAlgorithmOption().name(), is(expected.getAlgorithmOption().getType()));
        }
    }
}
