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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ExplainStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Explain statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExplainStatementAssert {
    
    /**
     * Assert explain statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual explain statement
     * @param expected expected explain statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ExplainStatement actual, final ExplainStatementTestCase expected) {
        if (null != expected.getSelectClause()) {
            assertTrue(assertContext.getText("Actual statement should exist."), actual.getStatement().isPresent());
            SQLStatementAssert.assertIs(assertContext, actual.getStatement().get(), expected.getSelectClause());
        } else if (null != expected.getUpdateClause()) {
            assertTrue(assertContext.getText("Actual statement should exist."), actual.getStatement().isPresent());
            SQLStatementAssert.assertIs(assertContext, actual.getStatement().get(), expected.getUpdateClause());
        } else if (null != expected.getInsertClause()) {
            assertTrue(assertContext.getText("Actual statement should exist."), actual.getStatement().isPresent());
            SQLStatementAssert.assertIs(assertContext, actual.getStatement().get(), expected.getInsertClause());
        } else if (null != expected.getDeleteClause()) {
            assertTrue(assertContext.getText("Actual statement should exist."), actual.getStatement().isPresent());
            SQLStatementAssert.assertIs(assertContext, actual.getStatement().get(), expected.getDeleteClause());
        } else if (null != expected.getCreateTableAsSelectClause()) {
            assertTrue(assertContext.getText("Actual statement should exist."), actual.getStatement().isPresent());
            SQLStatementAssert.assertIs(assertContext, actual.getStatement().get(), expected.getCreateTableAsSelectClause());
        } else if (actual instanceof MySQLExplainStatement && null != expected.getTable()) {
            mysqlExplainStatementAssert(assertContext, (MySQLExplainStatement) actual, expected);
        } else {
            assertFalse(assertContext.getText("Actual statement should not exist."), actual.getStatement().isPresent());
        }
    }
    
    private static void mysqlExplainStatementAssert(final SQLCaseAssertContext assertContext, final MySQLExplainStatement actual, final ExplainStatementTestCase expected) {
        if (actual.getTable().isPresent()) {
            TableAssert.assertIs(assertContext, actual.getTable().get(), expected.getTable());
            if (actual.getColumnWild().isPresent()) {
                ColumnAssert.assertIs(assertContext, actual.getColumnWild().get(), expected.getColumn());
            } else {
                assertFalse(assertContext.getText("Actual column wild should not exist."), actual.getColumnWild().isPresent());
            }
        } else {
            assertFalse(assertContext.getText("Actual table should not exist."), actual.getTable().isPresent());
        }
    }
}
