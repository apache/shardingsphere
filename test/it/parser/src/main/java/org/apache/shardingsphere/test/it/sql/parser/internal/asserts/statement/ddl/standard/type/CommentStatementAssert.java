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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexTypeAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.CommentStatementTestCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comment statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentStatementAssert {
    
    /**
     * Assert comment statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual comment statement
     * @param expected expected comment statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CommentStatement actual, final CommentStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertColumn(assertContext, actual, expected);
        assertIndexType(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CommentStatement actual, final CommentStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertColumn(final SQLCaseAssertContext assertContext, final CommentStatement actual, final CommentStatementTestCase expected) {
        if (null == expected.getColumn()) {
            assertNull(actual.getColumn(), assertContext.getText("Actual column should not exist."));
        } else {
            ColumnAssert.assertIs(assertContext, actual.getColumn(), expected.getColumn());
        }
    }
    
    private static void assertIndexType(final SQLCaseAssertContext assertContext, final CommentStatement actual, final CommentStatementTestCase expected) {
        Optional<IndexTypeSegment> indexTypeSegment = actual.getIndexType();
        if (null == expected.getIndexType()) {
            assertFalse(indexTypeSegment.isPresent(), assertContext.getText("Actual index type should not exist."));
        } else {
            assertTrue(indexTypeSegment.isPresent(), assertContext.getText("Actual index type should exist"));
            IndexTypeAssert.assertIs(assertContext, indexTypeSegment.get(), expected.getIndexType());
        }
    }
}
