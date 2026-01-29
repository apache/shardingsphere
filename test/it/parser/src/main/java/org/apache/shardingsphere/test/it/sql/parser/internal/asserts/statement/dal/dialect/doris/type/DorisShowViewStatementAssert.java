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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.show.DorisShowViewStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.show.DorisShowViewStatementTestCase;

/**
 * Show view statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowViewStatementAssert {
    
    /**
     * Assert show view statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show view statement
     * @param expected expected show view statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowViewStatement actual, final DorisShowViewStatementTestCase expected) {
        assertFromTable(assertContext, actual, expected);
        assertFromDatabase(assertContext, actual, expected);
    }
    
    private static void assertFromTable(final SQLCaseAssertContext assertContext, final DorisShowViewStatement actual, final DorisShowViewStatementTestCase expected) {
        if (null == expected.getFrom() || !actual.getFromTable().isPresent()) {
            return;
        }
        SQLSegmentAssert.assertIs(assertContext, actual.getFromTable().get(), expected.getFrom());
        if (null != expected.getFrom().getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable().orElse(null), expected.getFrom().getTable());
        }
    }
    
    private static void assertFromDatabase(final SQLCaseAssertContext assertContext, final DorisShowViewStatement actual, final DorisShowViewStatementTestCase expected) {
        if (null == expected.getFromDatabase() || !actual.getDatabase().isPresent()) {
            return;
        }
        SQLSegmentAssert.assertIs(assertContext, actual.getDatabase().get(), expected.getFromDatabase().getDatabase());
        DatabaseAssert.assertIs(assertContext, actual.getDatabase().orElse(null), expected.getFromDatabase().getDatabase());
    }
}
