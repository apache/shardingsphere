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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.ViewColumnSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedViewColumn;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * View column assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewColumnAssert {
    
    /**
     * Assert actual view column segment is correct with expected view column.
     *
     * @param assertContext assert context
     * @param actual actual view column segment
     * @param expected expected view column
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ViewColumnSegment actual, final ExpectedViewColumn expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getColumn().getIdentifier(), expected, "View column");
        if (null == expected.getComment()) {
            assertFalse(actual.getComment().isPresent(), assertContext.getText("Actual view column comment should not exist."));
        } else {
            assertTrue(actual.getComment().isPresent(), assertContext.getText("Actual view column comment should exist."));
            assertThat(assertContext.getText("View column comment assertion error: "), actual.getComment().get(), is(expected.getComment()));
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert actual view column segments is correct with expected view columns.
     *
     * @param assertContext assert context
     * @param actual actual view columns
     * @param expected expected view columns
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Collection<ViewColumnSegment> actual, final Collection<ExpectedViewColumn> expected) {
        assertThat(assertContext.getText("View columns size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        List<ExpectedViewColumn> expectedList = new java.util.ArrayList<>(expected);
        for (ViewColumnSegment each : actual) {
            assertIs(assertContext, each, expectedList.get(count));
            count++;
        }
    }
}
