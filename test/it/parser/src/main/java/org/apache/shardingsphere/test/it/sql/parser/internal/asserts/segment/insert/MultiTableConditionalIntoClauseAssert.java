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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.table.MultiTableConditionalIntoWhenThenSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedMultiTableConditionalIntoClause;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Multi table conditional into assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiTableConditionalIntoClauseAssert {
    
    /**
     * Assert actual multi table conditional into segment is correct with expected multi table conditional into segment.
     *
     * @param assertContext assert context
     * @param actual actual multi table conditional into segment
     * @param expected expected multi table conditional into segment
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MultiTableConditionalIntoSegment actual, final ExpectedMultiTableConditionalIntoClause expected) {
        assertThat(assertContext.getText("Conditional into when then segment size assertion error: "), actual.getWhenThenSegments().size(), is(expected.getConditionalIntoWhenThenClauses().size()));
        int index = 0;
        for (MultiTableConditionalIntoWhenThenSegment each : actual.getWhenThenSegments()) {
            ExpressionAssert.assertExpression(assertContext, each.getWhenSegment(), expected.getConditionalIntoWhenThenClauses().get(index).getWhenClause());
            MultiTableConditionalIntoThenSegmentAssert.assertIs(assertContext, each.getThenSegment(), expected.getConditionalIntoWhenThenClauses().get(index).getThenClause());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
            index++;
        }
        if (null == expected.getElseClause()) {
            assertFalse(actual.getElseSegment().isPresent(), assertContext.getText("Actual multi table conditional into else segment should not exist."));
        } else {
            assertTrue(actual.getElseSegment().isPresent(), assertContext.getText("Actual multi table conditional into else segment should exist."));
            MultiTableConditionalIntoElseSegmentAssert.assertIs(assertContext, actual.getElseSegment().get(), expected.getElseClause());
        }
    }
}
