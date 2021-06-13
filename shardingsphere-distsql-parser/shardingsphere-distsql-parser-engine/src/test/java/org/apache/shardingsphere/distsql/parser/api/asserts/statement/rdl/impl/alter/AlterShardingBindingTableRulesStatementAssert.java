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
import org.apache.shardingsphere.distsql.parser.api.asserts.segment.rdl.ShardingBindingTableRuleAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.segment.impl.rdl.ExpectedShardingBindingTableRule;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ShardingBindingTableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBindingTableRulesStatement;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Alter sharding binding table rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterShardingBindingTableRulesStatementAssert {

    /**
     * Assert alter sharding binding table rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual alter sharding binding table rule statement
     * @param expected      expected alter sharding binding table rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterShardingBindingTableRulesStatement actual, final AlterShardingBindingTableRulesStatementTestCase expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            assertShardingBindingTableRules(assertContext, actual.getRules(), expected.getRules());
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }

    private static void assertShardingBindingTableRules(final SQLCaseAssertContext assertContext, final Collection<ShardingBindingTableRuleSegment> actual,
                                                        final List<ExpectedShardingBindingTableRule> expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            assertThat(assertContext.getText(String.format("Actual sharding binding table rule size should be %s , but it was %s", expected.size(), actual.size())),
                    actual.size(), is(expected.size()));
            int count = 0;
            for (ShardingBindingTableRuleSegment shardingBindingTableRuleSegment : actual) {
                ExpectedShardingBindingTableRule expectedShardingBindingTableRule = expected.get(count);
                ShardingBindingTableRuleAssert.assertIs(assertContext, shardingBindingTableRuleSegment, expectedShardingBindingTableRule);
                count++;
            }
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }
}
