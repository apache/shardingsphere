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

package org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.CreateRDLStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.DropRDLStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter.AlterDatabaseDiscoveryRuleStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter.AlterEncryptRuleStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter.AlterReadwriteSplittingRuleStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter.AlterShardingBindingTableRulesStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter.AlterShardingBroadcastTableRulesStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.statement.rdl.impl.alter.AlterShardingTableRuleStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterDataBaseDiscoveryRuleStatementTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterEncryptRuleStatementTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterReadWriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.alter.AlterShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateRDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropRDLStatement;

/**
 * RDL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLStatementAssert {

    /**
     * Assert SQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual RDL statement
     * @param expected      expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RDLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof AlterDatabaseDiscoveryRuleStatement) {
            AlterDatabaseDiscoveryRuleStatementAssert.assertIs(assertContext, (AlterDatabaseDiscoveryRuleStatement) actual, (AlterDataBaseDiscoveryRuleStatementTestCase) expected);
        } else if (actual instanceof AlterReadwriteSplittingRuleStatement) {
            AlterReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (AlterReadwriteSplittingRuleStatement) actual, (AlterReadWriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof AlterShardingBindingTableRulesStatement) {
            AlterShardingBindingTableRulesStatementAssert.assertIs(assertContext, (AlterShardingBindingTableRulesStatement) actual, (AlterShardingBindingTableRulesStatementTestCase) expected);
        } else if (actual instanceof AlterEncryptRuleStatement) {
            AlterEncryptRuleStatementAssert.assertIs(assertContext, (AlterEncryptRuleStatement) actual, (AlterEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof AlterShardingBroadcastTableRulesStatement) {
            AlterShardingBroadcastTableRulesStatementAssert.assertIs(assertContext, (AlterShardingBroadcastTableRulesStatement) actual, (AlterShardingBroadcastTableRulesStatementTestCase) expected);
        } else if (actual instanceof AlterShardingTableRuleStatement) {
            AlterShardingTableRuleStatementAssert.assertIs(assertContext, (AlterShardingTableRuleStatement) actual, (AlterShardingTableRuleStatementTestCase) expected);
        } else if (actual instanceof CreateRDLStatement) {
            CreateRDLStatementAssert.assertIs(assertContext, (CreateRDLStatement) actual, expected);
        } else if (actual instanceof DropRDLStatement) {
            DropRDLStatementAssert.assertIs(assertContext, (DropRDLStatement) actual, expected);
        }
    }
}
