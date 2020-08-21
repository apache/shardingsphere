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

package org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.assignment.AssignmentAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.insert.ExpectedOnDuplicateKeyColumns;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * On duplicate key columns assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnDuplicateKeyColumnsAssert {
    
    /**
     * Assert actual on duplicate key columns segment is correct with expected on duplicate key columns.
     * 
     * @param assertContext assert context
     * @param actual actual on duplicate key columns segment
     * @param expected expected on duplicate key columns
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final OnDuplicateKeyColumnsSegment actual, final ExpectedOnDuplicateKeyColumns expected) {
        assertNotNull(assertContext.getText("On duplicate key columns should exist."), expected);
        assertThat(assertContext.getText("On duplicate key columns size assertion error: "), actual.getColumns().size(), is(expected.getAssignments().size()));
        int count = 0;
        for (AssignmentSegment each : actual.getColumns()) {
            AssignmentAssert.assertIs(assertContext, each, expected.getAssignments().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
