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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.show.DorisShowDataSkewStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.partition.PartitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.show.DorisShowDataSkewStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Show data skew statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowDataSkewStatementAssert {
    
    /**
     * Assert show data skew statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show data skew statement
     * @param expected expected show data skew statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowDataSkewStatement actual, final DorisShowDataSkewStatementTestCase expected) {
        if (null != expected.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable().orElse(null), expected.getTable());
        }
        assertPartitions(assertContext, actual, expected);
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final DorisShowDataSkewStatement actual, final DorisShowDataSkewStatementTestCase expected) {
        assertThat(assertContext.getText("Assertion error: partition size does not match."), actual.getPartitions().size(), is(expected.getPartitions().size()));
        int count = 0;
        for (PartitionSegment each : actual.getPartitions()) {
            PartitionAssert.assertIs(assertContext, each, expected.getPartitions().get(count));
            count++;
        }
    }
}
