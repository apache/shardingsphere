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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAuditorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateBroadcastTableRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateDatabaseDiscoveryHeartbeatStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateDatabaseDiscoveryRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateDatabaseDiscoveryTypeStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateDefaultShadowAlgorithmStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateDefaultShardingStrategyStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateEncryptRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateReadwriteSplittingRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateShadowRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateShardingAuditorStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateShardingTableReferenceRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.CreateShardingTableRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl.SetDefaultSingleTableStorageUnitStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateBroadcastTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDatabaseDiscoveryHeartbeatStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDatabaseDiscoveryTypeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDefaultShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingAuditorStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingTableReferenceRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.SetDefaultSingleTableStorageUnitStatementTestCase;

/**
 * Create RDL Statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateRuleStatementAssert {
    
    /**
     * Assert create RDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create RDL statement
     * @param expected expected create RDL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateRuleStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof CreateDatabaseDiscoveryRuleStatement) {
            CreateDatabaseDiscoveryRuleStatementAssert.assertIs(assertContext, (CreateDatabaseDiscoveryRuleStatement) actual, expected);
        } else if (actual instanceof CreateDatabaseDiscoveryTypeStatement) {
            CreateDatabaseDiscoveryTypeStatementAssert.assertIs(assertContext, (CreateDatabaseDiscoveryTypeStatement) actual, (CreateDatabaseDiscoveryTypeStatementTestCase) expected);
        } else if (actual instanceof CreateDatabaseDiscoveryHeartbeatStatement) {
            CreateDatabaseDiscoveryHeartbeatStatementAssert.assertIs(assertContext, (CreateDatabaseDiscoveryHeartbeatStatement) actual, (CreateDatabaseDiscoveryHeartbeatStatementTestCase) expected);
        } else if (actual instanceof CreateEncryptRuleStatement) {
            CreateEncryptRuleStatementAssert.assertIs(assertContext, (CreateEncryptRuleStatement) actual, (CreateEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof CreateReadwriteSplittingRuleStatement) {
            CreateReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (CreateReadwriteSplittingRuleStatement) actual, (CreateReadwriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof CreateShardingTableReferenceRuleStatement) {
            CreateShardingTableReferenceRuleStatementAssert.assertIs(assertContext, (CreateShardingTableReferenceRuleStatement) actual, (CreateShardingTableReferenceRuleStatementTestCase) expected);
        } else if (actual instanceof CreateBroadcastTableRuleStatement) {
            CreateBroadcastTableRuleStatementAssert.assertIs(assertContext, (CreateBroadcastTableRuleStatement) actual,
                    (CreateBroadcastTableRuleStatementTestCase) expected);
        } else if (actual instanceof CreateShardingTableRuleStatement) {
            CreateShardingTableRuleStatementAssert.assertIs(assertContext, (CreateShardingTableRuleStatement) actual, expected);
        } else if (actual instanceof CreateShadowRuleStatement) {
            CreateShadowRuleStatementAssert.assertIs(assertContext, (CreateShadowRuleStatement) actual, (CreateShadowRuleStatementTestCase) expected);
        } else if (actual instanceof CreateDefaultShardingStrategyStatement) {
            CreateDefaultShardingStrategyStatementAssert.assertIs(assertContext, (CreateDefaultShardingStrategyStatement) actual, (CreateDefaultShardingStrategyStatementTestCase) expected);
        } else if (actual instanceof CreateDefaultShadowAlgorithmStatement) {
            CreateDefaultShadowAlgorithmStatementAssert.assertIs(assertContext, (CreateDefaultShadowAlgorithmStatement) actual, (CreateDefaultShadowAlgorithmStatementTestCase) expected);
        } else if (actual instanceof SetDefaultSingleTableStorageUnitStatement) {
            SetDefaultSingleTableStorageUnitStatementAssert.assertIs(assertContext, (SetDefaultSingleTableStorageUnitStatement) actual, (SetDefaultSingleTableStorageUnitStatementTestCase) expected);
        } else if (actual instanceof CreateShardingAuditorStatement) {
            CreateShardingAuditorStatementAssert.assertIs(assertContext, (CreateShardingAuditorStatement) actual, (CreateShardingAuditorStatementTestCase) expected);
        }
    }
}
