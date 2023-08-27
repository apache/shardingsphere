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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.IndexPartitionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.IndexPartitionsSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.tablespace.TablespaceAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedIndexPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedIndexPartitions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexPartitionsAssert {
    
    /**
     * Assert actual index partitions segment is correct with expected index.
     *
     * @param assertContext assert context
     * @param actual actual index segment
     * @param expected expected index
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final IndexPartitionsSegment actual, final ExpectedIndexPartitions expected) {
        assertNotNull(expected, assertContext.getText("Index partitions should exist."));
        assertThat(assertContext.getText("Index partitions type assertion error: "), actual.getPartitionType().name(), is(expected.getType()));
        assertThat(assertContext.getText("Index partitions size assertion error: "), actual.getIndexPartitionSegment().size(), is(expected.getPartitions().size()));
        int i = 0;
        for (IndexPartitionSegment actualIndexPartitionSegment : actual.getIndexPartitionSegment()) {
            assertIs(assertContext, actualIndexPartitionSegment, expected.getPartitions().get(i++));
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final IndexPartitionSegment actual, final ExpectedIndexPartition expected) {
        assertNotNull(expected, assertContext.getText("Index partition should exist."));
        assertNotNull(actual, assertContext.getText("Index partition should exist."));
        IdentifierValueAssert.assertIs(assertContext, actual.getIndexPartitionName(), expected, "Index Partition");
        if (null == expected.getTablespace()) {
            assertFalse(actual.getTablespace().isPresent(), assertContext.getText("Actual tablespace should not exist."));
        } else {
            assertTrue(actual.getTablespace().isPresent(), assertContext.getText("Actual tablespace should exist."));
            TablespaceAssert.assertIs(assertContext, actual.getTablespace().get(), expected.getTablespace());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
