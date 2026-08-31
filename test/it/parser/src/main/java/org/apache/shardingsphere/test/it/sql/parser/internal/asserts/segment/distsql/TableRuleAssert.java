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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.AuditStrategyAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedTableRule;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedShardingStrategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Table rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableRuleAssert {
    
    /**
     * Assert table rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual table rule
     * @param expected expected table rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableRuleSegment actual, final ExpectedTableRule expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual table rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual table rule should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getLogicTable(), is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getDataSourceNodes(), is(expected.getDataNodes()));
            assertShardingStrategy(assertContext, actual.getTableStrategySegment(), expected.getTableStrategy());
            assertShardingStrategy(assertContext, actual.getDatabaseStrategySegment(), expected.getDataStrategy());
            if (null != expected.getKeyGeneratorName()) {
                KeyGenerateStrategyAssert.assertIs(assertContext, actual.getKeyGenerateStrategySegment(), expected.getKeyGenerateStrategyColumn(), expected.getKeyGeneratorName(),
                        expected.getKeyGenerateStrategy());
            } else if (null != actual.getKeyGenerateStrategySegment()) {
                assertThat(assertContext.getText("Key generate column assertion error: "),
                        actual.getKeyGenerateStrategySegment().getKeyGenerateColumn(), is(expected.getKeyGenerateStrategyColumn()));
            }
            AuditStrategyAssert.assertIs(assertContext, actual.getAuditStrategySegment(), expected.getAuditStrategy());
        }
    }
    
    private static void assertShardingStrategy(final SQLCaseAssertContext assertContext, final ShardingStrategySegment actual, final ExpectedShardingStrategy expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual sharding strategy should not exist."));
            return;
        }
        assertNotNull(actual, assertContext.getText("Actual sharding strategy should exist."));
        assertThat(assertContext.getText("Sharding column assertion error: "), actual.getShardingColumn(), is(expected.getShardingColumn()));
        assertThat(assertContext.getText("Sharding strategy type assertion error: "), actual.getType(), is(expected.getType()));
        if (!"none".equalsIgnoreCase(actual.getType())) {
            assertThat(assertContext.getText("Sharding algorithm name assertion error: "), actual.getShardingAlgorithm().getName(), is(expected.getAlgorithmSegment().getName()));
        }
    }
}
