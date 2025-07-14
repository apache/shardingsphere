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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.postgresql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.prepare.PrepareStatementQueryAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.postgresql.PostgreSQLCopyStatementTestCase;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Copy statement assert for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLCopyStatementAssert {
    
    /**
     * Assert copy statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual copy statement
     * @param expected expected copy statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final PostgreSQLCopyStatement actual, final PostgreSQLCopyStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
        assertPrepareStatementQuerySegment(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final PostgreSQLCopyStatement actual, final PostgreSQLCopyStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertFalse(actual.getTable().isPresent(), assertContext.getText("Actual table should not exist."));
        } else {
            assertTrue(actual.getTable().isPresent(), assertContext.getText("Actual table should exist."));
            TableAssert.assertIs(assertContext, actual.getTable().get(), expected.getTable());
        }
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final PostgreSQLCopyStatement actual, final PostgreSQLCopyStatementTestCase expected) {
        Collection<ColumnSegment> columnSegments = actual.getColumns();
        if (expected.getColumns().isEmpty()) {
            assertTrue(columnSegments.isEmpty(), assertContext.getText("Actual column segments should not exist."));
        } else {
            assertFalse(columnSegments.isEmpty(), assertContext.getText("Actual column segments should exist."));
            ColumnAssert.assertIs(assertContext, columnSegments, expected.getColumns());
        }
    }
    
    private static void assertPrepareStatementQuerySegment(final SQLCaseAssertContext assertContext, final PostgreSQLCopyStatement actual, final PostgreSQLCopyStatementTestCase expected) {
        Optional<PrepareStatementQuerySegment> prepareStatementQuerySegment = actual.getPrepareStatementQuery();
        if (null == expected.getQuery()) {
            assertFalse(prepareStatementQuerySegment.isPresent(), assertContext.getText("Actual prepare statement query segment should not exist."));
        } else {
            assertTrue(prepareStatementQuerySegment.isPresent(), assertContext.getText("Actual prepare statement query segment should exist."));
            PrepareStatementQueryAssert.assertIs(assertContext, prepareStatementQuerySegment.get(), expected.getQuery());
        }
    }
}
