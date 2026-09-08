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
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingKeyGenerateStrategyStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drop sharding key generate strategy statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropShardingKeyGenerateStrategyStatementAssert {
    
    /**
     * Assert drop sharding key generate strategy statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop sharding key generate strategy statement
     * @param expected expected drop sharding key generate strategy statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropShardingKeyGenerateStrategyStatement actual,
                                final DropShardingKeyGenerateStrategyStatementTestCase expected) {
        if (ExistingAssert.assertIs(assertContext, actual, expected)) {
            assertThat(assertContext.getText("Key generate strategy names assertion error: "), actual.getNames(), is(expected.getNames()));
            if (expected.isIfExists()) {
                assertTrue(actual.isIfExists(), assertContext.getText("Actual statement should contain IF EXISTS clause."));
            } else {
                assertFalse(actual.isIfExists(), assertContext.getText("Actual statement should not contain IF EXISTS clause."));
            }
        }
    }
}
