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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingConfigurationSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.PropertiesAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedAlgorithm;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.sharding.scaling.ExpectedInputOrOutputSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.sharding.scaling.ExpectedShardingScalingConfigurationSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingScalingStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Create sharding scaling statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateShardingScalingStatementAssert {
    
    /**
     * Assert create sharding scaling statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create sharding scaling statement
     * @param expected expected create sharding scaling statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateShardingScalingStatement actual, final CreateShardingScalingStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s scaling name assertion error: ", actual.getClass().getSimpleName())),
                    actual.getScalingName(), is(expected.getScalingName()));
            assertShardingScalingConfiguration(assertContext, actual.getConfigurationSegment(), expected.getConfigurationSegment());
        }
    }
    
    private static void assertShardingScalingConfiguration(final SQLCaseAssertContext assertContext, final ShardingScalingConfigurationSegment actual, 
                                                           final ExpectedShardingScalingConfigurationSegment expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding scaling configuration segment should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding scaling configuration segment should exist."), actual);
            assertInputOrOutputSegment("input", assertContext, actual.getInputSegment(), expected.getInputSegment());
            assertInputOrOutputSegment("output", assertContext, actual.getOutputSegment(), expected.getOutputSegment());
            assertAlgorithmSegment("stream channel", assertContext, actual.getStreamChannel(), expected.getStreamChannel());
            assertAlgorithmSegment("completion detector", assertContext, actual.getCompletionDetector(), expected.getCompletionDetector());
            assertAlgorithmSegment("data consistency checker", assertContext, actual.getDataConsistencyChecker(), expected.getDataConsistencyChecker());
        }
    }
    
    private static void assertInputOrOutputSegment(final String type, final SQLCaseAssertContext assertContext, final InputOrOutputSegment actual, final ExpectedInputOrOutputSegment expected) {
        if (null == expected) {
            assertNull(assertContext.getText(String.format("Actual %s configuration segment should not exist.", type)), actual);
        } else {
            assertNotNull(assertContext.getText(String.format("Actual %s configuration segment should exist.", type)), actual);
            assertThat(assertContext.getText(String.format("`%s`'s worker thread assertion error: ", actual.getClass().getSimpleName())),
                    actual.getWorkerThread(), is(expected.getWorkerThread()));
            assertThat(assertContext.getText(String.format("`%s`'s batch size assertion error: ", actual.getClass().getSimpleName())),
                    actual.getBatchSize(), is(expected.getBatchSize()));
            assertThat(assertContext.getText(String.format("`%s`'s rate limiter type assertion error: ", actual.getClass().getSimpleName())),
                    actual.getRateLimiter().getName(), is(expected.getRateLimiter().getName()));
            PropertiesAssert.assertIs(assertContext, actual.getRateLimiter().getProps(), expected.getRateLimiter().getProps());
        }
    }
    
    private static void assertAlgorithmSegment(final String type, final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final ExpectedAlgorithm expected) {
        if (null == expected) {
            assertNull(assertContext.getText(String.format("Actual %s segment should not exist.", type)), actual);
        } else {
            assertNotNull(assertContext.getText(String.format("Actual %s segment should exist.", type)), actual);
            assertThat(assertContext.getText(String.format("`%s`'s type assertion error: ", actual.getClass().getSimpleName())),
                    actual.getName(), is(expected.getName()));
            PropertiesAssert.assertIs(assertContext, actual.getProps(), expected.getProps());
        }
    }
}
