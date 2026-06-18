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
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowSlaveHostsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowSlaveHostsStatementTestCase;

/**
 * Show replica host statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLShowSlaveHostsStatementAssert {
    
    /**
     * Assert show replica host statement is correct with expected show replica host statement test case.
     *
     * @param assertContext assert context
     * @param actual actual show replica host statement
     * @param expected expected show replica host statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowSlaveHostsStatement actual, final MySQLShowSlaveHostsStatementTestCase expected) {
    }
}
