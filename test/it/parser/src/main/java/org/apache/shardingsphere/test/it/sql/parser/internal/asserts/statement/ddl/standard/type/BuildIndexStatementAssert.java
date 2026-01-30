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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.BuildIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.BuildIndexStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Build index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BuildIndexStatementAssert {
    
    /**
     * Assert build index statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual build index statement
     * @param expected expected build index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final BuildIndexStatement actual, final BuildIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertIndex(assertContext, actual, expected);
        assertPartitions(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final BuildIndexStatement actual, final BuildIndexStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertIndex(final SQLCaseAssertContext assertContext, final BuildIndexStatement actual, final BuildIndexStatementTestCase expected) {
        if (null == expected.getIndex()) {
            assertNull(actual.getIndex(), assertContext.getText("Actual index segment should not exist."));
        } else {
            assertNotNull(actual.getIndex(), assertContext.getText("Actual index segment should exist."));
            IndexAssert.assertIs(assertContext, actual.getIndex(), expected.getIndex());
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final BuildIndexStatement actual, final BuildIndexStatementTestCase expected) {
        if (null == expected.getPartitions() || expected.getPartitions().isEmpty()) {
            assertTrue(actual.getPartitions().isEmpty(), assertContext.getText("Actual partition segments should not exist."));
        } else {
            assertThat(assertContext.getText("Partition size assertion error: "),
                    actual.getPartitions().size(), is(expected.getPartitions().size()));
            int count = 0;
            for (PartitionSegment each : actual.getPartitions()) {
                assertPartition(assertContext, each, expected.getPartitions().get(count));
                count++;
            }
        }
    }
    
    private static void assertPartition(final SQLCaseAssertContext assertContext, final PartitionSegment actual, final ExpectedPartition expected) {
        assertThat(assertContext.getText(String.format("Partition name assertion error: ")), actual.getName().getValue(), is(expected.getName()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
