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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.UnsetVariableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAdminCleanTrashStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCleanAllProfileStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCleanProfileStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateExternalResourceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisPlanReplayerPlayStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAdminCopyTabletStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAdminSetReplicaStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAdminSetReplicaVersionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterResourceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterSystemStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisBackupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCancelBackupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCancelLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateSqlBlockRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateRepositoryStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisDescFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisDropRepositoryStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowFunctionsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowDataStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowDataTypesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowLoadWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowStreamLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowProcStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowTrashStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowEncryptKeysStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowFileStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowSyncJobStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisSwitchStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.show.DorisShowQueryStatsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAdminCleanTrashStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCleanAllProfileStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCleanProfileStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCreateExternalResourceStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisPlanReplayerPlayStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAdminCopyTabletStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAdminSetReplicaStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAdminSetReplicaVersionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAlterResourceStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisAlterSystemStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisBackupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCancelBackupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCancelLoadStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCreateSqlBlockRuleStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisCreateRepositoryStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisDescFunctionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisDropRepositoryStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowFunctionsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowDatabaseStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowDataStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowDataTypesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowTrashStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowProcStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowCreateLoadStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowLoadStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowLoadWarningsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowStreamLoadStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowCreateRoutineLoadStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowQueryStatsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowEncryptKeysStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowFileStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisShowSyncJobStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisSwitchStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type.DorisUnsetVariableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAdminCleanTrashStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCleanAllProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCleanProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateExternalResourceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisPlanReplayerPlayStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAdminCopyTabletStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAdminSetReplicaStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAdminSetReplicaVersionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterResourceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisBackupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCancelBackupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCancelLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateRepositoryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisDescFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisDropRepositoryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowFunctionsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowDataTypesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowTrashStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowProcStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowCreateLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowLoadWarningsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowStreamLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowCreateRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowEncryptKeysStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowFileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowSyncJobStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisSwitchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisUnsetVariableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.show.DorisShowQueryStatsStatementTestCase;

/**
 * DAL statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisDALStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof DorisAlterResourceStatement) {
            DorisAlterResourceStatementAssert.assertIs(assertContext, (DorisAlterResourceStatement) actual, (DorisAlterResourceStatementTestCase) expected);
        } else if (actual instanceof DorisAlterSystemStatement) {
            DorisAlterSystemStatementAssert.assertIs(assertContext, (DorisAlterSystemStatement) actual, (DorisAlterSystemStatementTestCase) expected);
        } else if (actual instanceof DorisCreateSqlBlockRuleStatement) {
            DorisCreateSqlBlockRuleStatementAssert.assertIs(assertContext, (DorisCreateSqlBlockRuleStatement) actual, (DorisCreateSqlBlockRuleStatementTestCase) expected);
        } else if (actual instanceof DorisShowQueryStatsStatement) {
            DorisShowQueryStatsStatementAssert.assertIs(assertContext, (DorisShowQueryStatsStatement) actual, (DorisShowQueryStatsStatementTestCase) expected);
        } else if (actual instanceof DorisSwitchStatement) {
            DorisSwitchStatementAssert.assertIs(assertContext, (DorisSwitchStatement) actual, (DorisSwitchStatementTestCase) expected);
        } else if (actual instanceof UnsetVariableStatement) {
            DorisUnsetVariableStatementAssert.assertIs(assertContext, (UnsetVariableStatement) actual, (DorisUnsetVariableStatementTestCase) expected);
        } else if (actual instanceof DorisCreateRepositoryStatement) {
            DorisCreateRepositoryStatementAssert.assertIs(assertContext, (DorisCreateRepositoryStatement) actual, (DorisCreateRepositoryStatementTestCase) expected);
        } else if (actual instanceof DorisDropRepositoryStatement) {
            DorisDropRepositoryStatementAssert.assertIs(assertContext, (DorisDropRepositoryStatement) actual, (DorisDropRepositoryStatementTestCase) expected);
        } else if (actual instanceof DorisShowFunctionsStatement) {
            DorisShowFunctionsStatementAssert.assertIs(assertContext, (DorisShowFunctionsStatement) actual, (DorisShowFunctionsStatementTestCase) expected);
        } else if (actual instanceof DorisDescFunctionStatement) {
            DorisDescFunctionStatementAssert.assertIs(assertContext, (DorisDescFunctionStatement) actual, (DorisDescFunctionStatementTestCase) expected);
        } else if (actual instanceof DorisShowProcStatement) {
            DorisShowProcStatementAssert.assertIs(assertContext, (DorisShowProcStatement) actual, (DorisShowProcStatementTestCase) expected);
        } else if (actual instanceof DorisShowCreateRoutineLoadStatement) {
            DorisShowCreateRoutineLoadStatementAssert.assertIs(assertContext, (DorisShowCreateRoutineLoadStatement) actual, (DorisShowCreateRoutineLoadStatementTestCase) expected);
        } else if (actual instanceof DorisShowSyncJobStatement) {
            DorisShowSyncJobStatementAssert.assertIs(assertContext, (DorisShowSyncJobStatement) actual, (DorisShowSyncJobStatementTestCase) expected);
        } else if (actual instanceof DorisShowDataTypesStatement) {
            DorisShowDataTypesStatementAssert.assertIs(assertContext, (DorisShowDataTypesStatement) actual, (DorisShowDataTypesStatementTestCase) expected);
        } else if (actual instanceof DorisShowDataStatement) {
            DorisShowDataStatementAssert.assertIs(assertContext, (DorisShowDataStatement) actual, (DorisShowDataStatementTestCase) expected);
        } else if (actual instanceof DorisBackupStatement) {
            DorisBackupStatementAssert.assertIs(assertContext, (DorisBackupStatement) actual, (DorisBackupStatementTestCase) expected);
        } else if (actual instanceof DorisCancelBackupStatement) {
            DorisCancelBackupStatementAssert.assertIs(assertContext, (DorisCancelBackupStatement) actual, (DorisCancelBackupStatementTestCase) expected);
        } else if (actual instanceof DorisAdminSetReplicaStatusStatement) {
            DorisAdminSetReplicaStatusStatementAssert.assertIs(assertContext, (DorisAdminSetReplicaStatusStatement) actual, (DorisAdminSetReplicaStatusStatementTestCase) expected);
        } else if (actual instanceof DorisAdminSetReplicaVersionStatement) {
            DorisAdminSetReplicaVersionStatementAssert.assertIs(assertContext, (DorisAdminSetReplicaVersionStatement) actual, (DorisAdminSetReplicaVersionStatementTestCase) expected);
        } else if (actual instanceof DorisAdminCopyTabletStatement) {
            DorisAdminCopyTabletStatementAssert.assertIs(assertContext, (DorisAdminCopyTabletStatement) actual, (DorisAdminCopyTabletStatementTestCase) expected);
        } else if (actual instanceof DorisShowFileStatement) {
            DorisShowFileStatementAssert.assertIs(assertContext, (DorisShowFileStatement) actual, (DorisShowFileStatementTestCase) expected);
        } else if (actual instanceof DorisShowEncryptKeysStatement) {
            DorisShowEncryptKeysStatementAssert.assertIs(assertContext, (DorisShowEncryptKeysStatement) actual, (DorisShowEncryptKeysStatementTestCase) expected);
        } else if (actual instanceof DorisAdminCleanTrashStatement) {
            DorisAdminCleanTrashStatementAssert.assertIs(assertContext, (DorisAdminCleanTrashStatement) actual, (DorisAdminCleanTrashStatementTestCase) expected);
        } else if (actual instanceof DorisShowTrashStatement) {
            DorisShowTrashStatementAssert.assertIs(assertContext, (DorisShowTrashStatement) actual, (DorisShowTrashStatementTestCase) expected);
        } else if (actual instanceof DorisShowLoadStatement) {
            DorisShowLoadStatementAssert.assertIs(assertContext, (DorisShowLoadStatement) actual, (DorisShowLoadStatementTestCase) expected);
        } else if (actual instanceof DorisShowCreateLoadStatement) {
            DorisShowCreateLoadStatementAssert.assertIs(assertContext, (DorisShowCreateLoadStatement) actual, (DorisShowCreateLoadStatementTestCase) expected);
        } else if (actual instanceof DorisShowStreamLoadStatement) {
            DorisShowStreamLoadStatementAssert.assertIs(assertContext, (DorisShowStreamLoadStatement) actual, (DorisShowStreamLoadStatementTestCase) expected);
        } else if (actual instanceof DorisShowDatabaseStatement) {
            DorisShowDatabaseStatementAssert.assertIs(assertContext, (DorisShowDatabaseStatement) actual, (DorisShowDatabaseStatementTestCase) expected);
        } else if (actual instanceof DorisShowTableStatement) {
            DorisShowTableStatementAssert.assertIs(assertContext, (DorisShowTableStatement) actual, (DorisShowTableStatementTestCase) expected);
        } else if (actual instanceof DorisCancelLoadStatement) {
            DorisCancelLoadStatementAssert.assertIs(assertContext, (DorisCancelLoadStatement) actual, (DorisCancelLoadStatementTestCase) expected);
        } else if (actual instanceof DorisShowLoadWarningsStatement) {
            DorisShowLoadWarningsStatementAssert.assertIs(assertContext, (DorisShowLoadWarningsStatement) actual, (DorisShowLoadWarningsStatementTestCase) expected);
        } else if (actual instanceof DorisCleanAllProfileStatement) {
            DorisCleanAllProfileStatementAssert.assertIs(assertContext, (DorisCleanAllProfileStatement) actual, (DorisCleanAllProfileStatementTestCase) expected);
        } else if (actual instanceof DorisCleanProfileStatement) {
            DorisCleanProfileStatementAssert.assertIs(assertContext, (DorisCleanProfileStatement) actual, (DorisCleanProfileStatementTestCase) expected);
        } else if (actual instanceof DorisCreateExternalResourceStatement) {
            DorisCreateExternalResourceStatementAssert.assertIs(assertContext, (DorisCreateExternalResourceStatement) actual, (DorisCreateExternalResourceStatementTestCase) expected);
        } else if (actual instanceof DorisPlanReplayerPlayStatement) {
            DorisPlanReplayerPlayStatementAssert.assertIs(assertContext, (DorisPlanReplayerPlayStatement) actual, (DorisPlanReplayerPlayStatementTestCase) expected);
        }
    }
}
