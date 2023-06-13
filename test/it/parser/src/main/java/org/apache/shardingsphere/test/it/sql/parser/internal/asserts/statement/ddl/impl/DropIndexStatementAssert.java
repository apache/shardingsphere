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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropIndexStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DropIndexStatementTestCase;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drop index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropIndexStatementAssert {
    
    /**
     * Assert drop index statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual drop index statement
     * @param expected expected drop index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        assertTables(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
        assertLockTable(assertContext, actual, expected);
        assertAlgorithm(assertContext, actual, expected);
    }
    
    private static void assertTables(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(actual);
        if (null == expected.getTable()) {
            assertFalse(simpleTableSegment.isPresent(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertTrue(simpleTableSegment.isPresent(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, simpleTableSegment.get(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        // TODO should assert index for all databases(mysql and sqlserver do not parse index right now)
        if (actual instanceof OracleDropIndexStatement) {
            int count = 0;
            for (IndexSegment each : actual.getIndexes()) {
                IndexAssert.assertIs(assertContext, each, expected.getIndexes().get(count));
                count++;
            }
        }
    }
    
    private static void assertLockTable(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        if (null == expected.getLockOption()) {
            assertFalse(DropIndexStatementHandler.getLockTableSegment(actual).isPresent(), assertContext.getText("Actual lock table segments should not exist."));
        } else {
            assertTrue(DropIndexStatementHandler.getLockTableSegment(actual).isPresent(), assertContext.getText("Actual lock table segments should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s lock table assertion error: ", actual.getClass().getSimpleName())),
                    DropIndexStatementHandler.getLockTableSegment(actual).get().getLockTableOption().name(), is(expected.getLockOption().getType()));
        }
    }
    
    private static void assertAlgorithm(final SQLCaseAssertContext assertContext, final DropIndexStatement actual, final DropIndexStatementTestCase expected) {
        if (null == expected.getAlgorithmOption()) {
            assertFalse(DropIndexStatementHandler.getAlgorithmTypeSegment(actual).isPresent(), assertContext.getText("Actual algorithm segments should not exist."));
        } else {
            assertTrue(DropIndexStatementHandler.getAlgorithmTypeSegment(actual).isPresent(), assertContext.getText("Actual algorithm segments should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s algorithm assertion error: ", actual.getClass().getSimpleName())),
                    DropIndexStatementHandler.getAlgorithmTypeSegment(actual).get().getAlgorithmOption().name(), is(expected.getAlgorithmOption().getType()));
        }
    }
}
