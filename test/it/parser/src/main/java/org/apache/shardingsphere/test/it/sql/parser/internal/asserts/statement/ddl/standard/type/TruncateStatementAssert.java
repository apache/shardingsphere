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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.TruncateStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Truncate statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TruncateStatementAssert {
    
    /**
     * Assert truncate statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual truncate statement
     * @param expected expected truncate statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TruncateStatement actual, final TruncateStatementTestCase expected) {
        assertTables(assertContext, actual, expected);
        assertPartitions(assertContext, actual, expected);
    }
    
    private static void assertTables(final SQLCaseAssertContext assertContext, final TruncateStatement actual, final TruncateStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getTables(), expected.getTables());
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final TruncateStatement actual, final TruncateStatementTestCase expected) {
        if (expected.getPartitions().isEmpty() || expected.getPartitions().stream().allMatch(each -> null == each.getName())) {
            return;
        }
        assertThat(assertContext.getText("Partition size assertion error: "), actual.getPartitions().size(), is(expected.getPartitions().size()));
        int count = 0;
        for (PartitionSegment each : actual.getPartitions()) {
            assertPartition(assertContext, each, expected.getPartitions().get(count));
            count++;
        }
    }
    
    private static void assertPartition(final SQLCaseAssertContext assertContext, final PartitionSegment actual, final ExpectedPartition expected) {
        assertThat(assertContext.getText("Partition name assertion error: "), actual.getName().getValue(), is(expected.getName()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
