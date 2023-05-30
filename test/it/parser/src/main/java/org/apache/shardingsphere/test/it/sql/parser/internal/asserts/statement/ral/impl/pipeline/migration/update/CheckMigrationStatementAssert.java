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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.pipeline.migration.update;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.pipeline.JobIdAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedAlgorithm;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.CheckMigrationStatementTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Check migration statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CheckMigrationStatementAssert {
    
    /**
     * Assert check migration statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual check migration statement
     * @param expected expected check migration statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CheckMigrationStatement actual, final CheckMigrationStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            JobIdAssert.assertJobId(assertContext, actual.getJobId(), expected.getJobId());
            assertTypeStrategy(assertContext, actual.getTypeStrategy(), expected.getTableStrategies());
        }
    }
    
    private static void assertTypeStrategy(final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final List<ExpectedAlgorithm> expected) {
        if (expected.isEmpty()) {
            assertNull(actual, assertContext.getText("Actual type strategy should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual type strategy should exist."));
            assertThat(assertContext.getText("Type strategy assertion error"), actual.getName(), is(expected.iterator().next().getName()));
        }
    }
}
