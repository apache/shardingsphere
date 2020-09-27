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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.output;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.output.ExpectedOutputClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.projection.impl.column.ExpectedColumnProjection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Output clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OutputClauseAssert {
    
    /**
     * Assert actual output segment is correct with expected output clause.
     *
     * @param assertContext assert context
     * @param actual actual output segment
     * @param expected expected output clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OutputSegment actual, final ExpectedOutputClause expected) {
        assertNotNull(assertContext.getText("Output clause should exist."), expected);
        if (null != actual.getOutputColumns()) {
            assertOutputColumnsSegment(assertContext, actual, expected);
        }
        if (null != actual.getTableName()) {
            assertOutputTableSegment(assertContext, actual, expected);
        }
        if (null != actual.getTableColumns()) {
            assertOutputTableColumnSegment(assertContext, actual, expected);
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertOutputColumnsSegment(final SQLCaseAssertContext assertContext, final OutputSegment actual, final ExpectedOutputClause expected) {
        assertThat(assertContext.getText("Output columns size assertion error: "),
                actual.getOutputColumns().size(), is(expected.getOutputColumns().getColumnProjections().size()));
        int count = 0;
        for (ColumnProjectionSegment each : actual.getOutputColumns()) {
            assertOutputColumnSegment(assertContext, each, expected.getOutputColumns().getColumnProjections().get(count));
            count++;
        }
    }
    
    private static void assertOutputColumnSegment(final SQLCaseAssertContext assertContext, final ColumnProjectionSegment actual, final ExpectedColumnProjection expected) { 
        assertThat(assertContext.getText("Output column name assertion error: "), 
                actual.getColumn().getIdentifier().getValue(), is(expected.getName()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertOutputTableSegment(final SQLCaseAssertContext assertContext, final OutputSegment actual, final ExpectedOutputClause expected) {
        assertThat(assertContext.getText("Output table name assertion error: "),
                actual.getTableName().getIdentifier().getValue(), is(expected.getOutputTable().getName()));
        SQLSegmentAssert.assertIs(assertContext, actual.getTableName(), expected.getOutputTable());
    }
    
    private static void assertOutputTableColumnSegment(final SQLCaseAssertContext assertContext, final OutputSegment actual, final ExpectedOutputClause expected) {
        assertThat(assertContext.getText("Output table columns size assertion error: "),
                actual.getTableColumns().size(), is(expected.getOutputTableColumns().getColumns().size()));
        int count = 0;
        for (ColumnSegment each : actual.getTableColumns()) {
            assertThat(assertContext.getText("Output table column name assertion error: "), 
                    each.getIdentifier().getValue(), is(expected.getOutputTableColumns().getColumns().get(count).getName()));
            SQLSegmentAssert.assertIs(assertContext, each, expected.getOutputTableColumns().getColumns().get(count));
            count++;
        }
    }
}
