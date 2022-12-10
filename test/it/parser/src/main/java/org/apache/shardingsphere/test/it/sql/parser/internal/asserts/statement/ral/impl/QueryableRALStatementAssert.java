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
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ConvertYamlConfigurationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ExportDatabaseConfigurationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowDistVariablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowAuthorityRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowComputeNodeInfoStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowComputeNodesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowComputeNodeModeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowStatusFromReadwriteSplittingRulesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowSQLParserRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowSQLTranslatorRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowTableMetaDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowTrafficRulesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowTransactionRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.queryable.ShowDistVariableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ConvertYamlConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ExportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowDistVariablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowAuthorityRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowComputeNodeInfoStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowComputeNodesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowComputeNodeModeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowStatusFromReadwriteSplittingRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowSQLTranslatorRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowTableMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowTrafficRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowTransactionRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowDistVariableStatementTestCase;
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
            ShowComputeNodesStatementAssert.assertIs(assertContext, (ShowComputeNodesStatement) actual, (ShowComputeNodesStatementTestCase) expected);
        } else if (actual instanceof ShowStatusFromReadwriteSplittingRulesStatement) {
            ShowStatusFromReadwriteSplittingRulesStatementAssert.assertIs(assertContext, (ShowStatusFromReadwriteSplittingRulesStatement) actual,
                    (ShowStatusFromReadwriteSplittingRulesStatementTestCase) expected);
        } else if (actual instanceof ShowTableMetaDataStatement) {
            ShowTableMetaDataStatementAssert.assertIs(assertContext, (ShowTableMetaDataStatement) actual, (ShowTableMetaDataStatementTestCase) expected);
        } else if (actual instanceof ShowAuthorityRuleStatement) {
            ShowAuthorityRuleStatementAssert.assertIs(assertContext, (ShowAuthorityRuleStatement) actual, (ShowAuthorityRuleStatementTestCase) expected);
        } else if (actual instanceof ShowTransactionRuleStatement) {
            ShowTransactionRuleStatementAssert.assertIs(assertContext, (ShowTransactionRuleStatement) actual, (ShowTransactionRuleStatementTestCase) expected);
        } else if (actual instanceof ShowTrafficRulesStatement) {
            ShowTrafficRulesStatementAssert.assertIs(assertContext, (ShowTrafficRulesStatement) actual, (ShowTrafficRulesStatementTestCase) expected);
        } else if (actual instanceof ShowSQLParserRuleStatement) {
            ShowSQLParserRuleStatementAssert.assertIs(assertContext, (ShowSQLParserRuleStatement) actual, (ShowSQLParserRuleStatementTestCase) expected);
        } else if (actual instanceof ExportDatabaseConfigurationStatement) {
            ExportDatabaseConfigurationStatementAssert.assertIs(assertContext, (ExportDatabaseConfigurationStatement) actual, (ExportDatabaseConfigurationStatementTestCase) expected);
        } else if (actual instanceof ShowSQLTranslatorRuleStatement) {
            ShowSQLTranslatorRuleStatementAssert.assertIs(assertContext, (ShowSQLTranslatorRuleStatement) actual, (ShowSQLTranslatorRuleStatementTestCase) expected);
        } else if (actual instanceof ShowComputeNodeInfoStatement) {
            ShowComputeNodeInfoStatementAssert.assertIs(assertContext, (ShowComputeNodeInfoStatement) actual, (ShowComputeNodeInfoStatementTestCase) expected);
        } else if (actual instanceof ShowComputeNodeModeStatement) {
            ShowComputeNodeModeStatementAssert.assertIs(assertContext, (ShowComputeNodeModeStatement) actual, (ShowComputeNodeModeStatementTestCase) expected);
        } else if (actual instanceof ConvertYamlConfigurationStatement) {
            ConvertYamlConfigurationStatementAssert.assertIs(assertContext, (ConvertYamlConfigurationStatement) actual, (ConvertYamlConfigurationStatementTestCase) expected);
        }
    }
}
