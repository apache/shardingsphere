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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.generic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParenthesesSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.generic.ExpectedParentheses;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Parentheses assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParenthesesAssert {
    
    /**
     * Assert actual brackets segment is correct with expected parentheses.
     *
     * @param assertContext assert context
     * @param actual actual brackets segment
     * @param expected expected parentheses
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ParenthesesSegment actual, final ExpectedParentheses expected) {
        assertThat(assertContext.getText("Left brackets assertion error: "), actual.isLeft(), is(expected.isLeft()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
