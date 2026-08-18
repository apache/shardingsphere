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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisRecoverPartitionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.partition.PartitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.RecoverPartitionStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Recover partition statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisRecoverPartitionStatementAssert {
    
    /**
     * Assert recover partition statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual recover partition statement
     * @param expected expected recover partition statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisRecoverPartitionStatement actual, final RecoverPartitionStatementTestCase expected) {
        assertPartition(assertContext, actual, expected);
        assertPartitionId(assertContext, actual, expected);
        assertNewPartition(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
    }
    
    private static void assertPartition(final SQLCaseAssertContext assertContext, final DorisRecoverPartitionStatement actual, final RecoverPartitionStatementTestCase expected) {
        if (null == expected.getPartition()) {
            assertNull(actual.getPartitionName(), assertContext.getText("Actual partition should not exist."));
        } else {
            assertNotNull(actual.getPartitionName(), assertContext.getText("Actual partition should exist."));
            PartitionAssert.assertIs(assertContext, actual.getPartitionName(), expected.getPartition());
        }
    }
    
    private static void assertPartitionId(final SQLCaseAssertContext assertContext, final DorisRecoverPartitionStatement actual, final RecoverPartitionStatementTestCase expected) {
        if (null == expected.getPartitionId()) {
            assertNull(actual.getPartitionId(), assertContext.getText("Actual partition id should not exist."));
        } else {
            assertNotNull(actual.getPartitionId(), assertContext.getText("Actual partition id should exist."));
            assertThat(assertContext.getText("Partition id assertion error: "), actual.getPartitionId().getIdentifier().getValue(), is(expected.getPartitionId()));
        }
    }
    
    private static void assertNewPartition(final SQLCaseAssertContext assertContext, final DorisRecoverPartitionStatement actual, final RecoverPartitionStatementTestCase expected) {
        if (null == expected.getNewPartition()) {
            assertNull(actual.getNewPartitionName(), assertContext.getText("Actual new partition should not exist."));
        } else {
            assertNotNull(actual.getNewPartitionName(), assertContext.getText("Actual new partition should exist."));
            PartitionAssert.assertIs(assertContext, actual.getNewPartitionName(), expected.getNewPartition());
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DorisRecoverPartitionStatement actual, final RecoverPartitionStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTableName(), assertContext.getText("Actual table should not exist."));
        } else {
            assertNotNull(actual.getTableName(), assertContext.getText("Actual table should exist."));
            TableAssert.assertIs(assertContext, actual.getTableName(), expected.getTable());
        }
    }
}
