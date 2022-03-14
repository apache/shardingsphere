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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.scaling.query;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedAlgorithm;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.CheckScalingStatementTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Check scaling statement assert.
 */
public final class CheckScalingStatementAssert {
    
    /**
     * Assert check scaling statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual check scaling statement
     * @param expected expected check scaling statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CheckScalingStatement actual, final CheckScalingStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertJobIds(assertContext, actual.getJobId(), expected.getJobIds());
            assertTypeStrategy(assertContext, actual.getTypeStrategy(), expected.getTableStrategies());
        }
    }
    
    private static void assertJobIds(final SQLCaseAssertContext assertContext, final String actual, final List<String> expected) {
        if (expected.isEmpty()) {
            assertNull(assertContext.getText("Actual job id should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual job id should exist."), actual);
            assertThat(assertContext.getText("Job id assertion error"), actual, is(expected.iterator().next()));
        }
    }
    
    private static void assertTypeStrategy(final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final List<ExpectedAlgorithm> expected) {
        if (expected.isEmpty()) {
            assertNull(assertContext.getText("Actual type strategy should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual type strategy should exist."), actual);
            assertThat(assertContext.getText("Type strategy assertion error"), actual.getName(), is(expected.iterator().next().getName()));
        }
    }
}
