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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.MaskRuleAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedMaskRule;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.CreateMaskRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Create mask rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateMaskRuleStatementAssert {
    
    /**
     * Assert create mask rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create mask rule statement
     * @param expected expected create mask rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateMaskRuleStatement actual, final CreateMaskRuleStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertThat(assertContext.getText("if not exists segment assertion error: "), actual.isIfNotExists(), is(expected.isIfNotExists()));
            assertMaskRules(assertContext, actual.getRules(), expected.getRules());
        }
    }
    
    private static void assertMaskRules(final SQLCaseAssertContext assertContext, final Collection<MaskRuleSegment> actual, final List<ExpectedMaskRule> expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual mask rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual mask rule should exist."));
            assertThat(assertContext.getText(String.format("Actual mask rule size should be %s, but it was %s", expected.size(), actual.size())), actual.size(), is(expected.size()));
            int count = 0;
            for (MaskRuleSegment each : actual) {
                ExpectedMaskRule expectedMaskRule = expected.get(count);
                assertThat(assertContext.getText("mask rule assertion error: "), each.getTableName(), is(expectedMaskRule.getName()));
                MaskRuleAssert.assertIs(assertContext, each, expectedMaskRule);
                count++;
            }
        }
    }
}
