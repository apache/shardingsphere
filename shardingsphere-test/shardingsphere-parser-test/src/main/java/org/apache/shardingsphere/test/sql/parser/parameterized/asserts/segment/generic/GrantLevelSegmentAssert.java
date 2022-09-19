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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.generic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.GrantLevelSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTable;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Grant level segment assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GrantLevelSegmentAssert {
    
    /**
     * Assert MySQL grant statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual grant level statement
     * @param expected expected grant statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final GrantLevelSegment actual, final List<ExpectedSimpleTable> expected) {
        if (null != expected && !expected.isEmpty()) {
            assertThat(expected.size(), is(1));
            assertThat(actual.getTableName(), is(expected.get(0).getName()));
        } else {
            assertNull(assertContext.getText("Actual table should not exist."), actual.getTableName());
        }
    }
}
