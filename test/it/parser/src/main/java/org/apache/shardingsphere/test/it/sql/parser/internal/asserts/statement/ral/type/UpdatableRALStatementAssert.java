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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.AlterComputeNodeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.LockClusterStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UpdatableRALStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.AlterComputeNodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.AlterReadwriteSplittingStorageUnitStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.ImportDatabaseConfigurationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.ImportMetaDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.LabelComputeNodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.LockClusterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.RefreshTableMetaDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.SetDistVariableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.type.updatable.UnlabelComputeNodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterReadwriteSplittingStorageUnitStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ImportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ImportMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.LabelComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.LockClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.RefreshTableMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.SetDistVariableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.UnlabelComputeNodeStatementTestCase;

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
        } else if (actual instanceof ImportDatabaseConfigurationStatement) {
            ImportDatabaseConfigurationStatementAssert.assertIs(assertContext, (ImportDatabaseConfigurationStatement) actual, (ImportDatabaseConfigurationStatementTestCase) expected);
        } else if (actual instanceof ImportMetaDataStatement) {
            ImportMetaDataStatementAssert.assertIs(assertContext, (ImportMetaDataStatement) actual, (ImportMetaDataStatementTestCase) expected);
        } else if (actual instanceof AlterReadwriteSplittingStorageUnitStatusStatement) {
            AlterReadwriteSplittingStorageUnitStatusStatementAssert.assertIs(assertContext,
                    (AlterReadwriteSplittingStorageUnitStatusStatement) actual, (AlterReadwriteSplittingStorageUnitStatusStatementTestCase) expected);
        } else if (actual instanceof LockClusterStatement) {
            LockClusterStatementAssert.assertIs(assertContext, (LockClusterStatement) actual, (LockClusterStatementTestCase) expected);
        }
    }
}
