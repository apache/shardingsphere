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
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.insert.ExpectedInsertColumnsClause;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Insert column clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertColumnsClauseAssert {
    
    /**
     * Assert actual insert columns segment is correct with expected insert columns clause.
     *
     * @param assertContext assert context
     * @param actual actual insert columns segment
     * @param expected expected insert columns clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final InsertColumnsSegment actual, final ExpectedInsertColumnsClause expected) {
        assertThat(assertContext.getText("Insert column size assertion error: "), actual.getColumns().size(), is(expected.getColumns().size()));
        int count = 0;
        for (ColumnSegment each : actual.getColumns()) {
            ColumnAssert.assertIs(assertContext, each, expected.getColumns().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
