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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.distsql.parser.statement.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportMetaDataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportStorageNodesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ConvertYamlConfigurationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowDistVariableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowDistVariablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowTableMetaDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowTrafficRulesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ConvertYamlConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowDistVariableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowDistVariablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowTableMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowTrafficRulesStatementTestCase;
import org.apache.shardingsphere.traffic.distsql.parser.statement.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.transaction.distsql.parser.statement.queryable.ShowTransactionRuleStatement;

/**
 * Queryable RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryableRALStatementAssert {
    
    /**
     * Assert queryable RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual queryable RAL statement
     * @param expected expected queryable RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final QueryableRALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof ShowDistVariableStatement) {
            ShowDistVariableStatementAssert.assertIs(assertContext, (ShowDistVariableStatement) actual, (ShowDistVariableStatementTestCase) expected);
        } else if (actual instanceof ShowDistVariablesStatement) {
            ShowDistVariablesStatementAssert.assertIs(assertContext, (ShowDistVariablesStatement) actual, (ShowDistVariablesStatementTestCase) expected);
        } else if (actual instanceof ShowComputeNodesStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowStatusFromReadwriteSplittingRulesStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowTableMetaDataStatement) {
            ShowTableMetaDataStatementAssert.assertIs(assertContext, (ShowTableMetaDataStatement) actual, (ShowTableMetaDataStatementTestCase) expected);
        } else if (actual instanceof ShowAuthorityRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowTransactionRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowTrafficRulesStatement) {
            ShowTrafficRulesStatementAssert.assertIs(assertContext, (ShowTrafficRulesStatement) actual, (ShowTrafficRulesStatementTestCase) expected);
        } else if (actual instanceof ShowSQLParserRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ExportDatabaseConfigurationStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ExportMetaDataStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ExportStorageNodesStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowSQLTranslatorRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowComputeNodeInfoStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ShowComputeNodeModeStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof ConvertYamlConfigurationStatement) {
            ConvertYamlConfigurationStatementAssert.assertIs(assertContext, (ConvertYamlConfigurationStatement) actual, (ConvertYamlConfigurationStatementTestCase) expected);
        }
    }
}
