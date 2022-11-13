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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.rdl.alter.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.rdl.EncryptRuleAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedEncryptRule;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.distsql.rdl.alter.AlterEncryptRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Create encrypt rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterEncryptRuleStatementAssert {
    
    /**
     * Assert create encrypt rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create encrypt rule statement
     * @param expected expected create encrypt rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterEncryptRuleStatement actual, final AlterEncryptRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertEncryptRules(assertContext, actual.getRules(), expected.getRules());
        }
    }
    
    private static void assertEncryptRules(final SQLCaseAssertContext assertContext, final Collection<EncryptRuleSegment> actual, final List<ExpectedEncryptRule> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual encrypt rule should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual encrypt rule should exist."), actual);
            assertThat(assertContext.getText(String.format("Actual encrypt rule size should be %s , but it was %s", expected.size(), actual.size())), actual.size(), is(expected.size()));
            int count = 0;
            for (EncryptRuleSegment encryptRuleSegment : actual) {
                ExpectedEncryptRule expectedEncryptRule = expected.get(count);
                assertThat(assertContext.getText("encrypt rule table name assertion error: "), encryptRuleSegment.getTableName(), is(expectedEncryptRule.getName()));
                EncryptRuleAssert.assertIs(assertContext, encryptRuleSegment, expectedEncryptRule);
            }
        }
    }
}
