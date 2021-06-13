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

package org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.asserts.segment.rdl.EncryptRuleAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.segment.impl.rdl.ExpectedEncryptRule;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterEncryptRuleStatementTestCase;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterEncryptRuleStatement;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Create encrypt rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterEncryptRuleStatementAssert {

    /**
     * Assert create encrypt rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual create encrypt rule statement
     * @param expected      expected create encrypt rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterEncryptRuleStatement actual, final AlterEncryptRuleStatementTestCase expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            assertEncryptRules(assertContext, actual.getEncryptRules(), expected.getEncryptRules());
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }

    private static void assertEncryptRules(final SQLCaseAssertContext assertContext, final Collection<EncryptRuleSegment> actual, final List<ExpectedEncryptRule> expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            assertThat(assertContext.getText(String.format("Actual encrypt rule size should be %s , but it was %s", expected.size(), actual.size())), actual.size(), is(expected.size()));
            int count = 0;
            for (EncryptRuleSegment encryptRuleSegment : actual) {
                ExpectedEncryptRule expectedEncryptRule = expected.get(count);
                assertThat(assertContext.getText("encrypt rule table name assertion error: "), encryptRuleSegment.getTableName(), is(expectedEncryptRule.getTableName()));
                EncryptRuleAssert.assertIs(assertContext, encryptRuleSegment, expectedEncryptRule);
            }
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }
}
