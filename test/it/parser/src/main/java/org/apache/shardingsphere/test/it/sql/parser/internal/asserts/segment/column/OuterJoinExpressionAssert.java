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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.join.OuterJoinExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedOuterJoinExpression;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Outer join expression assert.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OuterJoinExpressionAssert {
    
    /**
     * Assert actual outer join expression is correct with expected outer join expression.
     *
     * @param assertContext assert context
     * @param actual actual outer join expression 
     * @param expected expected outer join expression 
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OuterJoinExpression actual, final ExpectedOuterJoinExpression expected) {
        ColumnAssert.assertIs(assertContext, actual.getColumnName(), expected.getColumn());
        assertThat(actual.getJoinOperator(), is(expected.getJoinOperator()));
    }
}
