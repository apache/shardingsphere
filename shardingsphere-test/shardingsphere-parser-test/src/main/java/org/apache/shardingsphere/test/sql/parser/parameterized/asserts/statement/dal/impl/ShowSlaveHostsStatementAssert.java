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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowSlaveHostsStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowSlaveHostsStatementTestCase;

/**
 * Show slave host statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowSlaveHostsStatementAssert {

    /**
     * Assert show slave host statement is correct with expected show slave host statement test case.
     *
     * @param assertContext assert context
     * @param actual actual show slave host statement
     * @param expected expected show slave host statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowSlaveHostsStatement actual, final ShowSlaveHostsStatementTestCase expected) {
    }
}
