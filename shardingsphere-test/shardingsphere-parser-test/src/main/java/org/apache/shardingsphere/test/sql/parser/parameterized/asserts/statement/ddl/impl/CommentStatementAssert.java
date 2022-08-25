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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CommentStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.index.IndexTypeAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CommentStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        if (null != expected.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        } else {
            assertNull(assertContext.getText("Actual table should not exist."), actual.getTable());
        }
    }
    
    private static void assertColumn(final SQLCaseAssertContext assertContext, final CommentStatement actual, final CommentStatementTestCase expected) {
        if (null != expected.getColumn()) {
            ColumnAssert.assertIs(assertContext, actual.getColumn(), expected.getColumn());
        } else {
            assertNull(assertContext.getText("Actual column should not exist."), actual.getColumn());
        }
    }
    
    private static void assertIndexType(final SQLCaseAssertContext assertContext, final CommentStatement actual, final CommentStatementTestCase expected) {
        Optional<IndexTypeSegment> indexTypeSegment = CommentStatementHandler.getIndexType(actual);
        if (null != expected.getIndexType()) {
            assertTrue(assertContext.getText("Actual index type should exist"), indexTypeSegment.isPresent());
            IndexTypeAssert.assertIs(assertContext, indexTypeSegment.get(), expected.getIndexType());
        } else {
            assertFalse(assertContext.getText("Actual index type should not exist."), indexTypeSegment.isPresent());
        }
    }
}
