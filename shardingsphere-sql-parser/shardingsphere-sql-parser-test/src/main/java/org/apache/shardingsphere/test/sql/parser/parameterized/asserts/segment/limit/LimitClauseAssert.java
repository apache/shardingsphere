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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.limit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.limit.ExpectedPaginationValue;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.ParameterMarkerPaginationValueSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Limit clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LimitClauseAssert {
    
    /**
     * Assert actual offset segment is correct with expected offset.
     * 
     * @param assertContext assert context
     * @param actual actual offset
     * @param expected expected offset
     */
    public static void assertOffset(final SQLCaseAssertContext assertContext, final PaginationValueSegment actual, final ExpectedPaginationValue expected) {
        if (null == actual) {
            assertNull(assertContext.getText("Offset should not exist."), expected);
            return;
        }
        if (actual instanceof ParameterMarkerPaginationValueSegment) {
            assertThat(assertContext.getText("Offset index assertion error: "),
                    ((ParameterMarkerPaginationValueSegment) actual).getParameterIndex(), is(expected.getParameterIndex()));
        } else {
            assertThat(assertContext.getText("Offset value assertion error: "), ((NumberLiteralPaginationValueSegment) actual).getValue(), is(expected.getValue()));
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert actual row count segment is correct with expected row count.
     *
     * @param assertContext assert context
     * @param actual actual row count
     * @param expected expected row count
     */
    public static void assertRowCount(final SQLCaseAssertContext assertContext, final PaginationValueSegment actual, final ExpectedPaginationValue expected) {
        if (null == actual) {
            assertNull(assertContext.getText("Row count should not exist."), expected);
            return;
        }
        if (SQLCaseType.Placeholder == assertContext.getSqlCaseType()) {
            assertThat(assertContext.getText("Row count index assertion error: "),
                    ((ParameterMarkerPaginationValueSegment) actual).getParameterIndex(), is(expected.getParameterIndex()));
        } else {
            assertThat(assertContext.getText("Row count value assertion error: "), ((NumberLiteralPaginationValueSegment) actual).getValue(), is(expected.getValue()));
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
