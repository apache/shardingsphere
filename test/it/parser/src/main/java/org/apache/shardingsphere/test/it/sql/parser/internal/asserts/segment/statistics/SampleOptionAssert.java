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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.statistics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.segment.SampleOptionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.statistics.ExpectedSampleOption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sample option assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SampleOptionAssert {
    
    /**
     * Assert actual sample option segment is correct with expected returning clause.
     *
     * @param assertContext assert context
     * @param actual actual sample option segment
     * @param expected expected sample option clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SampleOptionSegment actual, final ExpectedSampleOption expected) {
        assertStrategy(assertContext, actual, expected);
        assertSampleNumber(assertContext, actual, expected);
        assertScanUnit(assertContext, actual, expected);
        assertPartitions(assertContext, actual, expected);
        assertThat(assertContext.getText("persistSamplePercent assertion error: "), actual.isPersistSamplePercent(), is(expected.isPersistSamplePercent()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertStrategy(final SQLCaseAssertContext assertContext, final SampleOptionSegment actual, final ExpectedSampleOption expected) {
        if (null == expected.getStrategy()) {
            assertNull(actual.getStrategy(), assertContext.getText("Actual strategy should not exist."));
        } else {
            assertNotNull(actual.getStrategy(), assertContext.getText("Actual strategy should exist."));
            assertThat(assertContext.getText("strategy assertion error: "), actual.getStrategy().name(), is(expected.getStrategy()));
        }
    }
    
    private static void assertSampleNumber(final SQLCaseAssertContext assertContext, final SampleOptionSegment actual, final ExpectedSampleOption expected) {
        if (null == expected.getSampleNumber()) {
            assertNull(actual.getSampleNumber(), assertContext.getText("Actual sample number should not exist."));
        } else {
            assertNotNull(actual.getSampleNumber(), assertContext.getText("Actual sample number should exist."));
            assertThat(assertContext.getText("sample number assertion error: "), actual.getSampleNumber(), is(expected.getSampleNumber()));
        }
    }
    
    private static void assertScanUnit(final SQLCaseAssertContext assertContext, final SampleOptionSegment actual, final ExpectedSampleOption expected) {
        if (null == expected.getScanUnit()) {
            assertNull(actual.getScanUnit(), assertContext.getText("Actual scan unit should not exist."));
        } else {
            assertNotNull(actual.getScanUnit(), assertContext.getText("Actual scan unit should exist."));
            assertThat(assertContext.getText("scan unit assertion error: "), actual.getScanUnit().name(), is(expected.getScanUnit()));
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final SampleOptionSegment actual, final ExpectedSampleOption expected) {
        if (null == expected.getPartitions()) {
            assertNull(actual.getPartitions(), assertContext.getText("Actual partitions should not exist."));
        } else {
            assertNotNull(actual.getPartitions(), assertContext.getText("Actual partitions should exist."));
            int count = 0;
            for (String partition : expected.getPartitions()) {
                assertThat(assertContext.getText("partition assertion error: "), actual.getPartitions().get(count), is(partition));
                count++;
            }
        }
    }
}
