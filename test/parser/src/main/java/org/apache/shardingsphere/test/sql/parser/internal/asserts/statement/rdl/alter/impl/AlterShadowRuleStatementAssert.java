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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.rdl.alter.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.rdl.ShadowRuleAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.distsql.rdl.ExpectedShadowRule;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.rdl.rule.shadow.AlterShadowRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Alter shadow rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterShadowRuleStatementAssert {
    
    /**
     * Assert alter shadow rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter shadow rule statement
     * @param expected expected alter shadow rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterShadowRuleStatement actual, final AlterShadowRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertShadowRule(assertContext, actual.getRules(), expected.getRules());
        }
    }
    
    private static void assertShadowRule(final SQLCaseAssertContext assertContext, final Collection<ShadowRuleSegment> actual, final List<ExpectedShadowRule> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual shadow rule should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual shadow rule should exist."), actual);
            int count = 0;
            for (ShadowRuleSegment tableRuleSegment : actual) {
                ExpectedShadowRule expectedTableRule = expected.get(count);
                ShadowRuleAssert.assertIs(assertContext, tableRuleSegment, expectedTableRule);
                count++;
            }
        }
    }
}
