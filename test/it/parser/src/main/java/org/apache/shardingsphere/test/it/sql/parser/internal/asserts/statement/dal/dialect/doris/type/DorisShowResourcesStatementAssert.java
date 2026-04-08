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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowResourcesStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowResourcesStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Show resources statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowResourcesStatementAssert {
    
    /**
     * Assert show resources statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show resources statement
     * @param expected expected show resources statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowResourcesStatement actual, final DorisShowResourcesStatementTestCase expected) {
        assertNameCondition(assertContext, actual, expected);
        assertResourceTypeCondition(assertContext, actual, expected);
        assertLike(assertContext, actual, expected);
        assertOrderBy(assertContext, actual, expected);
        assertLimit(assertContext, actual, expected);
    }
    
    private static void assertNameCondition(final SQLCaseAssertContext assertContext, final DorisShowResourcesStatement actual, final DorisShowResourcesStatementTestCase expected) {
        if (null != expected.getNameCondition()) {
            assertNotNull(actual.getNameCondition().orElse(null), assertContext.getText("Actual name condition segment should exist."));
            assertThat(assertContext.getText("Name condition type does not match: "), actual.getNameCondition().get().getType(), is(expected.getNameCondition().getType()));
            assertThat(assertContext.getText("Name condition value does not match: "), actual.getNameCondition().get().getValue(), is(expected.getNameCondition().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual.getNameCondition().get(), expected.getNameCondition());
        } else {
            assertNull(actual.getNameCondition().orElse(null), assertContext.getText("Actual name condition segment should not exist."));
        }
    }
    
    private static void assertResourceTypeCondition(final SQLCaseAssertContext assertContext, final DorisShowResourcesStatement actual, final DorisShowResourcesStatementTestCase expected) {
        if (null != expected.getResourceTypeCondition()) {
            assertNotNull(actual.getResourceTypeCondition().orElse(null), assertContext.getText("Actual resource type condition segment should exist."));
            assertThat(assertContext.getText("Resource type value does not match: "), actual.getResourceTypeCondition().get().getValue(), is(expected.getResourceTypeCondition().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual.getResourceTypeCondition().get(), expected.getResourceTypeCondition());
        } else {
            assertNull(actual.getResourceTypeCondition().orElse(null), assertContext.getText("Actual resource type condition segment should not exist."));
        }
    }
    
    private static void assertLike(final SQLCaseAssertContext assertContext, final DorisShowResourcesStatement actual, final DorisShowResourcesStatementTestCase expected) {
        if (null != expected.getLike()) {
            assertNotNull(actual.getLike().orElse(null), assertContext.getText("Actual like segment should exist."));
            assertThat(assertContext.getText("Like pattern does not match: "), actual.getLike().get().getPattern(), is(expected.getLike().getPattern()));
            SQLSegmentAssert.assertIs(assertContext, actual.getLike().get(), expected.getLike());
        } else {
            assertNull(actual.getLike().orElse(null), assertContext.getText("Actual like pattern should not exist."));
        }
    }
    
    private static void assertOrderBy(final SQLCaseAssertContext assertContext, final DorisShowResourcesStatement actual, final DorisShowResourcesStatementTestCase expected) {
        if (null != expected.getOrderBy()) {
            assertNotNull(actual.getOrderBy().orElse(null), assertContext.getText("Actual order by segment should exist."));
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderBy());
        } else {
            assertNull(actual.getOrderBy().orElse(null), assertContext.getText("Actual order by segment should not exist."));
        }
    }
    
    private static void assertLimit(final SQLCaseAssertContext assertContext, final DorisShowResourcesStatement actual, final DorisShowResourcesStatementTestCase expected) {
        if (null != expected.getLimit()) {
            assertNotNull(actual.getLimit().orElse(null), assertContext.getText("Actual limit segment should exist."));
            SQLSegmentAssert.assertIs(assertContext, actual.getLimit().get(), expected.getLimit());
            LimitClauseAssert.assertRowCount(assertContext, actual.getLimit().get().getRowCount().orElse(null), expected.getLimit().getRowCount());
            LimitClauseAssert.assertOffset(assertContext, actual.getLimit().get().getOffset().orElse(null), expected.getLimit().getOffset());
        } else {
            assertNull(actual.getLimit().orElse(null), assertContext.getText("Actual limit segment should not exist."));
        }
    }
}
