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
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl.AuditStrategyAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedAutoTableRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Auto table rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AutoTableRuleAssert {
    
    /**
     * Assert auto table rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual auto table rule
     * @param expected expected auto table rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AutoTableRuleSegment actual, final ExpectedAutoTableRule expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual auto table rule should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual auto table rule should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s auto table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getLogicTable(), is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s auto table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getDataSourceNodes(), is(expected.getDataSources()));
            assertThat(assertContext.getText(String.format("`%s`'s auto table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getShardingColumn(), is(expected.getTableStrategyColumn()));
            assertNotNull(actual.getKeyGenerateStrategySegment(), assertContext.getText("key generate should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s auto table rule segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getKeyGenerateStrategySegment().getKeyGenerateColumn(), is(expected.getKeyGenerateStrategyColumn()));
            AlgorithmAssert.assertIs(assertContext, actual.getShardingAlgorithmSegment(), expected.getTableStrategy());
            AlgorithmAssert.assertIs(assertContext, actual.getKeyGenerateStrategySegment().getKeyGenerateAlgorithmSegment(), expected.getKeyGenerateStrategy());
            AuditStrategyAssert.assertIs(assertContext, actual.getAuditStrategySegment(), expected.getAuditStrategy());
        }
    }
}
