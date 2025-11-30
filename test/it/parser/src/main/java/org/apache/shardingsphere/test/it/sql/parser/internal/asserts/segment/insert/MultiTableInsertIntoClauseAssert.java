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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.InsertStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedMultiTableInsertIntoClause;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Multi table insert into assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiTableInsertIntoClauseAssert {
    
    /**
     * Assert actual multi table insert into segment is correct with expected multi table insert into segment.
     *
     * @param assertContext assert context
     * @param actual actual multi table insert into segment
     * @param expected expected multi table insert into segment
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MultiTableInsertIntoSegment actual, final ExpectedMultiTableInsertIntoClause expected) {
        assertThat(assertContext.getText("Insert values size assertion error: "), actual.getInsertStatements().size(), is(expected.getInsertTestCases().size()));
        int count = 0;
        for (InsertStatement each : actual.getInsertStatements()) {
            InsertStatementAssert.assertIs(assertContext, each, expected.getInsertTestCases().get(count));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
            count++;
        }
    }
}
