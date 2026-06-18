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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.hierarchical;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HierarchicalQuerySegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.hierarchical.ExpectedHierarchicalQueryClause;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Hierarchical query clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HierarchicalQueryClauseAssert {
    
    /**
     * Assert actual hierarchical query segment is correct with expected hierarchical query clause.
     *
     * @param assertContext assert context
     * @param actual actual hierarchical query segment
     * @param expected expected hierarchical query clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final HierarchicalQuerySegment actual, final ExpectedHierarchicalQueryClause expected) {
        assertThat(assertContext.getText("Hierarchical query no cycle assertion error: "), actual.isNoCycle(), is(expected.isNoCycle()));
        ExpressionAssert.assertExpression(assertContext, actual.getStartWith(), expected.getStartWith());
        ExpressionAssert.assertExpression(assertContext, actual.getConnectBy(), expected.getConnectBy());
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
