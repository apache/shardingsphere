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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.AlterResourceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.AlterRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl.AlterDatabaseDiscoveryRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl.AlterEncryptRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl.AlterReadwriteSplittingRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl.AlterShardingBindingTableRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl.AlterShardingBroadcastTableRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl.AlterShardingTableRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.AddResourceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.CreateRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.drop.DropResourceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.drop.DropRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterResourceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.AddResourceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropResourceStatementTestCase;

/**
 * RDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLStatementAssert {
    
    /**
     * Assert SQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual RDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof AlterDatabaseDiscoveryRuleStatement) {
            AlterDatabaseDiscoveryRuleStatementAssert.assertIs(assertContext, (AlterDatabaseDiscoveryRuleStatement) actual, expected);
        } else if (actual instanceof AlterReadwriteSplittingRuleStatement) {
            AlterReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (AlterReadwriteSplittingRuleStatement) actual, (AlterReadwriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof AlterShardingBindingTableRulesStatement) {
            AlterShardingBindingTableRulesStatementAssert.assertIs(assertContext, (AlterShardingBindingTableRulesStatement) actual, (AlterShardingBindingTableRulesStatementTestCase) expected);
        } else if (actual instanceof AlterEncryptRuleStatement) {
            AlterEncryptRuleStatementAssert.assertIs(assertContext, (AlterEncryptRuleStatement) actual, (AlterEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof AlterShardingBroadcastTableRulesStatement) {
            AlterShardingBroadcastTableRulesStatementAssert.assertIs(assertContext, (AlterShardingBroadcastTableRulesStatement) actual, (AlterShardingBroadcastTableRulesStatementTestCase) expected);
        } else if (actual instanceof AlterShardingTableRuleStatement) {
            AlterShardingTableRuleStatementAssert.assertIs(assertContext, (AlterShardingTableRuleStatement) actual, expected);
        } else if (actual instanceof CreateRuleStatement) {
            CreateRuleStatementAssert.assertIs(assertContext, (CreateRuleStatement) actual, expected);
        } else if (actual instanceof RegisterStorageUnitStatement) {
            AddResourceStatementAssert.assertIs(assertContext, (RegisterStorageUnitStatement) actual, (AddResourceStatementTestCase) expected);
        } else if (actual instanceof AlterStorageUnitStatement) {
            AlterResourceStatementAssert.assertIs(assertContext, (AlterStorageUnitStatement) actual, (AlterResourceStatementTestCase) expected);
        } else if (actual instanceof AlterRuleStatement) {
            AlterRuleStatementAssert.assertIs(assertContext, (AlterRuleStatement) actual, expected);
        } else if (actual instanceof UnregisterStorageUnitStatement) {
            DropResourceStatementAssert.assertIs(assertContext, (UnregisterStorageUnitStatement) actual, (DropResourceStatementTestCase) expected);
        } else if (actual instanceof DropRuleStatement) {
            DropRuleStatementAssert.assertIs(assertContext, (DropRuleStatement) actual, expected);
        }
    }
}
