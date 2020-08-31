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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.JoinSpecificationAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedJoinTable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.JoinedTableSegment;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

/**
 * JoinTable assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinTableAssert {
    
    /**
     * Assert actual joinTable segments is correct with expected joinTables.
     *
     * @param assertContext assert context
     * @param actual actual joinTables
     * @param expected expected joinTables
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final JoinedTableSegment actual, final ExpectedJoinTable expected) {
        TableFactorAssert.assertIs(assertContext, actual.getTableFactor(), expected.getTableFactor());
        if (null != actual.getJoinSpecification()) {
            JoinSpecificationAssert.assertIs(assertContext, actual.getJoinSpecification(), expected.getJoinSpecification());
        }
    }
    
    public static void assertIs(final SQLCaseAssertContext assertContext, final List<JoinedTableSegment> actual, final List<ExpectedJoinTable> expected) {
        assertThat(assertContext.getText("JoinTable size assert error"), actual.size(), is(null == expected ? 0 : expected.size()));
        for (int i = 0; i < actual.size(); i++) {
            TableFactorAssert.assertIs(assertContext, actual.get(i).getTableFactor(), expected.get(i).getTableFactor());
            if (null != expected.get(i).getJoinSpecification()) {
                assertNotNull(assertContext.getText("Actual JoinSpecification segment should exist."), actual.get(i).getJoinSpecification());
                JoinSpecificationAssert.assertIs(assertContext, actual.get(i).getJoinSpecification(), expected.get(i).getJoinSpecification());
            } else {
                assertNull(assertContext.getText("Actual JoinSpecification segment should not exist."), actual.get(i).getJoinSpecification());
            }
        }
    }
}
