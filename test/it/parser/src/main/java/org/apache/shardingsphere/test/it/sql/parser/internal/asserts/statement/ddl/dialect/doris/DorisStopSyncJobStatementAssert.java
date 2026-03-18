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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisStopSyncJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisStopSyncJobStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Stop sync job statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisStopSyncJobStatementAssert {
    
    /**
     * Assert stop sync job statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual stop sync job statement
     * @param expected expected stop sync job statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisStopSyncJobStatement actual, final DorisStopSyncJobStatementTestCase expected) {
        if (actual.getJobName().isPresent()) {
            assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().get().getIdentifier().getValue(), is(expected.getJobName()));
            if (null != expected.getOwner()) {
                OwnerAssert.assertIs(assertContext, actual.getJobName().get().getOwner().orElse(null), expected.getOwner());
            }
        }
    }
}
