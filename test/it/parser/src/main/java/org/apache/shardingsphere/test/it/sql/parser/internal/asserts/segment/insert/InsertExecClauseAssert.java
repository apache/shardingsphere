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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.exec.ExecSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.exec.ExpectedExecClause;
import org.hamcrest.CoreMatchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Insert execute clause assert.
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertExecClauseAssert {
    
    /**
     *  Assert actual execute segment is correct with expected execute clause.
     *
     * @param assertContext assert context
     * @param actual actual execute segment
     * @param expected expected execute clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ExecSegment actual, final ExpectedExecClause expected) {
        assertThat(assertContext.getText("exec procedure name assertion error: "), actual.getProcedureName().getIdentifier().getValue(), CoreMatchers.is(expected.getName()));
        if (null == expected.getOwner()) {
            assertFalse(actual.getProcedureName().getOwner().isPresent(), assertContext.getText("Actual owner should not exist."));
        } else {
            assertTrue(actual.getProcedureName().getOwner().isPresent(), assertContext.getText("Actual owner should exist."));
            OwnerAssert.assertIs(assertContext, actual.getProcedureName().getOwner().get(), expected.getOwner());
        }
        if (null == expected.getParameters()) {
            assertThat(assertContext.getText("exec procedure parameters  assertion error: "), actual.getExpressionSegments().size(), CoreMatchers.is(expected.getParameters().size()));
        } else {
            int count = 0;
            for (ExpressionSegment expressionSegment : actual.getExpressionSegments()) {
                ExpressionAssert.assertExpression(assertContext, expressionSegment, expected.getParameters().get(count));
                count++;
            }
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
