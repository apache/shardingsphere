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

package org.apache.shardingsphere.core.parse.integrate.asserts.pagination;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.pagination.ExpectedPaginationValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Pagination assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class PaginationAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert offset.
     * 
     * @param actual actual offset
     * @param expected expected offset
     */
    public void assertOffset(final PaginationValueSegment actual, final ExpectedPaginationValue expected) {
        if (null == actual) {
            assertNull(assertMessage.getFullAssertMessage("Offset should not exist: "), expected);
            return;
        }
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Offset index assertion error: "),
                    ((ParameterMarkerPaginationValueSegment) actual).getParameterIndex(), is(expected.getParameterIndex()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Offset value assertion error: "), ((NumberLiteralPaginationValueSegment) actual).getValue(), is(expected.getValue()));
        }
    }
    
    /**
     * Assert row count.
     *
     * @param actual actual row count
     * @param expected expected row count
     */
    public void assertRowCount(final PaginationValueSegment actual, final ExpectedPaginationValue expected) {
        if (null == actual) {
            assertNull(assertMessage.getFullAssertMessage("Row count should not exist: "), expected);
            return;
        }
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Row count index assertion error: "),
                    ((ParameterMarkerPaginationValueSegment) actual).getParameterIndex(), is(expected.getParameterIndex()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Row count value assertion error: "), ((NumberLiteralPaginationValueSegment) actual).getValue(), is(expected.getValue()));
        }
    }
}
