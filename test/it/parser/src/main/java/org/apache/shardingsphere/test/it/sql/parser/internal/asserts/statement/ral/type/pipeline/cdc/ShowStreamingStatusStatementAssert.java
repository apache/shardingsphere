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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.cdc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingStatusStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.JobIdAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.ShowStreamingStatusStatementTestCase;

/**
 * Show streaming status statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowStreamingStatusStatementAssert {
    
    /**
     * Assert show streaming status statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show streaming status statement
     * @param expected expected show streaming status statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowStreamingStatusStatement actual, final ShowStreamingStatusStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            JobIdAssert.assertJobId(assertContext, actual.getJobId(), expected.getJobId());
        }
    }
}
