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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.pagination;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.pagination.ExpectedPaginationValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Pagination assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaginationAssert {
    
    /**
     * Assert actual offset segment is correct with expected offset.
     * 
     * @param assertMessage assert message
     * @param actual actual offset
     * @param expected expected offset
     * @param sqlCaseType SQL case type
     */
    public static void assertOffset(final SQLCaseAssertMessage assertMessage, final PaginationValueSegment actual, final ExpectedPaginationValue expected, final SQLCaseType sqlCaseType) {
        if (null == actual) {
            assertNull(assertMessage.getText("Offset should not exist."), expected);
            return;
        }
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getText("Offset index assertion error: "),
                    ((ParameterMarkerPaginationValueSegment) actual).getParameterIndex(), is(expected.getParameterIndex()));
        } else {
            assertThat(assertMessage.getText("Offset value assertion error: "), ((NumberLiteralPaginationValueSegment) actual).getValue(), is(expected.getValue()));
        }
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    /**
     * Assert actual row count segment is correct with expected row count.
     *
     * @param assertMessage assert message
     * @param actual actual row count
     * @param expected expected row count
     * @param sqlCaseType SQL case type
     */
    public static void assertRowCount(final SQLCaseAssertMessage assertMessage, final PaginationValueSegment actual, final ExpectedPaginationValue expected, final SQLCaseType sqlCaseType) {
        if (null == actual) {
            assertNull(assertMessage.getText("Row count should not exist."), expected);
            return;
        }
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getText("Row count index assertion error: "),
                    ((ParameterMarkerPaginationValueSegment) actual).getParameterIndex(), is(expected.getParameterIndex()));
        } else {
            assertThat(assertMessage.getText("Row count value assertion error: "), ((NumberLiteralPaginationValueSegment) actual).getValue(), is(expected.getValue()));
        }
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}
