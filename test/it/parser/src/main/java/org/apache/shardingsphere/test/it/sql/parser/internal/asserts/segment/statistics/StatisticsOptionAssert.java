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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.segment.StatisticsOptionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.statistics.ExpectedStatisticsOption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Statistics option assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsOptionAssert {
    
    /**
     * Assert actual statistics option segment is correct with expected returning clause.
     *
     * @param assertContext assert context
     * @param actual actual sample option segment
     * @param expected expected sample option clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final StatisticsOptionSegment actual, final ExpectedStatisticsOption expected) {
        assertDimension(assertContext, actual, expected);
        assertMaxDegreeOfParallelism(assertContext, actual, expected);
        assertThat(assertContext.getText("noRecompute assertion error: "), actual.isNoRecompute(), is(expected.isNoRecompute()));
        assertThat(assertContext.getText("incremental assertion error: "), actual.isIncremental(), is(expected.isIncremental()));
        assertThat(assertContext.getText("autoDrop assertion error: "), actual.isAutoDrop(), is(expected.isAutoDrop()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertDimension(final SQLCaseAssertContext assertContext, final StatisticsOptionSegment actual, final ExpectedStatisticsOption expected) {
        if (null == expected.getStatisticsDimension()) {
            assertNull(actual.getStatisticsDimension(), assertContext.getText("Actual dimension should not exist."));
        } else {
            assertNotNull(actual.getStatisticsDimension(), assertContext.getText("Actual dimension strategy should exist."));
            assertThat(assertContext.getText("dimension assertion error: "), actual.getStatisticsDimension().name(), is(expected.getStatisticsDimension()));
        }
    }
    
    private static void assertMaxDegreeOfParallelism(final SQLCaseAssertContext assertContext, final StatisticsOptionSegment actual, final ExpectedStatisticsOption expected) {
        if (null == expected.getMaxDegreeOfParallelism()) {
            assertNull(actual.getMaxDegreeOfParallelism(), assertContext.getText("Actual max degree parallelism should not exist."));
        } else {
            assertNotNull(actual.getMaxDegreeOfParallelism(), assertContext.getText("Actual max degree parallelism strategy should exist."));
            assertThat(assertContext.getText("max degree parallelism assertion error: "), actual.getMaxDegreeOfParallelism(), is(expected.getMaxDegreeOfParallelism()));
        }
    }
}
