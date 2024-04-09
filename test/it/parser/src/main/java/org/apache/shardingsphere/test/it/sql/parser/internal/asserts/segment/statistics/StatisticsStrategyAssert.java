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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.segment.StatisticsStrategySegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.statistics.ExpectedStatisticsStrategy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Statistics strategy assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsStrategyAssert {
    
    /**
     * Assert actual statistics strategy segment is correct with expected returning clause.
     *
     * @param assertContext assert context
     * @param actual actual statistics strategy segment
     * @param expected expected statistics strategy clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final StatisticsStrategySegment actual, final ExpectedStatisticsStrategy expected) {
        assertSampleOption(assertContext, actual, expected);
        assertStatisticsOption(assertContext, actual, expected);
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertSampleOption(final SQLCaseAssertContext assertContext, final StatisticsStrategySegment actual, final ExpectedStatisticsStrategy expected) {
        if (null == expected.getSampleOption()) {
            assertNull(actual.getSampleOption(), assertContext.getText("Actual sample option segment should not exist."));
        } else {
            assertNotNull(actual.getSampleOption(), assertContext.getText("Actual sample option segment should exist."));
            SampleOptionAssert.assertIs(assertContext, actual.getSampleOption(), expected.getSampleOption());
        }
    }
    
    private static void assertStatisticsOption(final SQLCaseAssertContext assertContext, final StatisticsStrategySegment actual, final ExpectedStatisticsStrategy expected) {
        if (null == expected.getStatisticsOption()) {
            assertNull(actual.getStatisticsOptions(), assertContext.getText("Actual statistics option segment should not exist."));
        } else {
            assertNotNull(actual.getStatisticsOptions(), assertContext.getText("Actual statistics option segment should exist."));
            StatisticsOptionAssert.assertIs(assertContext, actual.getStatisticsOptions(), expected.getStatisticsOption());
        }
    }
}
