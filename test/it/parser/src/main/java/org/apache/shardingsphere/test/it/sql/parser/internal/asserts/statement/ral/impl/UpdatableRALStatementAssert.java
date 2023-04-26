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
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterComputeNodeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LockClusterStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.globalclock.distsql.parser.statement.updatable.AlterGlobalClockRuleStatement;
import org.apache.shardingsphere.parser.distsql.parser.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ExistingAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.AlterComputeNodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.AlterGlobalClockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.AlterReadwriteSplittingStorageUnitStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.AlterSQLParserRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.AlterTrafficRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.ImportDatabaseConfigurationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.ImportMetaDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.LabelComputeNodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.LockClusterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.RefreshTableMetaDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.SetDistVariableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.updatable.UnlabelComputeNodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterGlobalClockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterReadwriteSplittingStorageUnitStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterTrafficRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ImportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ImportMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.LabelComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.LockClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.RefreshTableMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.SetDistVariableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.UnlabelComputeNodeStatementTestCase;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.transaction.distsql.parser.statement.updatable.AlterTransactionRuleStatement;

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
        if (actual instanceof LabelComputeNodeStatement) {
            LabelComputeNodeStatementAssert.assertIs(assertContext, (LabelComputeNodeStatement) actual, (LabelComputeNodeStatementTestCase) expected);
        } else if (actual instanceof UnlabelComputeNodeStatement) {
            UnlabelComputeNodeStatementAssert.assertIs(assertContext, (UnlabelComputeNodeStatement) actual, (UnlabelComputeNodeStatementTestCase) expected);
        } else if (actual instanceof AlterComputeNodeStatement) {
            AlterComputeNodeStatementAssert.assertIs(assertContext, (AlterComputeNodeStatement) actual, (AlterComputeNodeStatementTestCase) expected);
        } else if (actual instanceof SetDistVariableStatement) {
            SetDistVariableStatementAssert.assertIs(assertContext, (SetDistVariableStatement) actual, (SetDistVariableStatementTestCase) expected);
        } else if (actual instanceof RefreshTableMetaDataStatement) {
            RefreshTableMetaDataStatementAssert.assertIs(assertContext, (RefreshTableMetaDataStatement) actual, (RefreshTableMetaDataStatementTestCase) expected);
        } else if (actual instanceof AlterSQLParserRuleStatement) {
            AlterSQLParserRuleStatementAssert.assertIs(assertContext, (AlterSQLParserRuleStatement) actual, (AlterSQLParserRuleStatementTestCase) expected);
        } else if (actual instanceof AlterTrafficRuleStatement) {
            AlterTrafficRuleStatementAssert.assertIs(assertContext, (AlterTrafficRuleStatement) actual, (AlterTrafficRuleStatementTestCase) expected);
        } else if (actual instanceof ImportDatabaseConfigurationStatement) {
            ImportDatabaseConfigurationStatementAssert.assertIs(assertContext, (ImportDatabaseConfigurationStatement) actual, (ImportDatabaseConfigurationStatementTestCase) expected);
        } else if (actual instanceof ImportMetaDataStatement) {
            ImportMetaDataStatementAssert.assertIs(assertContext, (ImportMetaDataStatement) actual, (ImportMetaDataStatementTestCase) expected);
        } else if (actual instanceof AlterTransactionRuleStatement) {
            ExistingAssert.assertIs(assertContext, actual, expected);
        } else if (actual instanceof AlterReadwriteSplittingStorageUnitStatusStatement) {
            AlterReadwriteSplittingStorageUnitStatusStatementAssert.assertIs(assertContext,
                    (AlterReadwriteSplittingStorageUnitStatusStatement) actual, (AlterReadwriteSplittingStorageUnitStatusStatementTestCase) expected);
        } else if (actual instanceof LockClusterStatement) {
            LockClusterStatementAssert.assertIs(assertContext, (LockClusterStatement) actual, (LockClusterStatementTestCase) expected);
        } else if (actual instanceof AlterGlobalClockRuleStatement) {
            AlterGlobalClockRuleStatementAssert.assertIs(assertContext, (AlterGlobalClockRuleStatement) actual, (AlterGlobalClockRuleStatementTestCase) expected);
        }
    }
}
