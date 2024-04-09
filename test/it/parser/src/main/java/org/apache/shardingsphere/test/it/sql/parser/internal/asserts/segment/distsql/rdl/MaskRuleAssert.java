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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedMaskColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedMaskRule;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Mask rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaskRuleAssert {
    
    /**
     * Assert mask rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual mask rule
     * @param expected expected mask rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MaskRuleSegment actual, final ExpectedMaskRule expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual mask rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual mask rule should exist."));
            assertThat(assertContext.getText("mask rule assertion error: "), actual.getTableName(), is(expected.getName()));
            assertMaskColumns(assertContext, actual.getColumns(), expected.getColumns());
        }
    }
    
    private static void assertMaskColumns(final SQLCaseAssertContext assertContext, final Collection<MaskColumnSegment> actual, final List<ExpectedMaskColumn> expected) {
        if (expected.isEmpty()) {
            assertNull(actual, assertContext.getText("Actual mask column should not exist."));
        } else {
            assertFalse(actual.isEmpty(), assertContext.getText("Actual mask column should exist."));
            assertThat(assertContext.getText(String.format("Actual mask column size should be %s, but it was %s.", expected.size(), actual.size())), actual.size(), is(expected.size()));
            int count = 0;
            for (MaskColumnSegment each : actual) {
                ExpectedMaskColumn expectedMaskColumn = expected.get(count);
                MaskColumnAssert.assertIs(assertContext, each, expectedMaskColumn);
                count++;
            }
        }
    }
}
