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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.segment.TableRuleSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.TableRuleAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedTableRule;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingTableRuleStatementTestCase;

import java.util.Collection;
import java.util.List;

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
     * @param actual        actual create sharding table rule statement
     * @param expected      expected create sharding table rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateShardingTableRuleStatement actual, final CreateShardingTableRuleStatementTestCase expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            assertShardingTableRules(assertContext, actual.getRules(), expected.getTableRules());
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }

    private static void assertShardingTableRules(final SQLCaseAssertContext assertContext, final Collection<TableRuleSegment> actual, final List<ExpectedTableRule> expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            int count = 0;
            for (TableRuleSegment tableRuleSegment : actual) {
                ExpectedTableRule expectedTableRule = expected.get(count);
                TableRuleAssert.assertIs(assertContext, tableRuleSegment, expectedTableRule);
            }
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }
}
