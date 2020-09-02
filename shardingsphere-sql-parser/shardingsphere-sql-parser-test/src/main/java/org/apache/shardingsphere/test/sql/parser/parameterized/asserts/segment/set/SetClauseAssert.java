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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.assignment.AssignmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.set.ExpectedSetClause;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Set clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SetClauseAssert {
    
    /**
     * Assert actual set assignment segment is correct with expected set assignment.
     * 
     * @param assertContext assert context
     * @param actual actual set assignment segment
     * @param expected expected set clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SetAssignmentSegment actual, final ExpectedSetClause expected) {
        assertNotNull(assertContext.getText("Assignments should exist."), expected);
        assertThat(assertContext.getText("Assignments size assertion error: "), actual.getAssignments().size(), is(expected.getAssignments().size()));
        int count = 0;
        for (AssignmentSegment each : actual.getAssignments()) {
            AssignmentAssert.assertIs(assertContext, each, expected.getAssignments().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
