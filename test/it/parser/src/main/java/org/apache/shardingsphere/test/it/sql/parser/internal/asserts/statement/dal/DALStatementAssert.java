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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLCloneStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLDelimiterStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLResetPersistStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLRestartStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShutdownStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.component.MySQLInstallComponentStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.component.MySQLUninstallComponentStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.index.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.index.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.plugin.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.plugin.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.binlog.MySQLBinlogStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.binlog.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowRelayLogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowReplicaStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowReplicasStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowSlaveHostsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowSlaveStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.replication.show.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLAlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLDropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.resource.MySQLSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.character.MySQLShowCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.event.MySQLShowEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.privilege.MySQLShowCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureCodeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowOpenTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.trigger.MySQLShowCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.trigger.MySQLShowTriggersStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.variable.MySQLShowVariablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLCheckTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.dal.OracleSpoolStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.AlterResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.BinlogStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.CacheIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.CheckTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ChecksumTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.CloneStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.CreateResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.DelimiterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.DescribeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.DropResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.EmptyStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ExplainStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.FlushStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.InstallComponentStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.InstallPluginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.KillStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.LoadIndexInfoStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.OptimizeTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ResetParameterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ResetPersistStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ResetStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.RestartStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.SetParameterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowBinlogEventsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowCollationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowColumnsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowCreateTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowCreateTriggerStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowCreateUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowDatabasesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowEventsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowFunctionStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowOpenTablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowProcedureCodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowProcedureStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowRelayLogEventsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowReplicaStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowReplicasStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowSlaveHostsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowSlaveStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowTableStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowTablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowTriggersStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShowVariablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.ShutdownStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.SpoolStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.UninstallComponentStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.UninstallPluginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.mysql.RepairTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.mysql.SetResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl.mysql.UseStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.AlterResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.BinlogStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.CacheIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.CheckTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ChecksumTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.CloneStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.CreateResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.DelimiterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.DescribeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.DropResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.EmptyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ExplainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.FlushStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.InstallComponentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.InstallPluginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.KillStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.LoadIndexInfoStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.OptimizeTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.RepairTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ResetParameterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ResetPersistStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ResetStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.RestartStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.SetParameterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.SetResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowBinlogEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowCreateTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowCreateUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowFunctionStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowOpenTablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowProcedureCodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowProcedureStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowRelayLogEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowReplicaStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowReplicasStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowSlaveHostsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowSlaveStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowTriggersStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowVariablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShutdownStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.SpoolStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.UninstallComponentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.UninstallPluginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.UseStatementTestCase;

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
            UseStatementAssert.assertIs(assertContext, (MySQLUseStatement) actual, (UseStatementTestCase) expected);
        } else if (actual instanceof EmptyStatement) {
            EmptyStatementAssert.assertIs(assertContext, (EmptyStatement) actual, (EmptyStatementTestCase) expected);
        } else if (actual instanceof ExplainStatement) {
            ExplainStatementAssert.assertIs(assertContext, (ExplainStatement) actual, (ExplainStatementTestCase) expected);
        } else if (actual instanceof MySQLDescribeStatement) {
            DescribeStatementAssert.assertIs(assertContext, (MySQLDescribeStatement) actual, (DescribeStatementTestCase) expected);
        } else if (actual instanceof ShowDatabasesStatement) {
            ShowDatabasesStatementAssert.assertIs(assertContext, (ShowDatabasesStatement) actual, (ShowDatabasesStatementTestCase) expected);
        } else if (actual instanceof ShowTablesStatement) {
            ShowTablesStatementAssert.assertIs(assertContext, (ShowTablesStatement) actual, (ShowTablesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowColumnsStatement) {
            ShowColumnsStatementAssert.assertIs(assertContext, (MySQLShowColumnsStatement) actual, (ShowColumnsStatementTestCase) expected);
        } else if (actual instanceof ShowCreateTableStatement) {
            ShowCreateTableStatementAssert.assertIs(assertContext, (ShowCreateTableStatement) actual, (ShowCreateTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateTriggerStatement) {
            ShowCreateTriggerStatementAssert.assertIs(assertContext, (MySQLShowCreateTriggerStatement) actual, (ShowCreateTriggerStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateUserStatement) {
            ShowCreateUserStatementAssert.assertIs(assertContext, (MySQLShowCreateUserStatement) actual, (ShowCreateUserStatementTestCase) expected);
        } else if (actual instanceof ShowTableStatusStatement) {
            ShowTableStatusStatementAssert.assertIs(assertContext, (ShowTableStatusStatement) actual, (ShowTableStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowIndexStatement) {
            ShowIndexStatementAssert.assertIs(assertContext, (MySQLShowIndexStatement) actual, (ShowIndexStatementTestCase) expected);
        } else if (actual instanceof MySQLShowRelayLogEventsStatement) {
            ShowRelayLogEventsStatementAssert.assertIs(assertContext, (MySQLShowRelayLogEventsStatement) actual, (ShowRelayLogEventsStatementTestCase) expected);
        } else if (actual instanceof ShowStatement) {
            ShowStatementAssert.assertIs(assertContext, (ShowStatement) actual, (ShowStatementTestCase) expected);
        } else if (actual instanceof SetStatement) {
            SetParameterStatementAssert.assertIs(assertContext, (SetStatement) actual, (SetParameterStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLResetParameterStatement) {
            ResetParameterStatementAssert.assertIs(assertContext, (PostgreSQLResetParameterStatement) actual, (ResetParameterStatementTestCase) expected);
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
            CreateResourceGroupStatementAssert.assertIs(assertContext, (MySQLCreateResourceGroupStatement) actual, (CreateResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLUninstallPluginStatement) {
            UninstallPluginStatementAssert.assertIs(assertContext, (MySQLUninstallPluginStatement) actual, (UninstallPluginStatementTestCase) expected);
        } else if (actual instanceof MySQLRestartStatement) {
            RestartStatementAssert.assertIs(assertContext, (MySQLRestartStatement) actual, (RestartStatementTestCase) expected);
        } else if (actual instanceof MySQLSetResourceGroupStatement) {
            SetResourceGroupStatementAssert.assertIs(assertContext, (MySQLSetResourceGroupStatement) actual, (SetResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLOptimizeTableStatement) {
            OptimizeTableStatementAssert.assertIs(assertContext, (MySQLOptimizeTableStatement) actual, (OptimizeTableStatementTestCase) expected);
        } else if (actual instanceof MySQLRepairTableStatement) {
            RepairTableStatementAssert.assertIs(assertContext, (MySQLRepairTableStatement) actual, (RepairTableStatementTestCase) expected);
        } else if (actual instanceof MySQLBinlogStatement) {
            BinlogStatementAssert.assertIs(assertContext, (MySQLBinlogStatement) actual, (BinlogStatementTestCase) expected);
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
            ResetStatementAssert.assertIs(assertContext, (MySQLResetStatement) actual, (ResetStatementTestCase) expected);
        } else if (actual instanceof MySQLResetPersistStatement) {
            ResetPersistStatementAssert.assertIs(assertContext, (MySQLResetPersistStatement) actual, (ResetPersistStatementTestCase) expected);
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
            DropResourceGroupStatementAssert.assertIs(assertContext, (MySQLDropResourceGroupStatement) actual, (DropResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLAlterResourceGroupStatement) {
            AlterResourceGroupStatementAssert.assertIs(assertContext, (MySQLAlterResourceGroupStatement) actual, (AlterResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLChecksumTableStatement) {
            ChecksumTableStatementAssert.assertIs(assertContext, (MySQLChecksumTableStatement) actual, (ChecksumTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCollationStatement) {
            ShowCollationStatementAssert.assertIs(assertContext, (MySQLShowCollationStatement) actual, (ShowCollationStatementTestCase) expected);
        } else if (actual instanceof MySQLShowVariablesStatement) {
            ShowVariablesStatementAssert.assertIs(assertContext, (MySQLShowVariablesStatement) actual, (ShowVariablesStatementTestCase) expected);
        } else if (actual instanceof MySQLDelimiterStatement) {
            DelimiterStatementAssert.assertIs(assertContext, (MySQLDelimiterStatement) actual, (DelimiterStatementTestCase) expected);
        } else if (actual instanceof MySQLShowBinlogEventsStatement) {
            ShowBinlogEventsStatementAssert.assertIs(assertContext, (MySQLShowBinlogEventsStatement) actual, (ShowBinlogEventsStatementTestCase) expected);
        } else if (actual instanceof OracleSpoolStatement) {
            SpoolStatementAssert.assertIs(assertContext, (OracleSpoolStatement) actual, (SpoolStatementTestCase) expected);
        }
    }
}
