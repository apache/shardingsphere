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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowRoutineLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowRoutineLoadStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Show routine load statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowRoutineLoadStatementAssert {
    
    /**
     * Assert show routine load statement.
     *
     * @param assertContext assert context
     * @param actual actual show routine load statement
     * @param expected expected show routine load statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowRoutineLoadStatement actual, final DorisShowRoutineLoadStatementTestCase expected) {
        assertThat(assertContext.getText("Show all flag does not match: "), actual.isShowAll(), is(expected.isShowAll()));
        if (null != expected.getJobName() && actual.getJobName().isPresent()) {
            assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().get().getIdentifier().getValue(), is(expected.getJobName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getJobName().get(), expected.getJobName());
            if (null != expected.getJobName().getDatabase() && actual.getJobName().get().getDatabase().isPresent()) {
                DatabaseAssert.assertIs(assertContext, actual.getJobName().get().getDatabase().get(), expected.getJobName().getDatabase());
            }
        }
    }
}
