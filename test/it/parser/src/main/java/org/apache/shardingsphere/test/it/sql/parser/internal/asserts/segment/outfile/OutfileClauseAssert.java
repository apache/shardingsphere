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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.outfile;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.outfile.OutfileSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.outfile.ExpectedOutfileClause;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Outfile clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OutfileClauseAssert {
    
    /**
     * Assert outfile segment is correct with expected outfile clause.
     *
     * @param assertContext assert context
     * @param actual actual outfile segment
     * @param expected expected outfile clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OutfileSegment actual, final ExpectedOutfileClause expected) {
        assertFilePath(assertContext, actual.getFilePath(), expected.getFilePath());
        assertFormat(assertContext, actual.getFormat().orElse(null), expected.getFormat());
        assertProperties(assertContext, actual.getProperties().orElse(null), expected.getProperties());
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertFilePath(final SQLCaseAssertContext assertContext, final String actual, final String expected) {
        assertThat(assertContext.getText("Outfile path assertion error: "), actual, is(expected));
    }
    
    private static void assertFormat(final SQLCaseAssertContext assertContext, final String actual, final String expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual outfile format should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual outfile format should exist."));
            assertThat(assertContext.getText("Outfile format assertion error: "), actual, is(expected));
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final Map<String, String> actual, final Map<String, String> expected) {
        if (null == expected || expected.isEmpty()) {
            if (null != actual && !actual.isEmpty()) {
                assertThat(assertContext.getText("Actual outfile properties should be empty."), actual.isEmpty(), is(true));
            }
        } else {
            assertNotNull(actual, assertContext.getText("Actual outfile properties should exist."));
            assertThat(assertContext.getText("Outfile properties size assertion error: "), actual.size(), is(expected.size()));
            for (Map.Entry<String, String> entry : expected.entrySet()) {
                assertThat(assertContext.getText(String.format("Outfile property '%s' assertion error: ", entry.getKey())),
                        actual.get(entry.getKey()), is(entry.getValue()));
            }
        }
    }
}
