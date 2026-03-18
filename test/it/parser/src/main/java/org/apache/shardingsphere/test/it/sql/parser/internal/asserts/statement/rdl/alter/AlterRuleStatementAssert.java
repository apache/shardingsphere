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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.type.AlterRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.mask.distsql.statement.AlterMaskRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.sharding.distsql.statement.AlterDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterDefaultShadowAlgorithmStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterDefaultShardingStrategyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterEncryptRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterMaskRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterReadwriteSplittingRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterShadowRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterShardingTableReferenceRulesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.alter.type.AlterShardingTableRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.encrypt.AlterEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.AlterMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.readwritesplitting.AlterReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.AlterDefaultShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.AlterShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterShardingTableReferenceRulesStatementTestCase;

/**
 * Alter rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterRuleStatementAssert {
    
    /**
     * Assert alter sharding table rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter sharding table rule statement
     * @param expected expected alter sharding table rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterRuleStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof AlterDefaultShardingStrategyStatement) {
            AlterDefaultShardingStrategyStatementAssert.assertIs(assertContext, (AlterDefaultShardingStrategyStatement) actual, (AlterDefaultShardingStrategyStatementTestCase) expected);
        } else if (actual instanceof AlterEncryptRuleStatement) {
            AlterEncryptRuleStatementAssert.assertIs(assertContext, (AlterEncryptRuleStatement) actual, (AlterEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof AlterReadwriteSplittingRuleStatement) {
            AlterReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (AlterReadwriteSplittingRuleStatement) actual, (AlterReadwriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof AlterShardingTableReferenceRuleStatement) {
            AlterShardingTableReferenceRulesStatementAssert.assertIs(assertContext, (AlterShardingTableReferenceRuleStatement) actual, (AlterShardingTableReferenceRulesStatementTestCase) expected);
        } else if (actual instanceof AlterShardingTableRuleStatement) {
            AlterShardingTableRuleStatementAssert.assertIs(assertContext, (AlterShardingTableRuleStatement) actual, expected);
        } else if (actual instanceof AlterShadowRuleStatement) {
            AlterShadowRuleStatementAssert.assertIs(assertContext, (AlterShadowRuleStatement) actual, (AlterShadowRuleStatementTestCase) expected);
        } else if (actual instanceof AlterDefaultShadowAlgorithmStatement) {
            AlterDefaultShadowAlgorithmStatementAssert.assertIs(assertContext, (AlterDefaultShadowAlgorithmStatement) actual, (AlterDefaultShadowAlgorithmStatementTestCase) expected);
        } else if (actual instanceof AlterMaskRuleStatement) {
            AlterMaskRuleStatementAssert.assertIs(assertContext, (AlterMaskRuleStatement) actual, (AlterMaskRuleStatementTestCase) expected);
        }
    }
}
