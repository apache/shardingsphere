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
import org.apache.shardingsphere.distsql.parser.statement.ral.CommonDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.SetDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.RefreshTableMetadataStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.SetVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.ShowAllVariablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.ShowInstanceStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.ShowReadwriteSplittingReadResourcesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.common.ShowVariableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.RefreshTableMetadataStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.SetVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowAllVariablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowInstanceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowReadwriteSplittingReadResourcesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowVariableStatementTestCase;

/**
 * Common dist sql statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonDistSQLStatementAssert {
    
    /**
     * Assert common dist sql statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual common dist sql statement
     * @param expected expected common dist sql statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CommonDistSQLStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof SetDistSQLStatement) {
            SetVariableStatementAssert.assertIs(assertContext, (SetVariableStatement) actual, (SetVariableStatementTestCase) expected);
        } else if (actual instanceof ShowVariableStatement) {
            ShowVariableStatementAssert.assertIs(assertContext, (ShowVariableStatement) actual, (ShowVariableStatementTestCase) expected);
        } else if (actual instanceof ShowAllVariablesStatement) {
            ShowAllVariablesStatementAssert.assertIs(assertContext, (ShowAllVariablesStatement) actual, (ShowAllVariablesStatementTestCase) expected);
        } else if (actual instanceof ShowInstanceStatement) {
            ShowInstanceStatementAssert.assertIs(assertContext, (ShowInstanceStatement) actual, (ShowInstanceStatementTestCase) expected);
        } else if (actual instanceof ShowReadwriteSplittingReadResourcesStatement) {
            ShowReadwriteSplittingReadResourcesStatementAssert.assertIs(assertContext, (ShowReadwriteSplittingReadResourcesStatement) actual,
                    (ShowReadwriteSplittingReadResourcesStatementTestCase) expected);
        } else if (actual instanceof RefreshTableMetadataStatement) {
            RefreshTableMetadataStatementAssert.assertIs(assertContext, (RefreshTableMetadataStatement) actual, (RefreshTableMetadataStatementTestCase) expected);
        }
    }
}
