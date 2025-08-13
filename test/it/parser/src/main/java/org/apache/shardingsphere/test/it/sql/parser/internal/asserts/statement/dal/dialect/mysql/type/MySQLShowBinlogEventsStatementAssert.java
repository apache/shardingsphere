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
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.binlog.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.binlog.MySQLShowBinlogEventsStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Show binlog events statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLShowBinlogEventsStatementAssert {
    
    /**
     * Assert show binlog events statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show binlog events statement
     * @param expected expected show binlog events statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowBinlogEventsStatement actual, final MySQLShowBinlogEventsStatementTestCase expected) {
        if (null == expected.getLogName()) {
            assertNull(actual.getLogName(), assertContext.getText("Actual logName should not exist."));
        } else {
            assertNotNull(actual.getLogName(), assertContext.getText("Actual logName should exist."));
            assertThat(actual.getLogName(), is(expected.getLogName()));
        }
        if (null == expected.getLimitClause()) {
            assertNull(actual.getLimit(), assertContext.getText("Actual limit clause should not exist."));
        } else {
            assertNotNull(actual.getLimit(), assertContext.getText("Actual limit clause should exist."));
            LimitClauseAssert.assertOffset(assertContext, actual.getLimit().getOffset().get(), expected.getLimitClause().getOffset());
            LimitClauseAssert.assertRowCount(assertContext, actual.getLimit().getRowCount().get(), expected.getLimitClause().getRowCount());
        }
    }
}
