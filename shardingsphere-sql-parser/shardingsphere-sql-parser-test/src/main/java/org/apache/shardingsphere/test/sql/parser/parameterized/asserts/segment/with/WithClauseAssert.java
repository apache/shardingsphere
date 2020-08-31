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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.with;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.WithSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.with.ExpectedCommonTableExpressionClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.with.ExpectedWithClause;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * With clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WithClauseAssert {
    
    /**
     * Assert actual with segment is correct with expected with clause.
     *
     * @param assertContext assert context
     * @param actual actual with segment
     * @param expected expected with clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final WithSegment actual, final ExpectedWithClause expected) {
        assertNotNull(assertContext.getText("On duplicate key columns should exist."), expected);
        assertThat(assertContext.getText("On duplicate key columns size assertion error: "), 
                actual.getCommonTableExpressions().size(), is(expected.getCommonTableExpressions().size()));
        int count = 0;
        for (CommonTableExpressionSegment each : actual.getCommonTableExpressions()) {
            assertCommonTableExpressionSegment(assertContext, each, expected.getCommonTableExpressions().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertCommonTableExpressionSegment(final SQLCaseAssertContext assertContext, final CommonTableExpressionSegment actual, final ExpectedCommonTableExpressionClause expected) { 
        if (null != expected.getCommonTableExpressColumns()) {
            assertThat(assertContext.getText("Common table expression column size assertion error: "), actual.getColumns().size(), is(expected.getCommonTableExpressColumns().getColumns().size()));
        }
        assertThat(assertContext.getText("Common table expression name assertion error: "), actual.getIdentifier().getValue(), is(expected.getName()));
        int count = 0;
        for (ColumnSegment each : actual.getColumns()) {
            ColumnAssert.assertIs(assertContext, each, expected.getCommonTableExpressColumns().getColumns().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
