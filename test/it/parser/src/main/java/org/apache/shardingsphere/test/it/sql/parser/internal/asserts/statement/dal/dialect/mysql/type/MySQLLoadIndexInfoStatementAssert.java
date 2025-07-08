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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.LoadTableIndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.index.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedLoadTableIndex;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.index.MySQLLoadIndexInfoStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Load index info statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLLoadIndexInfoStatementAssert {
    
    /**
     * Assert load index info statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual load index info statement
     * @param expected expected load index info statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLLoadIndexInfoStatement actual, final MySQLLoadIndexInfoStatementTestCase expected) {
        assertThat("Load index info statement table index size assertion error: ", actual.getTableIndexes().size(), is(expected.getTableIndexes().size()));
        int count = 0;
        for (LoadTableIndexSegment each : actual.getTableIndexes()) {
            TableAssert.assertIs(assertContext, each.getTable(), expected.getTableIndexes().get(count).getTable());
            assertIndexes(assertContext, each, expected.getTableIndexes().get(count));
            assertPartitions(assertContext, each, expected.getTableIndexes().get(count));
            SQLSegmentAssert.assertIs(assertContext, each, expected.getTableIndexes().get(count));
            count++;
        }
    }
    
    private static void assertIndexes(final SQLCaseAssertContext assertContext, final LoadTableIndexSegment actual, final ExpectedLoadTableIndex expected) {
        int count = 0;
        for (IndexSegment index : actual.getIndexes()) {
            IndexAssert.assertIs(assertContext, index, expected.getIndexNames().get(count));
            count++;
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final LoadTableIndexSegment actual, final ExpectedLoadTableIndex expected) {
        int count = 0;
        for (PartitionSegment each : actual.getPartitions()) {
            IdentifierValueAssert.assertIs(assertContext, each.getName(), expected.getPartitions().get(count), "Partition");
            SQLSegmentAssert.assertIs(assertContext, each, expected.getPartitions().get(count));
            count++;
        }
    }
}
