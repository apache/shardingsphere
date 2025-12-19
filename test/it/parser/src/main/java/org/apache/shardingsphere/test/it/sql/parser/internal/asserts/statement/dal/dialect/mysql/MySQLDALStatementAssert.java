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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
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
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.MySQLShowTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.character.MySQLShowCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.event.MySQLShowEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.privilege.MySQLShowCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureCodeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowOpenTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.trigger.MySQLShowCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.trigger.MySQLShowTriggersStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.variable.MySQLShowVariablesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLCheckTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.table.MySQLRepairTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLAlterResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLBinlogStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLCacheIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLCheckTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLChecksumTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLCloneStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLCreateResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLDelimiterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLDescribeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLDropResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLFlushStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLInstallComponentStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLInstallPluginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLKillStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLLoadIndexInfoStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLOptimizeTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLRepairTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLResetPersistStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLResetStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLRestartStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLSetResourceGroupStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowBinlogEventsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowCollationStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowColumnsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowCreateTableStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowCreateTriggerStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowCreateUserStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowDatabasesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowEventsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowFunctionStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowIndexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowOpenTablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowProcedureCodeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowProcedureStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowRelayLogEventsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowReplicaStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowReplicasStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowSlaveHostsStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowSlaveStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowTableStatusStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowTablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowTransactionStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowTriggersStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShowVariablesStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLShutdownStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLUninstallComponentStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLUninstallPluginStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type.MySQLUseStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLCloneStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLDelimiterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLFlushStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLKillStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLResetPersistStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLResetStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLRestartStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLShutdownStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLUseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.component.MySQLInstallComponentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.component.MySQLUninstallComponentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.index.MySQLCacheIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.index.MySQLLoadIndexInfoStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.plugin.MySQLInstallPluginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.plugin.MySQLUninstallPluginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.binlog.MySQLBinlogStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.binlog.MySQLShowBinlogEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowRelayLogEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowReplicaStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowReplicasStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowSlaveHostsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowSlaveStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.replication.show.MySQLShowStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.resource.MySQLAlterResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.resource.MySQLCreateResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.resource.MySQLDropResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.resource.MySQLSetResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.character.MySQLShowCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.column.MySQLDescribeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.column.MySQLShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.database.MySQLShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.event.MySQLShowEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.function.MySQLShowFunctionStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.index.MySQLShowIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.MySQLShowTransactionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.privilege.MySQLShowCreateUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.procedure.MySQLShowProcedureCodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.procedure.MySQLShowProcedureStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.table.MySQLShowCreateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.table.MySQLShowOpenTablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.table.MySQLShowTableStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.table.MySQLShowTablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.trigger.MySQLShowCreateTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.trigger.MySQLShowTriggersStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.variable.MySQLShowVariablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.table.MySQLCheckTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.table.MySQLChecksumTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.table.MySQLOptimizeTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.table.MySQLRepairTableStatementTestCase;

/**
 * DAL statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLDALStatementAssert {
    
    /**
     * Assert DAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DAL statement
     * @param expected expected DAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof MySQLUseStatement) {
            MySQLUseStatementAssert.assertIs(assertContext, (MySQLUseStatement) actual, (MySQLUseStatementTestCase) expected);
        } else if (actual instanceof MySQLDescribeStatement) {
            MySQLDescribeStatementAssert.assertIs(assertContext, (MySQLDescribeStatement) actual, (MySQLDescribeStatementTestCase) expected);
        } else if (actual instanceof MySQLShowDatabasesStatement) {
            MySQLShowDatabasesStatementAssert.assertIs(assertContext, (MySQLShowDatabasesStatement) actual, (MySQLShowDatabasesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTablesStatement) {
            MySQLShowTablesStatementAssert.assertIs(assertContext, (MySQLShowTablesStatement) actual, (MySQLShowTablesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowColumnsStatement) {
            MySQLShowColumnsStatementAssert.assertIs(assertContext, (MySQLShowColumnsStatement) actual, (MySQLShowColumnsStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateTableStatement) {
            MySQLShowCreateTableStatementAssert.assertIs(assertContext, (MySQLShowCreateTableStatement) actual, (MySQLShowCreateTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateTriggerStatement) {
            MySQLShowCreateTriggerStatementAssert.assertIs(assertContext, (MySQLShowCreateTriggerStatement) actual, (MySQLShowCreateTriggerStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCreateUserStatement) {
            MySQLShowCreateUserStatementAssert.assertIs(assertContext, (MySQLShowCreateUserStatement) actual, (MySQLShowCreateUserStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTableStatusStatement) {
            MySQLShowTableStatusStatementAssert.assertIs(assertContext, (MySQLShowTableStatusStatement) actual, (MySQLShowTableStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowIndexStatement) {
            MySQLShowIndexStatementAssert.assertIs(assertContext, (MySQLShowIndexStatement) actual, (MySQLShowIndexStatementTestCase) expected);
        } else if (actual instanceof MySQLShowRelayLogEventsStatement) {
            MySQLShowRelayLogEventsStatementAssert.assertIs(assertContext, (MySQLShowRelayLogEventsStatement) actual, (MySQLShowRelayLogEventsStatementTestCase) expected);
        } else if (actual instanceof MySQLInstallComponentStatement) {
            MySQLInstallComponentStatementAssert.assertIs(assertContext, (MySQLInstallComponentStatement) actual, (MySQLInstallComponentStatementTestCase) expected);
        } else if (actual instanceof MySQLFlushStatement) {
            MySQLFlushStatementAssert.assertIs(assertContext, (MySQLFlushStatement) actual, (MySQLFlushStatementTestCase) expected);
        } else if (actual instanceof MySQLInstallPluginStatement) {
            MySQLInstallPluginStatementAssert.assertIs(assertContext, (MySQLInstallPluginStatement) actual, (MySQLInstallPluginStatementTestCase) expected);
        } else if (actual instanceof MySQLCloneStatement) {
            MySQLCloneStatementAssert.assertIs(assertContext, (MySQLCloneStatement) actual, (MySQLCloneStatementTestCase) expected);
        } else if (actual instanceof MySQLUninstallComponentStatement) {
            MySQLUninstallComponentStatementAssert.assertIs(assertContext, (MySQLUninstallComponentStatement) actual, (MySQLUninstallComponentStatementTestCase) expected);
        } else if (actual instanceof MySQLCreateResourceGroupStatement) {
            MySQLCreateResourceGroupStatementAssert.assertIs(assertContext, (MySQLCreateResourceGroupStatement) actual, (MySQLCreateResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLUninstallPluginStatement) {
            MySQLUninstallPluginStatementAssert.assertIs(assertContext, (MySQLUninstallPluginStatement) actual, (MySQLUninstallPluginStatementTestCase) expected);
        } else if (actual instanceof MySQLRestartStatement) {
            MySQLRestartStatementAssert.assertIs(assertContext, (MySQLRestartStatement) actual, (MySQLRestartStatementTestCase) expected);
        } else if (actual instanceof MySQLSetResourceGroupStatement) {
            MySQLSetResourceGroupStatementAssert.assertIs(assertContext, (MySQLSetResourceGroupStatement) actual, (MySQLSetResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLOptimizeTableStatement) {
            MySQLOptimizeTableStatementAssert.assertIs(assertContext, (MySQLOptimizeTableStatement) actual, (MySQLOptimizeTableStatementTestCase) expected);
        } else if (actual instanceof MySQLRepairTableStatement) {
            MySQLRepairTableStatementAssert.assertIs(assertContext, (MySQLRepairTableStatement) actual, (MySQLRepairTableStatementTestCase) expected);
        } else if (actual instanceof MySQLBinlogStatement) {
            MySQLBinlogStatementAssert.assertIs(assertContext, (MySQLBinlogStatement) actual, (MySQLBinlogStatementTestCase) expected);
        } else if (actual instanceof MySQLShowFunctionStatusStatement) {
            MySQLShowFunctionStatusStatementAssert.assertIs(assertContext, (MySQLShowFunctionStatusStatement) actual, (MySQLShowFunctionStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowProcedureStatusStatement) {
            MySQLShowProcedureStatusStatementAssert.assertIs(assertContext, (MySQLShowProcedureStatusStatement) actual, (MySQLShowProcedureStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowReplicasStatement) {
            MySQLShowReplicasStatementAssert.assertIs(assertContext, (MySQLShowReplicasStatement) actual, (MySQLShowReplicasStatementTestCase) expected);
        } else if (actual instanceof MySQLShowReplicaStatusStatement) {
            MySQLShowReplicaStatusStatementAssert.assertIs(assertContext, (MySQLShowReplicaStatusStatement) actual, (MySQLShowReplicaStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowSlaveStatusStatement) {
            MySQLShowSlaveStatusStatementAssert.assertIs(assertContext, (MySQLShowSlaveStatusStatement) actual, (MySQLShowSlaveStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLShowSlaveHostsStatement) {
            MySQLShowSlaveHostsStatementAssert.assertIs(assertContext, (MySQLShowSlaveHostsStatement) actual, (MySQLShowSlaveHostsStatementTestCase) expected);
        } else if (actual instanceof MySQLResetStatement) {
            MySQLResetStatementAssert.assertIs(assertContext, (MySQLResetStatement) actual, (MySQLResetStatementTestCase) expected);
        } else if (actual instanceof MySQLResetPersistStatement) {
            MySQLResetPersistStatementAssert.assertIs(assertContext, (MySQLResetPersistStatement) actual, (MySQLResetPersistStatementTestCase) expected);
        } else if (actual instanceof MySQLShowProcedureCodeStatement) {
            MySQLShowProcedureCodeStatementAssert.assertIs(assertContext, (MySQLShowProcedureCodeStatement) actual, (MySQLShowProcedureCodeStatementTestCase) expected);
        } else if (actual instanceof MySQLKillStatement) {
            MySQLKillStatementAssert.assertIs(assertContext, (MySQLKillStatement) actual, (MySQLKillStatementTestCase) expected);
        } else if (actual instanceof MySQLCacheIndexStatement) {
            MySQLCacheIndexStatementAssert.assertIs(assertContext, (MySQLCacheIndexStatement) actual, (MySQLCacheIndexStatementTestCase) expected);
        } else if (actual instanceof MySQLLoadIndexInfoStatement) {
            MySQLLoadIndexInfoStatementAssert.assertIs(assertContext, (MySQLLoadIndexInfoStatement) actual, (MySQLLoadIndexInfoStatementTestCase) expected);
        } else if (actual instanceof MySQLShutdownStatement) {
            MySQLShutdownStatementAssert.assertIs(assertContext, (MySQLShutdownStatement) actual, (MySQLShutdownStatementTestCase) expected);
        } else if (actual instanceof MySQLShowOpenTablesStatement) {
            MySQLShowOpenTablesStatementAssert.assertIs(assertContext, (MySQLShowOpenTablesStatement) actual, (MySQLShowOpenTablesStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTriggersStatement) {
            MySQLShowTriggersStatementAssert.assertIs(assertContext, (MySQLShowTriggersStatement) actual, (MySQLShowTriggersStatementTestCase) expected);
        } else if (actual instanceof MySQLShowStatusStatement) {
            MySQLShowStatusStatementAssert.assertIs(assertContext, (MySQLShowStatusStatement) actual, (MySQLShowStatusStatementTestCase) expected);
        } else if (actual instanceof MySQLCheckTableStatement) {
            MySQLCheckTableStatementAssert.assertIs(assertContext, (MySQLCheckTableStatement) actual, (MySQLCheckTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowEventsStatement) {
            MySQLShowEventsStatementAssert.assertIs(assertContext, (MySQLShowEventsStatement) actual, (MySQLShowEventsStatementTestCase) expected);
        } else if (actual instanceof MySQLDropResourceGroupStatement) {
            MySQLDropResourceGroupStatementAssert.assertIs(assertContext, (MySQLDropResourceGroupStatement) actual, (MySQLDropResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLAlterResourceGroupStatement) {
            MySQLAlterResourceGroupStatementAssert.assertIs(assertContext, (MySQLAlterResourceGroupStatement) actual, (MySQLAlterResourceGroupStatementTestCase) expected);
        } else if (actual instanceof MySQLChecksumTableStatement) {
            MySQLChecksumTableStatementAssert.assertIs(assertContext, (MySQLChecksumTableStatement) actual, (MySQLChecksumTableStatementTestCase) expected);
        } else if (actual instanceof MySQLShowCollationStatement) {
            MySQLShowCollationStatementAssert.assertIs(assertContext, (MySQLShowCollationStatement) actual, (MySQLShowCollationStatementTestCase) expected);
        } else if (actual instanceof MySQLShowVariablesStatement) {
            MySQLShowVariablesStatementAssert.assertIs(assertContext, (MySQLShowVariablesStatement) actual, (MySQLShowVariablesStatementTestCase) expected);
        } else if (actual instanceof MySQLDelimiterStatement) {
            MySQLDelimiterStatementAssert.assertIs(assertContext, (MySQLDelimiterStatement) actual, (MySQLDelimiterStatementTestCase) expected);
        } else if (actual instanceof MySQLShowBinlogEventsStatement) {
            MySQLShowBinlogEventsStatementAssert.assertIs(assertContext, (MySQLShowBinlogEventsStatement) actual, (MySQLShowBinlogEventsStatementTestCase) expected);
        } else if (actual instanceof MySQLShowTransactionStatement) {
            MySQLShowTransactionStatementAssert.assertIs(assertContext, (MySQLShowTransactionStatement) actual, (MySQLShowTransactionStatementTestCase) expected);
        }
    }
}
