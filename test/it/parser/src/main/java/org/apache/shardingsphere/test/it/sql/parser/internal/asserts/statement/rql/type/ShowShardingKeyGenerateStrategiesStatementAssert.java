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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingKeyGenerateStrategiesStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingKeyGenerateStrategiesStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Show sharding key generate strategies statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowShardingKeyGenerateStrategiesStatementAssert {
    
    /**
     * Assert show sharding key generate strategies statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show sharding key generate strategies statement
     * @param expected expected show sharding key generate strategies statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowShardingKeyGenerateStrategiesStatement actual,
                                final ShowShardingKeyGenerateStrategiesStatementTestCase expected) {
        ShowRulesStatementAssert.assertIs(assertContext, actual, expected);
        if (null == expected.getName()) {
            assertFalse(actual.getName().isPresent(), assertContext.getText("Actual key generate strategy name should not exist."));
        } else {
            assertThat(assertContext.getText("Key generate strategy name assertion error: "), actual.getName().orElse(null), is(expected.getName()));
        }
    }
}
