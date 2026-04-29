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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisRecoverStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisRecoverStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Recover statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisRecoverStatementAssert {
    
    /**
     * Assert recover statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual recover statement
     * @param expected expected recover statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisRecoverStatement actual, final DorisRecoverStatementTestCase expected) {
        assertThat(assertContext.getText("Recover object type does not match: "), actual.getObjectType().name(), is(expected.getObjectType()));
        assertDatabase(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertThat(assertContext.getText("Partition name does not match: "), actual.getPartitionName(), is(expected.getPartitionName()));
        assertThat(assertContext.getText("Object id does not match: "), actual.getObjectId(), is(expected.getObjectId()));
        assertThat(assertContext.getText("New name does not match: "), actual.getNewName(), is(expected.getNewName()));
    }
    
    private static void assertDatabase(final SQLCaseAssertContext assertContext, final DorisRecoverStatement actual, final DorisRecoverStatementTestCase expected) {
        if (null == expected.getDatabase()) {
            assertNull(actual.getDatabase(), assertContext.getText("Actual database should not exist."));
        } else {
            assertNotNull(actual.getDatabase(), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabase(), expected.getDatabase());
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DorisRecoverStatement actual, final DorisRecoverStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table should not exist."));
        } else {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
}
