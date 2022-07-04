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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.tcl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.LockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLLockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLLockStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.LockStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Lock statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockStatementAssert {
    
    /**
     * Assert lock statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual lock statement
     * @param expected expected lock statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final LockStatement actual, final LockStatementTestCase expected) {
        if (actual instanceof MySQLLockStatement) {
            MySQLLockStatement lockStatement = (MySQLLockStatement) actual;
            if (null != expected.getTables() && !expected.getTables().isEmpty()) {
                assertFalse(assertContext.getText("Actual lock statement should exist."), lockStatement.getTables().isEmpty());
                int count = 0;
                for (SimpleTableSegment each : lockStatement.getTables()) {
                    TableAssert.assertIs(assertContext, each, expected.getTables().get(count));
                    count++;
                }
            } else {
                assertTrue(assertContext.getText("Actual lock statement should not exist."), lockStatement.getTables().isEmpty());
            }
        } else if (actual instanceof PostgreSQLLockStatement) {
            PostgreSQLLockStatement lockStatement = (PostgreSQLLockStatement) actual;
            if (null != expected.getTables() && !expected.getTables().isEmpty()) {
                assertFalse(assertContext.getText("Actual lock statement should exist."), lockStatement.getTables().isEmpty());
                int count = 0;
                for (SimpleTableSegment each : lockStatement.getTables()) {
                    TableAssert.assertIs(assertContext, each, expected.getTables().get(count));
                    count++;
                }
            } else {
                assertTrue(assertContext.getText("Actual lock statement should not exist."), lockStatement.getTables().isEmpty());
            }
        }
    }
}
