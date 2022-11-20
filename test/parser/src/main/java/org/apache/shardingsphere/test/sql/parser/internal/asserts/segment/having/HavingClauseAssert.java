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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.having;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.having.ExpectedHavingClause;

/**
 * Having clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HavingClauseAssert {
    
    /**
     * Assert actual having segment is correct with expected having clause.
     * 
     * @param assertContext assert context
     * @param actual actual having segment
     * @param expected expected having clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final HavingSegment actual, final ExpectedHavingClause expected) {
        ExpressionAssert.assertExpression(assertContext, actual.getExpr(), expected.getExpr());
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
