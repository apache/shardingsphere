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
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.ParseStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.PreviewStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAllVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowVariableStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.advanced.ParseStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.advanced.PreviewStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ExportDatabaseConfigurationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowAllVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowAuthorityRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowInstanceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowReadwriteSplittingReadResourcesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowSQLParserRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowTableMetadataStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowTrafficRulesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowTransactionRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.queryable.ShowVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ExportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ParseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.PreviewStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowAllVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowAuthorityRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowInstanceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowReadwriteSplittingReadResourcesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTableMetadataStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTrafficRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowTransactionRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowVariableStatementTestCase;

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
        } else if (actual instanceof ShowAllVariableStatement) {
            ShowAllVariableStatementAssert.assertIs(assertContext, (ShowAllVariableStatement) actual, (ShowAllVariableStatementTestCase) expected);
        } else if (actual instanceof ShowInstanceStatement) {
            ShowInstanceStatementAssert.assertIs(assertContext, (ShowInstanceStatement) actual, (ShowInstanceStatementTestCase) expected);
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
        } else if (actual instanceof PreviewStatement) {
            PreviewStatementAssert.assertIs(assertContext, (PreviewStatement) actual, (PreviewStatementTestCase) expected);
        } else if (actual instanceof ParseStatement) {
            ParseStatementAssert.assertIs(assertContext, (ParseStatement) actual, (ParseStatementTestCase) expected);
        }
    }
}
