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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.PropertiesAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedAlgorithm;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExceptedReadwriteSplittingRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Readwrite-splitting rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingRuleAssert {
    
    /**
     * Assert readwrite-splitting rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual readwrite-splitting rule
     * @param expected expected readwrite-splitting rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ReadwriteSplittingRuleSegment actual, final ExceptedReadwriteSplittingRule expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual readwrite-splitting rule should not exit."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual readwrite-splitting rule should exit."));
            assertThat(assertContext.getText(String.format("`%s`'s readwrite-splitting rule segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getName(), is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s readwrite-splitting rule segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getWriteDataSource(), is(expected.getWriteDataSource()));
            assertThat(assertContext.getText(String.format("`%s`'s readwrite-splitting rule segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getReadDataSources(), is(expected.getReadDataSources()));
            assertTransactionalReadQueryStrategy(assertContext, actual, expected);
            assertLoadBalancer(assertContext, actual.getLoadBalancer(), expected.getLoadBalancer());
        }
    }
    
    private static void assertTransactionalReadQueryStrategy(final SQLCaseAssertContext assertContext, final ReadwriteSplittingRuleSegment actual, final ExceptedReadwriteSplittingRule expected) {
        if (null == expected.getTransactionalReadQueryStrategy()) {
            assertNull(actual.getTransactionalReadQueryStrategy(), assertContext.getText("Actual transactional read query strategy should not exist."));
        } else {
            assertNotNull(actual.getTransactionalReadQueryStrategy(), assertContext.getText("Actual transactional read query strategy should exist."));
            assertThat(assertContext.getText("Transactional read query strategy assertion error"), actual.getTransactionalReadQueryStrategy(), is(expected.getTransactionalReadQueryStrategy()));
        }
    }
    
    private static void assertLoadBalancer(final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final ExpectedAlgorithm expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual load balancer should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual load balancer should exist."));
            assertThat(assertContext.getText("Load balancer assertion error"), actual.getName(), is(expected.getName()));
            PropertiesAssert.assertIs(assertContext, actual.getProps(), expected.getProperties());
        }
    }
}
