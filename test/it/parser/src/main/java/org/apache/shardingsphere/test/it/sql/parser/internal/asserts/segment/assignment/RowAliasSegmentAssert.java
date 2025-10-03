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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.RowAliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.assignment.ExpectedRowAlias;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Row alias segment assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RowAliasSegmentAssert {
    
    /**
     * Assert actual row alias segment is correct with expected row alias.
     *
     * @param assertContext assert context
     * @param actual actual row alias segment
     * @param expected expected row alias
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RowAliasSegment actual, final ExpectedRowAlias expected) {
        assertThat(assertContext.getText("Row alias name assertion error: "), actual.getAlias().getIdentifier().getValue(), is(expected.getAlias()));
        if (null != expected.getDerivedColumns() && !expected.getDerivedColumns().isEmpty()) {
            assertThat(assertContext.getText("Row alias derived columns size assertion error: "),
                    actual.getDerivedColumns().isPresent(), is(true));
            assertThat(assertContext.getText("Row alias derived columns size assertion error: "),
                    actual.getDerivedColumns().get().size(), is(expected.getDerivedColumns().size()));
            int count = 0;
            for (ColumnSegment each : actual.getDerivedColumns().get()) {
                ColumnAssert.assertIs(assertContext, each, expected.getDerivedColumns().get(count));
                count++;
            }
        } else {
            assertThat(assertContext.getText("Row alias derived columns assertion error: "),
                    actual.getDerivedColumns().isPresent(), is(false));
        }
    }
}
