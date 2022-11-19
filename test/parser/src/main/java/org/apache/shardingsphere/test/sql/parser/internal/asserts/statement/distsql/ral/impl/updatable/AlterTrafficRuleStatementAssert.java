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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.ral.impl.updatable;

import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.distsql.ral.ExpectedTrafficRule;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.distsql.ral.AlterTrafficRuleStatementTestCase;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Alter traffic rule statement assert.
 */
public final class AlterTrafficRuleStatementAssert {
    
    /**
     * Assert alter traffic rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter traffic rule statement
     * @param expected expected alter traffic rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterTrafficRuleStatement actual, final AlterTrafficRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertTrafficRuleSegments(assertContext, actual.getSegments(), expected.getRules());
        }
    }
    
    private static void assertTrafficRuleSegments(final SQLCaseAssertContext assertContext, final Collection<TrafficRuleSegment> actual, final List<ExpectedTrafficRule> expected) {
        Map<String, TrafficRuleSegment> actualMap = actual.stream().collect(Collectors.toMap(TrafficRuleSegment::getName, each -> each));
        expected.forEach(each -> {
            TrafficRuleSegment actualRule = actualMap.get(each.getName());
            assertThat(actualRule.getName(), is(each.getName()));
            assertThat(actualRule.getLabels(), is(each.getLabels()));
            AlgorithmAssert.assertIs(assertContext, actualRule.getAlgorithm(), each.getTrafficAlgorithm());
            AlgorithmAssert.assertIs(assertContext, actualRule.getLoadBalancer(), each.getLoadBalancer());
        });
    }
}
