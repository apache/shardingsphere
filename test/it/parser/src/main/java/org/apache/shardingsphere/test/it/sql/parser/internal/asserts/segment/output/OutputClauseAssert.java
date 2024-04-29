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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.output;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.output.ExpectedOutputClause;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertNotNull(expected, assertContext.getText("Output clause should exist."));
        if (null != actual.getOutputColumns()) {
            ProjectionAssert.assertIs(assertContext, actual.getOutputColumns(), expected.getOutputColumns());
        }
        if (null != actual.getTable()) {
            assertOutputTableSegment(assertContext, actual, expected);
        }
        if (!actual.getTableColumns().isEmpty()) {
            assertOutputTableColumnSegment(assertContext, actual, expected);
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertOutputTableSegment(final SQLCaseAssertContext assertContext, final OutputSegment actual, final ExpectedOutputClause expected) {
        TableAssert.assertIs(assertContext, actual.getTable(), expected.getOutputTable());
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
