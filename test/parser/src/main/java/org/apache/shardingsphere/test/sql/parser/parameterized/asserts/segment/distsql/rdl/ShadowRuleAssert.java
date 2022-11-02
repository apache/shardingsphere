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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShadowAlgorithm;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShadowRule;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedShadowRule.ExpectedShadowTableRule;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Shadow table rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRuleAssert {
    
    /**
     * Assert shadow rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual shadow rule
     * @param expected expected shadow rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShadowRuleSegment actual, final ExpectedShadowRule expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual shadow rule should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual shadow rule should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s shadow rule segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getShadow(), is(expected.getShadow()));
            assertThat(assertContext.getText(String.format("`%s`'s shadow rule segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getSource(), is(expected.getSource()));
            assertThat(assertContext.getText(String.format("`%s`'s shadow rule segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getRuleName(), is(expected.getRuleName()));
            for (ExpectedShadowTableRule each : expected.getShadowTableRules()) {
                assertIsTableRules(assertContext, actual.getShadowTableRules().get(each.getTableName()), each.getAlgorithms());
            }
        }
    }
    
    private static void assertIsTableRules(final SQLCaseAssertContext assertContext, final Collection<ShadowAlgorithmSegment> actual,
                                           final Collection<ExpectedShadowAlgorithm> expected) {
        assertNotNull(actual);
        Map<String, ShadowAlgorithmSegment> actualMap = actual.stream().collect(Collectors.toMap(ShadowAlgorithmSegment::getAlgorithmName, each -> each));
        expected.forEach(each -> assertIsAlgorithmsSegment(assertContext, actualMap.get(each.getAlgorithmName()), each));
    }
    
    private static void assertIsAlgorithmsSegment(final SQLCaseAssertContext assertContext, final ShadowAlgorithmSegment actual,
                                                  final ExpectedShadowAlgorithm expected) {
        assertNotNull(actual);
        AlgorithmAssert.assertIs(assertContext, actual.getAlgorithmSegment(), expected.getAlgorithmSegment());
    }
}
