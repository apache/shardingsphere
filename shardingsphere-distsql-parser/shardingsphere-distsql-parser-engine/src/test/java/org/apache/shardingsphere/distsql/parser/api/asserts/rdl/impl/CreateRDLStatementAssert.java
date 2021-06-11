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

package org.apache.shardingsphere.distsql.parser.api.asserts.rdl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.asserts.rdl.impl.create.*;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.create.*;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateRDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.*;

/**
 * Create RDL Statement
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateRDLStatementAssert {

    /**
     * Assert create RDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual create RDL statement
     * @param expected      expected create RDL statement test case
     */
    public static void assertIs(SQLCaseAssertContext assertContext, CreateRDLStatement actual, SQLParserTestCase expected) {
        if (actual instanceof AddResourceStatement) {
            AddResourceStatementAssert.assertIs(assertContext, (AddResourceStatement) actual, (AddResourceStatementTestCase) expected);
        } else if (actual instanceof CreateDatabaseDiscoveryRuleStatement) {
            CreateDatabaseDiscoveryRuleStatementAssert.assertIs(assertContext, (CreateDatabaseDiscoveryRuleStatement) actual, (CreateDataBaseDiscoveryRuleStatementTestCase) expected);
        } else if (actual instanceof CreateEncryptRuleStatement) {
            CreateEncryptRuleStatementAssert.assertIs(assertContext, (CreateEncryptRuleStatement) actual, (CreateEncryptRuleStatementTestCase) expected);
        } else if (actual instanceof CreateReadwriteSplittingRuleStatement) {
            CreateReadwriteSplittingRuleStatementAssert.assertIs(assertContext, (CreateReadwriteSplittingRuleStatement) actual, (CreateReadWriteSplittingRuleStatementTestCase) expected);
        } else if (actual instanceof CreateShardingBindingTableRulesStatement) {
            CreateShardingBindingTableRulesStatementAssert.assertIs(assertContext, (CreateShardingBindingTableRulesStatement) actual, (CreateShardingBindingTableRulesStatementTestCase) expected);
        } else if (actual instanceof CreateShardingBroadcastTableRulesStatement) {
            CreateShardingBroadcastTableRulesStatementAssert.assertIs(assertContext, (CreateShardingBroadcastTableRulesStatement) actual, (CreateShardingBroadcastTableRulesStatementTestCase) expected);
        } else if (actual instanceof CreateShardingTableRuleStatement) {
            CreateShardingTableRuleStatementAssert.assertIs(assertContext, (CreateShardingTableRuleStatement) actual, (CreateShardingTableRuleStatementTestCase) expected);
        }
    }
}
