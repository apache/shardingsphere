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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.migration.update;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.RollbackMigrationStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline.JobIdAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.RollbackMigrationStatementTestCase;

/**
 * Rollback migration statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RollbackMigrationStatementAssert {
    
    /**
     * Assert whether parsed rollback migration statement is correct or not.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RollbackMigrationStatement actual, final RollbackMigrationStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            JobIdAssert.assertJobId(assertContext, actual.getJobId(), expected.getJobId());
        }
    }
}
