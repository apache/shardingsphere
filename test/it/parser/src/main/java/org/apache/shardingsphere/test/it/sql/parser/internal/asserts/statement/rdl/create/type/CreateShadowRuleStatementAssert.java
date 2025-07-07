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
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.ShadowRuleAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedShadowRule;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.CreateShadowRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Create shadow rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateShadowRuleStatementAssert {
    
    /**
     * Assert create shadow rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create shadow rule statement
     * @param expected expected create shadow rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateShadowRuleStatement actual, final CreateShadowRuleStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertThat(assertContext.getText("if not exists segment assertion error: "), actual.isIfNotExists(), is(expected.isIfNotExists()));
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
