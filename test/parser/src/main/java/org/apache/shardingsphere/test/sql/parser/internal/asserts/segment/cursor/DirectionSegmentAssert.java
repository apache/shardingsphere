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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.cursor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.cursor.ExpectedDirectionSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Direction segment assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectionSegmentAssert {
    
    /**
     * Assert actual direction segment is correct with expected direction segment.
     * 
     * @param assertContext assert context
     * @param actual actual direction segment
     * @param expected expected direction segment
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DirectionSegment actual, final ExpectedDirectionSegment expected) {
        assertDirectionType(assertContext, actual, expected);
        assertCount(assertContext, actual, expected);
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertDirectionType(final SQLCaseAssertContext assertContext, final DirectionSegment actual, final ExpectedDirectionSegment expected) {
        if (null == expected.getDirectionType()) {
            assertFalse(assertContext.getText("Actual direction type should not exist."), actual.getDirectionType().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual direction type should exist."), actual.getDirectionType().isPresent());
            assertThat(assertContext.getText("Direction type assertion error: "), actual.getDirectionType().get().name(), is(expected.getDirectionType()));
        }
    }
    
    private static void assertCount(final SQLCaseAssertContext assertContext, final DirectionSegment actual, final ExpectedDirectionSegment expected) {
        if (null == expected.getCount()) {
            assertFalse(assertContext.getText("Actual count should not exist."), actual.getCount().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual count should exist."), actual.getCount().isPresent());
            assertThat(assertContext.getText("Count assertion error: "), actual.getCount().get(), is(expected.getCount()));
        }
    }
}
