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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAnalyzeTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAnalyzeTableStatementTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Analyze table statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAnalyzeTableStatementAssert {
    
    /**
     * Assert analyze table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual analyze table statement
     * @param expected expected analyze table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAnalyzeTableStatement actual, final DorisAnalyzeTableStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertDatabase(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected.getColumns());
        assertSync(assertContext, actual.isSync(), expected.getSync());
        assertSample(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DorisAnalyzeTableStatement actual, final DorisAnalyzeTableStatementTestCase expected) {
        if (null != expected.getTable()) {
            assertNotNull(actual.getTable(), assertContext.getText("Table should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        } else {
            assertNull(actual.getTable(), assertContext.getText("Table should not exist."));
        }
    }
    
    private static void assertDatabase(final SQLCaseAssertContext assertContext, final DorisAnalyzeTableStatement actual, final DorisAnalyzeTableStatementTestCase expected) {
        if (null != expected.getDatabase()) {
            assertNotNull(actual.getDatabase(), assertContext.getText("Database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabase(), expected.getDatabase());
        } else {
            assertNull(actual.getDatabase(), assertContext.getText("Database should not exist."));
        }
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final DorisAnalyzeTableStatement actual, final List<ExpectedColumn> expected) {
        if (!expected.isEmpty()) {
            ColumnAssert.assertIs(assertContext, actual.getColumns(), expected);
        }
    }
    
    private static void assertSync(final SQLCaseAssertContext assertContext, final boolean actual, final Boolean expected) {
        if (null != expected) {
            assertThat(assertContext.getText("sync flag does not match: "), actual, is(expected));
        }
    }
    
    private static void assertSample(final SQLCaseAssertContext assertContext, final DorisAnalyzeTableStatement actual, final DorisAnalyzeTableStatementTestCase expected) {
        if (null != expected.getSampleType()) {
            assertTrue(actual.getSampleType().isPresent(), assertContext.getText("Sample type should be present"));
            assertThat(assertContext.getText("sample type does not match: "), actual.getSampleType().get(), is(expected.getSampleType()));
        } else {
            assertFalse(actual.getSampleType().isPresent(), assertContext.getText("sample type should not be present"));
        }
        if (null != expected.getSampleValue()) {
            assertTrue(actual.getSampleValue().isPresent(), assertContext.getText("sample value should be present"));
            assertThat(assertContext.getText("sample value does not match :"), actual.getSampleValue().get().toString(), is(expected.getSampleValue()));
        } else {
            assertFalse(actual.getSampleValue().isPresent(), assertContext.getText("sample value should not be present"));
        }
    }
}
