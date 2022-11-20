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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.CopyStatementHandler;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.prepare.PrepareStatementQueryAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.dml.CopyStatementTestCase;

import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Copy statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CopyStatementAssert {
    
    /**
     * Assert copy statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual copy statement
     * @param expected expected copy statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CopyStatement actual, final CopyStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
        assertPrepareStatementQuerySegment(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CopyStatement actual, final CopyStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(assertContext.getText("Actual table should not exist."), actual.getTableSegment());
        } else {
            TableAssert.assertIs(assertContext, actual.getTableSegment(), expected.getTable());
        }
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final CopyStatement actual, final CopyStatementTestCase expected) {
        Collection<ColumnSegment> columnSegments = CopyStatementHandler.getColumns(actual);
        if (null == expected.getColumns() || expected.getColumns().getColumns().isEmpty()) {
            assertTrue(assertContext.getText("Actual column segments should not exist."), columnSegments.isEmpty());
        } else {
            assertFalse(assertContext.getText("Actual column segments should exist."), columnSegments.isEmpty());
            ColumnAssert.assertIs(assertContext, columnSegments, expected.getColumns().getColumns());
        }
    }
    
    private static void assertPrepareStatementQuerySegment(final SQLCaseAssertContext assertContext, final CopyStatement actual, final CopyStatementTestCase expected) {
        Optional<PrepareStatementQuerySegment> prepareStatementQuerySegment = CopyStatementHandler.getPrepareStatementQuerySegment(actual);
        if (null == expected.getQuery()) {
            assertFalse(assertContext.getText("Actual prepare statement query segment should not exist."), prepareStatementQuerySegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual prepare statement query segment should exist."), prepareStatementQuerySegment.isPresent());
            PrepareStatementQueryAssert.assertIs(assertContext, prepareStatementQuerySegment.get(), expected.getQuery());
        }
    }
}
