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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.distsql.statement.DisableShardingScalingRuleStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.DisableShardingScalingRuleStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Disable sharding scaling rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisableShardingScalingRuleStatementAssert {
    
    /**
     * Assert disable sharding scaling rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual disable sharding scaling rule statement
     * @param expected expected disable sharding scaling rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DisableShardingScalingRuleStatement actual, final DisableShardingScalingRuleStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s scaling name assertion error: ", actual.getClass().getSimpleName())),
                    actual.getScalingName(), is(expected.getScalingName()));
        }
    }
}
