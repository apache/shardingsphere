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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableRulesStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowDatabaseDiscoveryRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowEncryptRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowReadwriteSplittingRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowShadowAlgorithmsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowShadowRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowShardingAlgorithmsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowShardingBindingTableRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowShardingBroadcastTableRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowShardingTableRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule.ShowSingleTableRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowDataBaseDiscoveryRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowEncryptRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowReadwriteSplittingRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShadowAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShadowRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowSingleTableRulesStatementTestCase;

/**
 * Show rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowRulesStatementAssert {
    
    /**
     * Assert show rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show rule statement
     * @param expected expected show rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowRulesStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof ShowDatabaseDiscoveryRulesStatement) {
            ShowDatabaseDiscoveryRulesStatementAssert.assertIs(assertContext, (ShowDatabaseDiscoveryRulesStatement) actual, (ShowDataBaseDiscoveryRulesStatementTestCase) expected);
        } else if (actual instanceof ShowEncryptRulesStatement) {
            ShowEncryptRulesStatementAssert.assertIs(assertContext, (ShowEncryptRulesStatement) actual, (ShowEncryptRulesStatementTestCase) expected);
        } else if (actual instanceof ShowReadwriteSplittingRulesStatement) {
            ShowReadwriteSplittingRulesStatementAssert.assertIs(assertContext, (ShowReadwriteSplittingRulesStatement) actual, (ShowReadwriteSplittingRulesStatementTestCase) expected);
        } else if (actual instanceof ShowShardingBindingTableRulesStatement) {
            ShowShardingBindingTableRulesStatementAssert.assertIs(assertContext, (ShowShardingBindingTableRulesStatement) actual, (ShowShardingBindingTableRulesStatementTestCase) expected);
        } else if (actual instanceof ShowShardingBroadcastTableRulesStatement) {
            ShowShardingBroadcastTableRulesStatementAssert.assertIs(assertContext, (ShowShardingBroadcastTableRulesStatement) actual, (ShowShardingBroadcastTableRulesStatementTestCase) expected);
        } else if (actual instanceof ShowShardingAlgorithmsStatement) {
            ShowShardingAlgorithmsStatementAssert.assertIs(assertContext, (ShowShardingAlgorithmsStatement) actual, (ShowShardingAlgorithmsStatementTestCase) expected);
        } else if (actual instanceof ShowShardingTableRulesStatement) {
            ShowShardingTableRulesStatementAssert.assertIs(assertContext, (ShowShardingTableRulesStatement) actual, (ShowShardingTableRulesStatementTestCase) expected);
        } else if (actual instanceof ShowShadowRulesStatement) {
            ShowShadowRulesStatementAssert.assertIs(assertContext, (ShowShadowRulesStatement) actual, (ShowShadowRulesStatementTestCase) expected);
        } else if (actual instanceof ShowShadowAlgorithmsStatement) {
            ShowShadowAlgorithmsStatementAssert.assertIs(assertContext, (ShowShadowAlgorithmsStatement) actual, (ShowShadowAlgorithmsStatementTestCase) expected);
        } else if (actual instanceof ShowSingleTableRulesStatement) {
            ShowSingleTableRulesStatementAssert.assertIs(assertContext, (ShowSingleTableRulesStatement) actual, (ShowSingleTableRulesStatementTestCase) expected);
        }
    }
}
