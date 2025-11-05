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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.assignment;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ValueReferenceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.assignment.ExpectedValueReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Value reference segment assert.
 */
public final class ValueReferenceSegmentAssert {
    
    /**
     * Assert actual value reference segment is correct with expected value reference.
     *
     * @param assertContext assert context
     * @param actual actual value reference segment
     * @param expected expected value reference
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ValueReferenceSegment actual, final ExpectedValueReference expected) {
        assertThat(assertContext.getText("Value reference name assertion error: "), actual.getAlias().getIdentifier().getValue(), is(expected.getAlias()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        if (expected.getDerivedColumns().isPresent() && actual.getDerivedColumns().isPresent()) {
            assertThat(assertContext.getText("Value reference derived columns size assertion error: "),
                    actual.getDerivedColumns().get().size(), is(expected.getDerivedColumns().get().size()));
            int index = 0;
            for (ColumnSegment each : actual.getDerivedColumns().get()) {
                ColumnAssert.assertIs(assertContext, each, expected.getDerivedColumns().get().get(index));
                index++;
            }
        }
    }
}
