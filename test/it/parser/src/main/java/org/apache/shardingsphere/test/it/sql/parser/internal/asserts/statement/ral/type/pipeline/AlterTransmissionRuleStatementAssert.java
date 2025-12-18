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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.pipeline;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedAlgorithm;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ral.ExpectedRead;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ral.ExpectedWrite;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterTransmissionRuleStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Alter transmission rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterTransmissionRuleStatementAssert {
    
    /**
     * Assert statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual statement
     * @param expected expected statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterTransmissionRuleStatement actual, final AlterTransmissionRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual statement should not exist."));
        } else {
            assertThat(actual.getJobTypeName(), is(expected.getJobTypeName()));
            assertRead(assertContext, actual.getProcessConfigSegment().getReadSegment(), expected.getRule().getRead());
            assertWrite(assertContext, actual.getProcessConfigSegment().getWriteSegment(), expected.getRule().getWrite());
            assertTypeStrategy(assertContext, actual.getProcessConfigSegment().getStreamChannel(), expected.getRule().getStreamChannel());
        }
    }
    
    private static void assertRead(final SQLCaseAssertContext assertContext, final ReadOrWriteSegment actual, final ExpectedRead expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual read or write should not exist."));
            return;
        }
        assertThat(actual.getWorkerThread(), is(expected.getWorkerThread()));
        assertThat(actual.getBatchSize(), is(expected.getBatchSize()));
        assertThat(actual.getShardingSize(), is(expected.getShardingSize()));
        assertAlgorithm(assertContext, actual.getRateLimiter(), expected.getRateLimiter(), "rate limiter");
    }
    
    private static void assertWrite(final SQLCaseAssertContext assertContext, final ReadOrWriteSegment actual, final ExpectedWrite expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual read or write should not exist."));
            return;
        }
        assertThat(actual.getWorkerThread(), is(expected.getWorkerThread()));
        assertThat(actual.getBatchSize(), is(expected.getBatchSize()));
        assertAlgorithm(assertContext, actual.getRateLimiter(), expected.getRateLimiter(), "rate limiter");
    }
    
    private static void assertTypeStrategy(final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final ExpectedAlgorithm expected) {
        assertAlgorithm(assertContext, actual, expected, "strategy");
    }
    
    private static void assertAlgorithm(final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final ExpectedAlgorithm expected, final String subject) {
        if (null == expected) {
            assertNull(actual, assertContext.getText(String.format("Actual %s should not exist.", subject)));
        } else {
            assertNotNull(actual, assertContext.getText(String.format("Actual %s should exist.", subject)));
            assertThat(assertContext.getText("Type assertion error"), actual.getName(), is(expected.getName()));
        }
    }
}
