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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLAlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLBinlogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCheckTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCloneStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDelimiterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLInstallComponentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLResetPersistStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLRestartStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCollationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowEventsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOpenTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureCodeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowRelayLogEventsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowReplicaStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowReplicasStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowSlaveHostsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowSlaveStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTriggersStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowVariablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShutdownStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallComponentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.CacheIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.CheckTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.CloneStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.EmptyStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ExplainStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.FlushStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.InstallComponentStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.InstallPluginStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.KillStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.LoadIndexInfoStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLAlterResourceGroupStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLBinlogStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLChecksumTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLCreateResourceGroupStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLDelimiterStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLDropResourceGroupStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLOptimizeTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLRepairTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLResetPersistStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLResetStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLSetResourceGroupStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.mysql.MySQLUseStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ResetParameterStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.RestartStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.SetParameterStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowBinlogEventsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowCollationStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowColumnsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowCreateTableStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowCreateTriggerStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowCreateUserStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowDatabasesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowEventsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowFunctionStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowIndexStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowOpenTablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowProcedureCodeStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowProcedureStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowRelaylogEventsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowReplicaStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowReplicasStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowSlaveHostsStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowSlaveStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowTableStatusStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowTablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowTriggersStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShowVariablesStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.ShutdownStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.UninstallComponentStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dal.impl.UninstallPluginStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.AlterResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.BinlogStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.CacheIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.CheckTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ChecksumTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.CloneStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.CreateResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.DelimiterStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.DropResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.EmptyStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ExplainStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.FlushStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.InstallComponentStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.InstallPluginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.KillStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.LoadIndexInfoStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.OptimizeTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.RepairTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ResetParameterStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ResetPersistStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ResetStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.RestartStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.SetParameterStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.SetResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowBinlogEventsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowCollationStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowCreateTriggerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowCreateUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowEventsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowFunctionStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowOpenTablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowProcedureCodeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowProcedureStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowRelayLogEventsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowReplicaStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowReplicasStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowSlaveHostsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowSlaveStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowTriggersStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShowVariablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.ShutdownStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.UninstallComponentStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.UninstallPluginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dal.UseStatementTestCase;

/**
 * DAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DALStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof MySQLUseStatement) {
            MySQLUseStatementAssert.assertIs(assertContext, (MySQLUseStatement) actual, (UseStatementTestCase) expected);
        } else if (actual instanceof EmptyStatement) {
            EmptyStatementAssert.assertIs(assertContext, (EmptyStatement) actual, (EmptyStatementTestCase) expected);
        } else if (actual instanceof ExplainStatement) {
            ExplainStatementAssert.assertIs(assertContext, (ExplainStatement) actual, (ExplainStatementTestCase) expected);
        } else if (actual instanceof MySQLShowDatabasesStatement) {
            ShowDatabasesStatementAssert.assertIs(assertContext, (MySQLShowDatabasesStatement) actual, (ShowDatabasesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTablesStatement) {
            ShowTablesStatementAssert.assertIs(assertContext, (MySQLShowTablesStatement) actual, (ShowTablesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowColumnsStatement) {
            ShowColumnsStatementAssert.assertIs(assertContext, (MySQLShowColumnsStatement) actual, (ShowColumnsStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateTableStatement) {
            ShowCreateTableStatementAssert.assertIs(assertContext, (MySQLShowCreateTableStatement) actual, (ShowCreateTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateTriggerStatement) {
            ShowCreateTriggerStatementAssert.assertIs(assertContext, (MySQLShowCreateTriggerStatement) actual, (ShowCreateTriggerStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateUserStatement) {
            ShowCreateUserStatementAssert.assertIs(assertContext, (MySQLShowCreateUserStatement) actual, (ShowCreateUserStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTableStatusStatement) {
            ShowTableStatusStatementAssert.assertIs(assertContext, (MySQLShowTableStatusStatement) actual, (ShowTableStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowIndexStatement) {
            ShowIndexStatementAssert.assertIs(assertContext, (MySQLShowIndexStatement) actual, (ShowIndexStatementTestCase) expected);
        } else if (actual instanceof MySQLShowRelayLogEventsStatement) {
            ShowRelaylogEventsStatementAssert.assertIs(assertContext, (MySQLShowRelayLogEventsStatement) actual, (ShowRelayLogEventsStatementTestCase) expected);
        } else if (actual instanceof ShowStatement) {
            ShowStatementAssert.assertIs(assertContext, (ShowStatement) actual, (ShowStatementTestCase) expected);
        } else if (actual instanceof SetStatement) {
            SetParameterStatementAssert.assertIs(assertContext, (SetStatement) actual, (SetParameterStatementTestCase) expected);
        } else if (actual instanceof ResetParameterStatement) {
            ResetParameterStatementAssert.assertIs(assertContext, (ResetParameterStatement) actual, (ResetParameterStatementTestCase) expected);
        } else if (actual instanceof MySQLInstallComponentStatement) {
            InstallComponentStatementAssert.assertIs(assertContext, (MySQLInstallComponentStatement) actual, (InstallComponentStatementTestCase) expected);
        } else if (actual instanceof MySQLFlushStatement) {
            FlushStatementAssert.assertIs(assertContext, (MySQLFlushStatement) actual, (FlushStatementTestCase) expected);
        } else if (actual instanceof MySQLInstallPluginStatement) {
            InstallPluginStatementAssert.assertIs(assertContext, (MySQLInstallPluginStatement) actual, (InstallPluginStatementTestCase) expected);
        } else if (actual instanceof MySQLCloneStatement) {
            CloneStatementAssert.assertIs(assertContext, (MySQLCloneStatement) actual, (CloneStatementTestCase) expected);
        } else if (actual instanceof MySQLUninstallComponentStatement) {
            UninstallComponentStatementAssert.assertIs(assertContext, (MySQLUninstallComponentStatement) actual, (UninstallComponentStatementTestCase) expected);
        } else if (actual instanceof MySQLCreateResourceGroupStatement) {
            MySQLCreateResourceGroupStatementAssert.assertIs(assertContext, (MySQLCreateResourceGroupStatement) actual, (CreateResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLUninstallPluginStatement) {
            UninstallPluginStatementAssert.assertIs(assertContext, (MySQLUninstallPluginStatement) actual, (UninstallPluginStatementTestCase) expected);
        } else if (actual instanceof MySQLRestartStatement) {
            RestartStatementAssert.assertIs(assertContext, (MySQLRestartStatement) actual, (RestartStatementTestCase) expected);
        } else if (actual instanceof MySQLSetResourceGroupStatement) {
            MySQLSetResourceGroupStatementAssert.assertIs(assertContext, (MySQLSetResourceGroupStatement) actual, (SetResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLOptimizeTableStatement) {
            MySQLOptimizeTableStatementAssert.assertIs(assertContext, (MySQLOptimizeTableStatement) actual, (OptimizeTableStatementTestCase) expected);
        } else if (actual instanceof MySQLRepairTableStatement) {
            MySQLRepairTableStatementAssert.assertIs(assertContext, (MySQLRepairTableStatement) actual, (RepairTableStatementTestCase) expected);
        } else if (actual instanceof MySQLBinlogStatement) {
            MySQLBinlogStatementAssert.assertIs(assertContext, (MySQLBinlogStatement) actual, (BinlogStatementTestCase) expected);
        } else if (actual instanceof MySQLShowFunctionStatusStatement) {
            ShowFunctionStatusStatementAssert.assertIs(assertContext, (MySQLShowFunctionStatusStatement) actual, (ShowFunctionStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowProcedureStatusStatement) {
            ShowProcedureStatusStatementAssert.assertIs(assertContext, (MySQLShowProcedureStatusStatement) actual, (ShowProcedureStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowReplicasStatement) {
            ShowReplicasStatementAssert.assertIs(assertContext, (MySQLShowReplicasStatement) actual, (ShowReplicasStatementTestCase) expected);
        } else if (actual instanceof MySQLShowReplicaStatusStatement) {
            ShowReplicaStatusStatementAssert.assertIs(assertContext, (MySQLShowReplicaStatusStatement) actual, (ShowReplicaStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowSlaveStatusStatement) {
            ShowSlaveStatusStatementAssert.assertIs(assertContext, (MySQLShowSlaveStatusStatement) actual, (ShowSlaveStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowSlaveHostsStatement) {
            ShowSlaveHostsStatementAssert.assertIs(assertContext, (MySQLShowSlaveHostsStatement) actual, (ShowSlaveHostsStatementTestCase) expected);
        } else if (actual instanceof MySQLResetStatement) {
            MySQLResetStatementAssert.assertIs(assertContext, (MySQLResetStatement) actual, (ResetStatementTestCase) expected);
        } else if (actual instanceof MySQLResetPersistStatement) {
            MySQLResetPersistStatementAssert.assertIs(assertContext, (MySQLResetPersistStatement) actual, (ResetPersistStatementTestCase) expected);
        } else if (actual instanceof MySQLShowProcedureCodeStatement) {
            ShowProcedureCodeStatementAssert.assertIs(assertContext, (MySQLShowProcedureCodeStatement) actual, (ShowProcedureCodeStatementTestCase) expected);
        } else if (actual instanceof MySQLKillStatement) {
            KillStatementAssert.assertIs(assertContext, (MySQLKillStatement) actual, (KillStatementTestCase) expected);
        } else if (actual instanceof MySQLCacheIndexStatement) {
            CacheIndexStatementAssert.assertIs(assertContext, (MySQLCacheIndexStatement) actual, (CacheIndexStatementTestCase) expected);
        } else if (actual instanceof MySQLLoadIndexInfoStatement) {
            LoadIndexInfoStatementAssert.assertIs(assertContext, (MySQLLoadIndexInfoStatement) actual, (LoadIndexInfoStatementTestCase) expected);
        } else if (actual instanceof MySQLShutdownStatement) {
            ShutdownStatementAssert.assertIs(assertContext, (MySQLShutdownStatement) actual, (ShutdownStatementTestCase) expected);
        } else if (actual instanceof MySQLShowOpenTablesStatement) {
            ShowOpenTablesStatementAssert.assertIs(assertContext, (MySQLShowOpenTablesStatement) actual, (ShowOpenTablesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTriggersStatement) {
            ShowTriggersStatementAssert.assertIs(assertContext, (MySQLShowTriggersStatement) actual, (ShowTriggersStatementTestCase) expected);
        } else if (actual instanceof MySQLShowStatusStatement) {
            ShowStatusStatementAssert.assertIs(assertContext, (MySQLShowStatusStatement) actual, (ShowStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLCheckTableStatement) {
            CheckTableStatementAssert.assertIs(assertContext, (MySQLCheckTableStatement) actual, (CheckTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowEventsStatement) {
            ShowEventsStatementAssert.assertIs(assertContext, (MySQLShowEventsStatement) actual, (ShowEventsStatementTestCase) expected);
        } else if (actual instanceof MySQLDropResourceGroupStatement) {
            MySQLDropResourceGroupStatementAssert.assertIs(assertContext, (MySQLDropResourceGroupStatement) actual, (DropResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLAlterResourceGroupStatement) {
            MySQLAlterResourceGroupStatementAssert.assertIs(assertContext, (MySQLAlterResourceGroupStatement) actual, (AlterResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLChecksumTableStatement) {
            MySQLChecksumTableStatementAssert.assertIs(assertContext, (MySQLChecksumTableStatement) actual, (ChecksumTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCollationStatement) {
            ShowCollationStatementAssert.assertIs(assertContext, (MySQLShowCollationStatement) actual, (ShowCollationStatementTestCase) expected);
        } else if (actual instanceof MySQLShowVariablesStatement) {
            ShowVariablesStatementAssert.assertIs(assertContext, (MySQLShowVariablesStatement) actual, (ShowVariablesStatementTestCase) expected);
        } else if (actual instanceof MySQLDelimiterStatement) {
            MySQLDelimiterStatementAssert.assertIs(assertContext, (MySQLDelimiterStatement) actual, (DelimiterStatementTestCase) expected);
        } else if (actual instanceof MySQLShowBinlogEventsStatement) {
            ShowBinlogEventsStatementAssert.assertIs(assertContext, (MySQLShowBinlogEventsStatement) actual, (ShowBinlogEventsStatementTestCase) expected);
        }
    }
}
