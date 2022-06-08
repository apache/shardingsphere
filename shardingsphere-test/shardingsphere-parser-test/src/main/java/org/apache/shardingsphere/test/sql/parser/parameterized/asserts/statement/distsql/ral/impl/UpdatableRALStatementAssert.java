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
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.DropTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.LabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.UnlabelInstanceStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.AlterInstanceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.AlterSQLParserRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.AlterTrafficRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.CreateTrafficRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.DropTrafficRuleStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.ImportDatabaseConfigurationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.LabelInstanceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.RefreshTableMetadataStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.SetVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.updatable.UnlabelInstanceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AlterInstanceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AlterSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AlterTrafficRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.CreateTrafficRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.DropTrafficRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ImportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.LabelInstanceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.RefreshTableMetadataStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.SetVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.UnlabelInstanceStatementTestCase;

/**
 * Updatable RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdatableRALStatementAssert {
    
    /**
     * Assert updatable RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual updatable RAL statement
     * @param expected expected updatable RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdatableRALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof LabelInstanceStatement) {
            LabelInstanceStatementAssert.assertIs(assertContext, (LabelInstanceStatement) actual, (LabelInstanceStatementTestCase) expected);
        } else if (actual instanceof UnlabelInstanceStatement) {
            UnlabelInstanceStatementAssert.assertIs(assertContext, (UnlabelInstanceStatement) actual, (UnlabelInstanceStatementTestCase) expected);
        } else if (actual instanceof AlterInstanceStatement) {
            AlterInstanceStatementAssert.assertIs(assertContext, (AlterInstanceStatement) actual, (AlterInstanceStatementTestCase) expected);
        } else if (actual instanceof SetVariableStatement) {
            SetVariableStatementAssert.assertIs(assertContext, (SetVariableStatement) actual, (SetVariableStatementTestCase) expected);
        } else if (actual instanceof RefreshTableMetadataStatement) {
            RefreshTableMetadataStatementAssert.assertIs(assertContext, (RefreshTableMetadataStatement) actual, (RefreshTableMetadataStatementTestCase) expected);
        } else if (actual instanceof AlterSQLParserRuleStatement) {
            AlterSQLParserRuleStatementAssert.assertIs(assertContext, (AlterSQLParserRuleStatement) actual, (AlterSQLParserRuleStatementTestCase) expected);
        } else if (actual instanceof DropTrafficRuleStatement) {
            DropTrafficRuleStatementAssert.assertIs(assertContext, (DropTrafficRuleStatement) actual, (DropTrafficRuleStatementTestCase) expected);
        } else if (actual instanceof CreateTrafficRuleStatement) {
            CreateTrafficRuleStatementAssert.assertIs(assertContext, (CreateTrafficRuleStatement) actual, (CreateTrafficRuleStatementTestCase) expected);
        } else if (actual instanceof AlterTrafficRuleStatement) {
            AlterTrafficRuleStatementAssert.assertIs(assertContext, (AlterTrafficRuleStatement) actual, (AlterTrafficRuleStatementTestCase) expected);
        } else if (actual instanceof ImportDatabaseConfigurationStatement) {
            ImportDatabaseConfigurationStatementAssert.assertIs(assertContext, (ImportDatabaseConfigurationStatement) actual, (ImportDatabaseConfigurationStatementTestCase) expected);
        }
    }
}
