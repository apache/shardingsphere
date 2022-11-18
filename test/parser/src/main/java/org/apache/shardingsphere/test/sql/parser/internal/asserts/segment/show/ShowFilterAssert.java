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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.show;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.show.ExpectedShowFilter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Show filter assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowFilterAssert {
    
    /**
     * Assert actual show filter segment is correct with expected schema.
     *
     * @param assertContext assert context
     * @param actual actual show filter segment
     * @param expected expected show filter
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowFilterSegment actual, final ExpectedShowFilter expected) {
        if (actual.getLike().isPresent()) {
            assertThat(assertContext.getText("Show filter like segment pattern content assert error."),
                    actual.getLike().get().getPattern(), is(expected.getLike().getPattern()));
            SQLSegmentAssert.assertIs(assertContext, actual.getLike().get(), expected.getLike());
        }
        if (actual.getWhere().isPresent()) {
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhere());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
