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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.window;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.orderby.ExpectedOrderByClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.window.ExpectedWindowClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.window.ExpectedWindowItem;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowClauseAssert {
    
    /**
     * Assert actual window segment is correct with expected window clause.
     *
     * @param assertContext assert context
     * @param actual actual window segment
     * @param expected expected window clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final WindowSegment actual, final ExpectedWindowClause expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        assertThat(assertContext.getText("Window items size assertion error: "), actual.getItemSegments().size(), is(expected.getWindowItems().size()));
        Iterator<ExpectedWindowItem> expectedWindowItemIterator = expected.getWindowItems().iterator();
        Iterator<WindowItemSegment> windowItemIterator = actual.getItemSegments().iterator();
        while (expectedWindowItemIterator.hasNext()) {
            ExpectedOrderByClause expectedOrderByClause = expectedWindowItemIterator.next().getOrderByClause();
            OrderBySegment orderBySegment = windowItemIterator.next().getOrderBySegment();
            if (null != expectedOrderByClause) {
                OrderByClauseAssert.assertIs(assertContext, orderBySegment, expectedOrderByClause);
            }
        }
    }
}
