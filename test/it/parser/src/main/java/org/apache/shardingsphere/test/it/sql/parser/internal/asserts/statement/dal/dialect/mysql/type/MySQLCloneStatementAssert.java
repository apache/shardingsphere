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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.CloneInstanceSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLCloneStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLCloneStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Clone statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCloneStatementAssert {
    
    /**
     * Assert clone statement is correct with expected clone statement test case.
     *
     * @param assertContext assert context
     * @param actual actual clone statement
     * @param expected expected clone statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLCloneStatement actual, final MySQLCloneStatementTestCase expected) {
        if (null != expected.getDataDirectory()) {
            assertThat(assertContext.getText("Actual data directory does not match: "), actual.getCloneAction().getCloneDir(), is(expected.getDataDirectory().getLocation()));
        }
        if (null != expected.getInstance()) {
            CloneInstanceSegment instance = actual.getCloneAction().getCloneInstance();
            assertThat(assertContext.getText("Actual instance hostname does not match: "), instance.getHostname(), is(expected.getInstance().getHostname()));
            assertThat(assertContext.getText("Actual instance username does not match: "), instance.getUsername(), is(expected.getInstance().getUsername()));
            assertThat(assertContext.getText("Actual instance port does not match: "), instance.getPort(), is(expected.getInstance().getPort()));
            assertThat(assertContext.getText("Actual instance password does not match: "), instance.getPassword(), is(expected.getInstance().getPassword()));
            assertThat(assertContext.getText("Actual instance SSL requirement does not match: "), instance.isSslRequired(), is(expected.getInstance().isSslRequired()));
        }
    }
}
