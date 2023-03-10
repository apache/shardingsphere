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
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableRuleSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.AuditStrategyAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedTableRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

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
            assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getTableStrategySegment().getShardingColumn(), is(expected.getTableStrategy().getShardingColumn()));
            assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getTableStrategySegment().getType(), is(expected.getTableStrategy().getType()));
            if (!"none".equalsIgnoreCase(actual.getTableStrategySegment().getType())) {
                assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                        actual.getTableStrategySegment().getShardingAlgorithm().getName(), is(expected.getTableStrategy().getAlgorithmSegment().getName()));
            }
            assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getDatabaseStrategySegment().getShardingColumn(), is(expected.getDataStrategy().getShardingColumn()));
            assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getDatabaseStrategySegment().getType(), is(expected.getDataStrategy().getType()));
            if (!"none".equalsIgnoreCase(actual.getDatabaseStrategySegment().getType())) {
                assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                        actual.getDatabaseStrategySegment().getShardingAlgorithm().getName(), is(expected.getDataStrategy().getAlgorithmSegment().getName()));
            }
            if (null != actual.getKeyGenerateStrategySegment()) {
                assertThat(assertContext.getText(String.format("`%s`'s table rule segment assertion error: ", actual.getClass().getSimpleName())),
                        actual.getKeyGenerateStrategySegment().getKeyGenerateColumn(), is(expected.getKeyGenerateStrategyColumn()));
            }
            AuditStrategyAssert.assertIs(assertContext, actual.getAuditStrategySegment(), expected.getAuditStrategy());
        }
    }
}
