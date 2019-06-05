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
import org.apache.shardingsphere.core.parse.integrate.jaxb.pagination.ExpectedPagination;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationSegment;
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
     * Assert pagination.
     * 
     * @param actual actual pagination
     * @param expected expected pagination
     */
    public void assertPagination(final PaginationSegment actual, final ExpectedPagination expected) {
        if (null == actual) {
            assertNull(assertMessage.getFullAssertMessage("Pagination should not exist: "), expected);
            return;
        }
        if (SQLCaseType.Placeholder == sqlCaseType) {
            if (actual.getOffset().isPresent()) {
                assertThat(assertMessage.getFullAssertMessage("Pagination offset index assertion error: "),
                        ((ParameterMarkerPaginationValueSegment) actual.getOffset().get()).getParameterIndex(), is(expected.getOffsetParameterIndex()));
            }
            if (actual.getRowCount().isPresent()) {
                assertThat(assertMessage.getFullAssertMessage("Pagination row count index assertion error: "), 
                        ((ParameterMarkerPaginationValueSegment) actual.getRowCount().get()).getParameterIndex(), is(expected.getRowCountParameterIndex()));
            }
        } else {
            if (actual.getOffset().isPresent()) {
                assertThat(assertMessage.getFullAssertMessage("Pagination offset value assertion error: "), 
                        ((NumberLiteralPaginationValueSegment) actual.getOffset().get()).getValue(), is(expected.getOffset()));
            }
            if (actual.getRowCount().isPresent()) {
                assertThat(assertMessage.getFullAssertMessage("Pagination row count value assertion error: "),
                        ((NumberLiteralPaginationValueSegment) actual.getRowCount().get()).getValue(), is(expected.getRowCount()));
            }
        }
    }
}
