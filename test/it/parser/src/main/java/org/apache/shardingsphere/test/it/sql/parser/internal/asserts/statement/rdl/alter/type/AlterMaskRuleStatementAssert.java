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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.AlterMaskRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.MaskRuleAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedMaskRule;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.AlterMaskRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Alter mask rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterMaskRuleStatementAssert {
    
    /**
     * Assert alter mask rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter mask rule statement
     * @param expected expected alter mask rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterMaskRuleStatement actual, final AlterMaskRuleStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertMaskRules(assertContext, actual.getRules(), expected.getRules());
        }
    }
    
    private static void assertMaskRules(final SQLCaseAssertContext assertContext, final Collection<MaskRuleSegment> actual, final List<ExpectedMaskRule> expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual mask rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual mask rule should exist."));
            assertThat(assertContext.getText(String.format("Actual mask rule size should be %s , but it was %s", expected.size(), actual.size())), actual.size(), is(expected.size()));
            int count = 0;
            for (MaskRuleSegment each : actual) {
                ExpectedMaskRule expectedMaskRule = expected.get(count);
                assertThat(assertContext.getText("mask rule name assertion error: "), each.getTableName(), is(expectedMaskRule.getName()));
                MaskRuleAssert.assertIs(assertContext, each, expectedMaskRule);
            }
        }
    }
}
