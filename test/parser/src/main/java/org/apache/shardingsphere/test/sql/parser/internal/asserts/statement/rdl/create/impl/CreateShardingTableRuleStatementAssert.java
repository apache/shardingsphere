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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.AutoTableRuleAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.TableRuleAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.distsql.ExpectedAutoTableRule;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.distsql.ExpectedTableRule;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.rdl.create.CreateShardingAutoTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.rdl.create.CreateShardingTableRuleStatementTestCase;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Create sharding table rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateShardingTableRuleStatementAssert {
    
    /**
     * Assert create sharding binding table rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create sharding table rule statement
     * @param expected expected create sharding table rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateShardingTableRuleStatement actual, final SQLParserTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            if (expected instanceof CreateShardingAutoTableRuleStatementTestCase) {
                CreateShardingAutoTableRuleStatementTestCase autoTableRuleStatementTestCase = (CreateShardingAutoTableRuleStatementTestCase) expected;
                Collection<AutoTableRuleSegment> actualAutoTableRules = actual.getRules().stream().map(each -> (AutoTableRuleSegment) each).collect(Collectors.toList());
                assertShardingAutoTableRules(assertContext, actualAutoTableRules, autoTableRuleStatementTestCase.getRules());
            } else {
                CreateShardingTableRuleStatementTestCase tableRuleStatementTestCase = (CreateShardingTableRuleStatementTestCase) expected;
                Collection<TableRuleSegment> actualTableRules = actual.getRules().stream().map(each -> (TableRuleSegment) each).collect(Collectors.toList());
                assertShardingTableRules(assertContext, actualTableRules, tableRuleStatementTestCase.getRules());
            }
        }
    }
    
    private static void assertShardingAutoTableRules(final SQLCaseAssertContext assertContext, final Collection<AutoTableRuleSegment> actual, final List<ExpectedAutoTableRule> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding auto table rule should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding auto table rule should exist."), actual);
            int count = 0;
            for (AutoTableRuleSegment tableRuleSegment : actual) {
                ExpectedAutoTableRule expectedTableRule = expected.get(count);
                AutoTableRuleAssert.assertIs(assertContext, tableRuleSegment, expectedTableRule);
                count++;
            }
        }
    }
    
    private static void assertShardingTableRules(final SQLCaseAssertContext assertContext, final Collection<TableRuleSegment> actual, final List<ExpectedTableRule> expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual sharding table rule should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual sharding table rule should exist."), actual);
            int count = 0;
            for (TableRuleSegment tableRuleSegment : actual) {
                ExpectedTableRule expectedTableRule = expected.get(count);
                TableRuleAssert.assertIs(assertContext, tableRuleSegment, expectedTableRule);
                count++;
            }
        }
    }
}
