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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.LockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLLockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLLockStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.LockStatementTestCase;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertIs(assertContext, ((MySQLLockStatement) actual).getTables(), expected);
        } else if (actual instanceof PostgreSQLLockStatement) {
            assertIs(assertContext, ((PostgreSQLLockStatement) actual).getTables(), expected);
        }
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final Collection<SimpleTableSegment> actual, final LockStatementTestCase expected) {
        if (null == expected.getTables() || expected.getTables().isEmpty()) {
            assertTrue(actual.isEmpty(), assertContext.getText("Actual lock statement should not exist."));
            return;
        }
        assertFalse(actual.isEmpty(), assertContext.getText("Actual lock statement should exist."));
        int count = 0;
        for (SimpleTableSegment each : actual) {
            TableAssert.assertIs(assertContext, each, expected.getTables().get(count));
            count++;
        }
    }
}
