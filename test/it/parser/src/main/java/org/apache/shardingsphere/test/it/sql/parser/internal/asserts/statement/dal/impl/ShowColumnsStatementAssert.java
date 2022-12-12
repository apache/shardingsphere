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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.show.ShowFilterAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowColumnsStatementTestCase;

/**
 * Show columns statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowColumnsStatementAssert {
    
    /**
     * Assert show columns statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual show columns statement
     * @param expected expected show columns statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowColumnsStatement actual, final ShowColumnsStatementTestCase expected) {
        if (null != actual.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
        if (actual.getFromSchema().isPresent()) {
            DatabaseAssert.assertIs(assertContext, actual.getFromSchema().get().getSchema(), expected.getSchema());
        }
        if (actual.getFilter().isPresent()) {
            ShowFilterAssert.assertIs(assertContext, actual.getFilter().get(), expected.getFilter());
        }
    }
}
