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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingTableRuleStatementTestCase;

import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Drop sharding table rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropShardingTableRuleStatementAssert {
    
    /**
     * Assert drop sharding table rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop sharding table rule statement
     * @param expected expected drop sharding table rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropShardingTableRuleStatement actual, final DropShardingTableRuleStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertThat(assertContext.getText("Sharding table rule assertion error: "), actual.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()),
                    is(expected.getRuleNames()));
            assertThat(assertContext.getText("Sharding table rule assertion error: "), actual.isIfExists(), is(expected.isIfExists()));
        }
    }
}
