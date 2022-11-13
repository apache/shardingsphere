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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dml.impl.InsertStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.insert.ExpectedInsertMultiTableElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Insert multi table element assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertMultiTableElementAssert {
    
    /**
     * Assert actual insert multi table element segment is correct with expected multi table element.
     *
     * @param assertContext assert context
     * @param actual actual insert multi table element
     * @param expected expected insert multi table element
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final InsertMultiTableElementSegment actual, final ExpectedInsertMultiTableElement expected) {
        assertThat(assertContext.getText("Insert values size assertion error: "), actual.getInsertStatements().size(), is(expected.getInsertTestCases().size()));
        int count = 0;
        for (InsertStatement each : actual.getInsertStatements()) {
            InsertStatementAssert.assertIs(assertContext, each, expected.getInsertTestCases().get(count));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
            count++;
        }
    }
}
