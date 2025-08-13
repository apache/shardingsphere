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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ResetMasterOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ResetOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ResetSlaveOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.reset.ExpectedResetOptionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLResetStatementTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reset statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLResetStatementAssert {
    
    /**
     * Assert reset statement is correct with expected reset statement test case.
     *
     * @param assertContext assert context
     * @param actual actual reset statement
     * @param expected expected reset statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLResetStatement actual, final MySQLResetStatementTestCase expected) {
        assertThat(assertContext.getText("Actual options size assertion error: "), actual.getOptions().size(), is(expected.getOptions().size()));
        assertOptions(assertContext, actual.getOptions(), expected.getOptions());
    }
    
    private static void assertOptions(final SQLCaseAssertContext assertContext, final List<ResetOptionSegment> actual, final List<ExpectedResetOptionSegment> expected) {
        int i = 0;
        for (ExpectedResetOptionSegment each : expected) {
            SQLSegmentAssert.assertIs(assertContext, actual.get(i), each);
            if (each.isMaster()) {
                assertMasterOption(assertContext, (ResetMasterOptionSegment) actual.get(i), each);
            } else {
                assertSlaveOption(assertContext, (ResetSlaveOptionSegment) actual.get(i), each);
            }
            i++;
        }
    }
    
    private static void assertMasterOption(final SQLCaseAssertContext assertContext, final ResetMasterOptionSegment actual, final ExpectedResetOptionSegment expected) {
        if (null != expected.getBinaryLogFileIndexNumber()) {
            assertThat(assertContext.getText("Actual reset master binlog index does not match: "), actual.getBinaryLogFileIndexNumber(), is(expected.getBinaryLogFileIndexNumber()));
        }
    }
    
    private static void assertSlaveOption(final SQLCaseAssertContext assertContext, final ResetSlaveOptionSegment actual, final ExpectedResetOptionSegment expected) {
        assertThat(assertContext.getText("Actual reset slave all does not match: "), actual.isAll(), is(expected.isAll()));
        if (null != expected.getChannel()) {
            assertThat(assertContext.getText("Actual reset slave channel does not match: "), actual.getChannelOption(), is(expected.getChannel()));
        }
    }
}
