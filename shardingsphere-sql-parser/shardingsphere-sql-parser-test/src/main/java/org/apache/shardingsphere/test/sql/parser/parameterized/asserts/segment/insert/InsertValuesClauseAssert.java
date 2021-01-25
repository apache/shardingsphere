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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.assignment.AssignmentValueAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.insert.ExpectedInsertValue;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.insert.ExpectedInsertValuesClause;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Insert values clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertValuesClauseAssert {
    
    /**
     * Assert actual insert values segment is correct with expected insert values clause.
     *
     * @param assertContext assert context
     * @param actual actual insert values segment
     * @param expected expected insert values clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Collection<InsertValuesSegment> actual, final ExpectedInsertValuesClause expected) {
        assertThat(assertContext.getText("Insert values size assertion error: "), actual.size(), is(expected.getValues().size()));
        int count = 0;
        for (InsertValuesSegment each : actual) {
            assertInsertValues(assertContext, each, expected.getValues().get(count));
            count++;
        }
    }
    
    private static void assertInsertValues(final SQLCaseAssertContext assertContext, final InsertValuesSegment actual, final ExpectedInsertValue expected) {
        assertThat(assertContext.getText("Insert assignment value size assertion error: "), actual.getValues().size(), is(expected.getAssignmentValues().size()));
        int count = 0;
        for (ExpressionSegment each : actual.getValues()) {
            AssignmentValueAssert.assertIs(assertContext, each, expected.getAssignmentValues().get(count));
            count++;
        }
    }
}
