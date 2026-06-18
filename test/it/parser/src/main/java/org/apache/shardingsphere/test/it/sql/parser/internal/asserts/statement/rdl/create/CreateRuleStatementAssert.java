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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.broadcast.distsql.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.type.CreateRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.mask.distsql.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.sharding.distsql.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateBroadcastTableRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateDefaultShadowAlgorithmStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateDefaultShardingStrategyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateEncryptRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateMaskRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateReadwriteSplittingRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateShadowRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateShardingTableReferenceRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.CreateShardingTableRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.rdl.create.type.SetDefaultSingleTableStorageUnitStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.encrypt.CreateEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.CreateMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.readwritesplitting.CreateReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.CreateDefaultShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.CreateShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateBroadcastTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateShardingTableReferenceRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.single.SetDefaultSingleTableStorageUnitStatementTestCase;

/**
 * Create rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateRuleStatementAssert {
    
    /**
     * Assert create rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create rule statement
     * @param expected expected create rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateRuleStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof CreateEncryptRuleStatement) {
            CreateEncryptRuleStatementAssert.assertIs(assertContext, (CreateEncryptRuleStatement) actual, (CreateEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof CreateReadwriteSplittingRuleStatement) {
            CreateReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (CreateReadwriteSplittingRuleStatement) actual, (CreateReadwriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof CreateShardingTableReferenceRuleStatement) {
            CreateShardingTableReferenceRuleStatementAssert.assertIs(assertContext, (CreateShardingTableReferenceRuleStatement) actual, (CreateShardingTableReferenceRuleStatementTestCase) expected);
        } else if (actual instanceof CreateBroadcastTableRuleStatement) {
            CreateBroadcastTableRuleStatementAssert.assertIs(assertContext, (CreateBroadcastTableRuleStatement) actual, (CreateBroadcastTableRuleStatementTestCase) expected);
        } else if (actual instanceof CreateShardingTableRuleStatement) {
            CreateShardingTableRuleStatementAssert.assertIs(assertContext, (CreateShardingTableRuleStatement) actual, expected);
        } else if (actual instanceof CreateShadowRuleStatement) {
            CreateShadowRuleStatementAssert.assertIs(assertContext, (CreateShadowRuleStatement) actual, (CreateShadowRuleStatementTestCase) expected);
        } else if (actual instanceof CreateDefaultShardingStrategyStatement) {
            CreateDefaultShardingStrategyStatementAssert.assertIs(assertContext, (CreateDefaultShardingStrategyStatement) actual, (CreateDefaultShardingStrategyStatementTestCase) expected);
        } else if (actual instanceof CreateDefaultShadowAlgorithmStatement) {
            CreateDefaultShadowAlgorithmStatementAssert.assertIs(assertContext, (CreateDefaultShadowAlgorithmStatement) actual, (CreateDefaultShadowAlgorithmStatementTestCase) expected);
        } else if (actual instanceof CreateMaskRuleStatement) {
            CreateMaskRuleStatementAssert.assertIs(assertContext, (CreateMaskRuleStatement) actual, (CreateMaskRuleStatementTestCase) expected);
        } else if (actual instanceof SetDefaultSingleTableStorageUnitStatement) {
            SetDefaultSingleTableStorageUnitStatementAssert.assertIs(assertContext, (SetDefaultSingleTableStorageUnitStatement) actual, (SetDefaultSingleTableStorageUnitStatementTestCase) expected);
        }
    }
}
