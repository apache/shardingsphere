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
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.ShadowRuleAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedShadowRule;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.AlterShadowRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertShadowRule(assertContext, actual.getRules(), expected.getRules());
        }
    }
    
    private static void assertShadowRule(final SQLCaseAssertContext assertContext, final Collection<ShadowRuleSegment> actual, final List<ExpectedShadowRule> expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual shadow rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual shadow rule should exist."));
            int count = 0;
            for (ShadowRuleSegment each : actual) {
                ShadowRuleAssert.assertIs(assertContext, each, expected.get(count));
                count++;
            }
        }
    }
}
