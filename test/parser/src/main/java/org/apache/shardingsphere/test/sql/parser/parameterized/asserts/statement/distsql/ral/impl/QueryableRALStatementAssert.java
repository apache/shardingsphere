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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.distsql.parser.statement.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowModeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowVariableStatement;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ConvertYamlConfigurationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ExportDatabaseConfigurationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowAllVariablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowAuthorityRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowInstanceInfoStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowInstanceListStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowModeInfoStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowReadwriteSplittingReadResourcesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowSQLParserRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowSQLTranslatorRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowTableMetadataStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowTrafficRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowTransactionRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ConvertYamlConfigurationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ExportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowAllVariablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowAuthorityRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowInstanceInfoStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowInstanceListStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowModeInfoStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowReadwriteSplittingReadResourcesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowSQLTranslatorRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTableMetadataStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTrafficRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTransactionRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowVariableStatementTestCase;
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
        if (actual instanceof ShowVariableStatement) {
            ShowVariableStatementAssert.assertIs(assertContext, (ShowVariableStatement) actual, (ShowVariableStatementTestCase) expected);
        } else if (actual instanceof ShowDistVariablesStatement) {
            ShowAllVariablesStatementAssert.assertIs(assertContext, (ShowDistVariablesStatement) actual, (ShowAllVariablesStatementTestCase) expected);
        } else if (actual instanceof ShowInstanceListStatement) {
            ShowInstanceListStatementAssert.assertIs(assertContext, (ShowInstanceListStatement) actual, (ShowInstanceListStatementTestCase) expected);
        } else if (actual instanceof ShowReadwriteSplittingReadResourcesStatement) {
            ShowReadwriteSplittingReadResourcesStatementAssert.assertIs(assertContext, (ShowReadwriteSplittingReadResourcesStatement) actual,
                    (ShowReadwriteSplittingReadResourcesStatementTestCase) expected);
        } else if (actual instanceof ShowTableMetadataStatement) {
            ShowTableMetadataStatementAssert.assertIs(assertContext, (ShowTableMetadataStatement) actual, (ShowTableMetadataStatementTestCase) expected);
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
        } else if (actual instanceof ShowInstanceInfoStatement) {
            ShowInstanceInfoStatementAssert.assertIs(assertContext, (ShowInstanceInfoStatement) actual, (ShowInstanceInfoStatementTestCase) expected);
        } else if (actual instanceof ShowModeInfoStatement) {
            ShowModeInfoStatementAssert.assertIs(assertContext, (ShowModeInfoStatement) actual, (ShowModeInfoStatementTestCase) expected);
        } else if (actual instanceof ConvertYamlConfigurationStatement) {
            ConvertYamlConfigurationStatementAssert.assertIs(assertContext, (ConvertYamlConfigurationStatement) actual, (ConvertYamlConfigurationStatementTestCase) expected);
        }
    }
}
