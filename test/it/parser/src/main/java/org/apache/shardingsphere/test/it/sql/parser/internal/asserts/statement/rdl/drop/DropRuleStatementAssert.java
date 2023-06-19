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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.broadcast.distsql.parser.statement.DropBroadcastTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.mask.distsql.parser.statement.DropMaskRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAuditorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropBroadcastTableRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropDefaultShardingStrategyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropEncryptRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropMaskRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropReadwriteSplittingRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShadowAlgorithmStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShadowRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShardingAlgorithmStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShardingAuditorStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShardingKeyGeneratorStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShardingTableReferenceRulesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.drop.impl.DropShardingTableRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.encrypt.DropEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.DropMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.readwritesplitting.DropReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.DropShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.DropShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropBroadcastTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingAuditorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingKeyGeneratorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingTableReferenceRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingTableRuleStatementTestCase;

/**
 * Drop RDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropRuleStatementAssert {
    
    /**
     * Assert drop RDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop RDL statement
     * @param expected expected drop RDL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropRuleStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof DropDefaultShardingStrategyStatement) {
            DropDefaultShardingStrategyStatementAssert.assertIs(assertContext, (DropDefaultShardingStrategyStatement) actual, (DropDefaultShardingStrategyStatementTestCase) expected);
        } else if (actual instanceof DropEncryptRuleStatement) {
            DropEncryptRuleStatementAssert.assertIs(assertContext, (DropEncryptRuleStatement) actual, (DropEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof DropReadwriteSplittingRuleStatement) {
            DropReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (DropReadwriteSplittingRuleStatement) actual, (DropReadwriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof DropShardingTableReferenceRuleStatement) {
            DropShardingTableReferenceRulesStatementAssert.assertIs(assertContext, (DropShardingTableReferenceRuleStatement) actual, (DropShardingTableReferenceRuleStatementTestCase) expected);
        } else if (actual instanceof DropBroadcastTableRuleStatement) {
            DropBroadcastTableRuleStatementAssert.assertIs(assertContext, (DropBroadcastTableRuleStatement) actual, (DropBroadcastTableRuleStatementTestCase) expected);
        } else if (actual instanceof DropShardingTableRuleStatement) {
            DropShardingTableRuleStatementAssert.assertIs(assertContext, (DropShardingTableRuleStatement) actual, (DropShardingTableRuleStatementTestCase) expected);
        } else if (actual instanceof DropShadowRuleStatement) {
            DropShadowRuleStatementAssert.assertIs(assertContext, (DropShadowRuleStatement) actual, (DropShadowRuleStatementTestCase) expected);
        } else if (actual instanceof DropShadowAlgorithmStatement) {
            DropShadowAlgorithmStatementAssert.assertIs(assertContext, (DropShadowAlgorithmStatement) actual, (DropShadowAlgorithmStatementTestCase) expected);
        } else if (actual instanceof DropShardingAlgorithmStatement) {
            DropShardingAlgorithmStatementAssert.assertIs(assertContext, (DropShardingAlgorithmStatement) actual, (DropShardingAlgorithmStatementTestCase) expected);
        } else if (actual instanceof DropShardingKeyGeneratorStatement) {
            DropShardingKeyGeneratorStatementAssert.assertIs(assertContext, (DropShardingKeyGeneratorStatement) actual, (DropShardingKeyGeneratorStatementTestCase) expected);
        } else if (actual instanceof DropShardingAuditorStatement) {
            DropShardingAuditorStatementAssert.assertIs(assertContext, (DropShardingAuditorStatement) actual, (DropShardingAuditorStatementTestCase) expected);
        } else if (actual instanceof DropMaskRuleStatement) {
            DropMaskRuleStatementAssert.assertIs(assertContext, (DropMaskRuleStatement) actual, (DropMaskRuleStatementTestCase) expected);
        }
    }
}
