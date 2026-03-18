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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowOpenTablesStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.show.ShowFilterAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.table.MySQLShowOpenTablesStatementTestCase;

/**
 * Show open tables statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLShowOpenTablesStatementAssert {
    
    /**
     * Assert show open tables statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show open tables statement
     * @param expected expected show open tables statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowOpenTablesStatement actual, final MySQLShowOpenTablesStatementTestCase expected) {
        if (null != actual.getFromDatabase()) {
            DatabaseAssert.assertIs(assertContext, actual.getFromDatabase().getDatabase(), expected.getFromDatabase().getDatabase());
            SQLSegmentAssert.assertIs(assertContext, actual.getFromDatabase(), expected.getFromDatabase());
        }
        if (null != actual.getFilter()) {
            ShowFilterAssert.assertIs(assertContext, actual.getFilter(), expected.getFilter());
        }
    }
}
