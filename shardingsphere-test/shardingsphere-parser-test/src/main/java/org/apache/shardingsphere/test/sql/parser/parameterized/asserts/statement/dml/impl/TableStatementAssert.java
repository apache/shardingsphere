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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.TableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLTableStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.TableStatementTestCase;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Table statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableStatementAssert {
    
    /**
     * Assert table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual table statement
     * @param expected expected table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableStatement actual, final TableStatementTestCase expected) {
        if (actual instanceof MySQLTableStatement) {
            assertTable(assertContext, (MySQLTableStatement) actual, expected);
            assertLimitClause(assertContext, (MySQLTableStatement) actual, expected);
            ColumnAssert.assertIs(assertContext, ((MySQLTableStatement) actual).getColumn(), expected.getColumn());
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final MySQLTableStatement actual, final TableStatementTestCase expected) {
        if (null != expected.getSimpleTable()) {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getSimpleTable());
        } else {
            assertNull(assertContext.getText("Actual source should not exist."), actual.getTable());
        }
    }
    
    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final MySQLTableStatement actual, final TableStatementTestCase expected) {
        if (null != expected.getLimitClause()) {
            assertNotNull(assertContext.getText("Actual limit segment should exist."), actual.getLimit());
            LimitClauseAssert.assertOffset(assertContext, actual.getLimit().getOffset().orElse(null), expected.getLimitClause().getOffset());
            LimitClauseAssert.assertRowCount(assertContext, actual.getLimit().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, actual.getLimit(), expected.getLimitClause());
        } else {
            assertNull(assertContext.getText("Actual limit segment should not exist."), actual.getLimit());
        }
    }
}
