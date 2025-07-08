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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.CacheTableIndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.index.MySQLCacheIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedCacheTableIndex;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartitionDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.index.MySQLCacheIndexStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Cache index statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCacheIndexStatementAssert {
    
    /**
     * Assert cache index statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual cache index statement
     * @param expected expected cache index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLCacheIndexStatement actual, final MySQLCacheIndexStatementTestCase expected) {
        if (!expected.getTableIndexes().isEmpty()) {
            int count = 0;
            for (CacheTableIndexSegment each : actual.getTableIndexes()) {
                TableAssert.assertIs(assertContext, each.getTable(), expected.getTableIndexes().get(count).getTable());
                assertIndexes(assertContext, each, expected.getTableIndexes().get(count));
                SQLSegmentAssert.assertIs(assertContext, each, expected.getTableIndexes().get(count));
                count++;
            }
        }
        if (null != expected.getPartitionDefinition()) {
            assertPartitions(assertContext, actual.getPartitionDefinition(), expected.getPartitionDefinition());
            SQLSegmentAssert.assertIs(assertContext, actual.getPartitionDefinition(), expected.getPartitionDefinition());
        }
        if (null != expected.getName()) {
            assertThat(assertContext.getText("Cache index statement name assert error: "), actual.getName().getValue(), is(expected.getName()));
        }
    }
    
    private static void assertIndexes(final SQLCaseAssertContext assertContext, final CacheTableIndexSegment actual, final ExpectedCacheTableIndex expected) {
        int count = 0;
        for (IndexSegment index : actual.getIndexes()) {
            IndexAssert.assertIs(assertContext, index, expected.getIndexNames().get(count));
            count++;
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final PartitionDefinitionSegment actual, final ExpectedPartitionDefinition expected) {
        TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        int count = 0;
        for (PartitionSegment each : actual.getPartitions()) {
            IdentifierValueAssert.assertIs(assertContext, each.getName(), expected.getPartitions().get(count), "Partition");
            SQLSegmentAssert.assertIs(assertContext, each, expected.getPartitions().get(count));
            count++;
        }
    }
}
