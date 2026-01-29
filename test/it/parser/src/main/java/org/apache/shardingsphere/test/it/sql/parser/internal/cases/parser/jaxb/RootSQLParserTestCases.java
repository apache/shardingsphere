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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.CommonStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisCreateSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisSwitchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.CreateEncryptKeyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterStoragePolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisResumeJobStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisDropSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowSqlBlockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCancelMaterializedViewTaskStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowRoutineLoadTaskStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowRoutineLoadStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowCreateMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowAlterTableMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisDropMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisPauseMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisRefreshMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisResumeMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCreateMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.show.DorisShowViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.catalog.AlterCatalogStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.HiveAbortStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLCloneStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLCreateLoadableFunctionTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLDelimiterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLFlushStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.MySQLHelpStatementTestCase;
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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.character.MySQLShowCharacterSetStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.character.MySQLShowCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.column.MySQLDescribeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.column.MySQLShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.database.MySQLShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.event.MySQLShowEventsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.function.MySQLShowFunctionStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.index.MySQLShowIndexStatementTestCase;
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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterResourceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.show.DorisShowQueryStatsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.oracle.OracleSpoolStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.postgresql.PostgreSQLResetParameterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.EmptyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.ExplainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.SetParameterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.standard.ShowStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.DorisRefreshStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.mysql.MySQLRenameUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.mysql.MySQLSetDefaultRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.mysql.MySQLSetPasswordStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.postgresql.PostgreSQLReassignOwnedStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerAlterLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerCreateLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerDenyUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerDropLoginStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerSetUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.AlterRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.AlterUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.CreateGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.CreateRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.CreateUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.DropRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.DropUserStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.GrantStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.RevertStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.RevokeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.SetRoleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.opengauss.OpengaussAlterDirectoryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterAuditPolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterHierarchyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterSessionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAnalyzeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAuditStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleDropPackageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleNoAuditStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OraclePurgeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleSwitchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.cluster.OracleAlterClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.cluster.OracleCreateClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.cluster.OracleDropClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.context.OracleCreateContextStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.context.OracleDropContextStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.database.OracleAlterDatabaseDictionaryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.database.OracleAlterDatabaseLinkStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.database.OracleAlterPluggableDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.database.OracleCreateDatabaseLinkStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.database.OracleDropDatabaseLinkStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.database.OracleDropPluggableDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.dimension.OracleAlterAttributeDimensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.dimension.OracleAlterDimensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.dimension.OracleCreateDimensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.dimension.OracleDropDimensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.diskgroup.OracleAlterDiskgroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.diskgroup.OracleCreateDiskgroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.diskgroup.OracleDropDiskgroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.edition.OracleCreateEditionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.edition.OracleDropEditionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.file.OracleCreateControlFileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.file.OracleCreatePFileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.file.OracleCreateSPFileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.flashback.OracleAlterFlashbackArchiveStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.flashback.OracleCreateFlashbackArchiveStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.flashback.OracleDropFlashbackArchiveStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.flashback.OracleFlashbackDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.flashback.OracleFlashbackTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.index.OracleAlterIndexTypeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.index.OracleDropIndexTypeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.java.OracleAlterJavaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.java.OracleCreateJavaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.java.OracleDropJavaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.join.OracleAlterInmemoryJoinGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.join.OracleCreateInmemoryJoinGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.join.OracleDropInmemoryJoinGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.library.OracleAlterLibraryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.library.OracleCreateLibraryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.library.OracleDropLibraryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.lockdown.OracleAlterLockdownProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.lockdown.OracleCreateLockdownProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.lockdown.OracleDropLockdownProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.outline.OracleAlterOutlineStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.outline.OracleCreateOutlineStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.outline.OracleDropOutlineStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.profile.OracleAlterProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.profile.OracleCreateProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.profile.OracleDropProfileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.restore.OracleCreateRestorePointStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.restore.OracleDropRestorePointStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.rollback.OracleAlterRollbackSegmentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.rollback.OracleCreateRollbackSegmentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.rollback.OracleDropRollbackSegmentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.statistics.OracleAssociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.statistics.OracleDisassociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.view.OracleAlterAnalyticViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.view.OracleAlterMaterializedViewLogStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.view.OracleCreateMaterializedViewLogStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.view.OracleDropMaterializedViewLogStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.zone.OracleAlterMaterializedZonemapStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.zone.OracleDropMaterializedZonemapStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterDefaultPrivilegesTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDeclareStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDiscardStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropDomainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropOperatorClassStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropOperatorFamilyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropOwnedStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropTypeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLListenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLNotifyStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLOpenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLReindexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLSecurityLabelStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLUnlistenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.accessmethod.PostgreSQLCreateAccessMethodStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.accessmethod.PostgreSQLDropAccessMethodStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.aggregate.PostgreSQLAlterAggregateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.aggregate.PostgreSQLCreateAggregateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.aggregate.PostgreSQLDropAggregateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.cast.PostgreSQLCreateCastStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.cast.PostgreSQLDropCastStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.collation.PostgreSQLAlterCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.collation.PostgreSQLDropCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.conversion.PostgreSQLAlterConversionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.conversion.PostgreSQLCreateConversionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.conversion.PostgreSQLDropConversionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.event.PostgreSQLCreateEventTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.event.PostgreSQLDropEventTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.extension.PostgreSQLAlterExtensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.extension.PostgreSQLCreateExtensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.extension.PostgreSQLDropExtensionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigndata.PostgreSQLAlterForeignDataWrapperTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigndata.PostgreSQLCreateForeignDataWrapperStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigndata.PostgreSQLDropForeignDataWrapperStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigntable.PostgreSQLAlterForeignTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigntable.PostgreSQLCreateForeignTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigntable.PostgreSQLDropForeignTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.group.PostgreSQLAlterGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.group.PostgreSQLDropGroupStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.language.PostgreSQLAlterLanguageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.language.PostgreSQLCreateLanguageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.language.PostgreSQLDropLanguageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.policy.PostgreSQLAlterPolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.policy.PostgreSQLCreatePolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.policy.PostgreSQLDropPolicyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.publication.PostgreSQLAlterPublicationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.publication.PostgreSQLCreatePublicationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.publication.PostgreSQLDropPublicationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.routine.PostgreSQLAlterRoutineStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.routine.PostgreSQLDropRoutineStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.rule.PostgreSQLAlterRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.rule.PostgreSQLCreateRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.rule.PostgreSQLDropRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.statistics.PostgreSQLAlterStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.statistics.PostgreSQLDropStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.subscription.PostgreSQLAlterSubscriptionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.subscription.PostgreSQLDropSubscriptionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.textsearch.PostgreSQLAlterTextSearchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.textsearch.PostgreSQLCreateTextSearchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.textsearch.PostgreSQLDropTextSearchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.sqlserver.service.SQLServerAlterServiceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.sqlserver.service.SQLServerCreateServiceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.sqlserver.service.SQLServerDropServiceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.sqlserver.statistics.SQLServerUpdateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.AlterPackageStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.CloseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.CommentStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.CreateCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.CursorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.ExecuteStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.FetchStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.MoveStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.PreparedStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.RenameStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.TruncateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.database.AlterDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.database.CreateDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.database.DropDatabaseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.directory.CreateDirectoryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.directory.DropDirectoryStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.domain.AlterDomainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.domain.CreateDomainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.AlterFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.CreateFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.DropFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.ReloadFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.AlterIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.CreateIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.DropIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.CancelBuildIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.BuildIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.macro.CreateMacroStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.macro.DropMacroStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ShowBuildIndexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.operator.AlterOperatorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.operator.CreateOperatorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.operator.DropOperatorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.procedure.AlterProcedureStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.procedure.DropProcedureStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.schema.AlterSchemaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.schema.CreateSchemaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.schema.DropSchemaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.sequence.AlterSequenceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.sequence.CreateSequenceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.sequence.DropSequenceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.server.AlterServerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.server.CreateServerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.server.DropServerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.synonym.AlterSynonymStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.synonym.CreateSynonymStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.synonym.DropSynonymStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.AlterTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.CreateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.DropTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DropEncryptKeyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.RenameTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.tablespace.AlterTablespaceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.tablespace.CreateTablespaceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.tablespace.DropTableSpaceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.trigger.AlterTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.trigger.CreateTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.trigger.DropTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.type.AlterTypeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.type.CreateTypeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.AlterViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.CreateMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.CreateViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.DropMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.DropViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.view.RefreshMatViewStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.mysql.MySQLHandlerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.mysql.MySQLImportStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.mysql.MySQLLoadDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.mysql.MySQLLoadXMLStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.postgresql.PostgreSQLCheckpointStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.postgresql.PostgreSQLCopyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.CallStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.DeleteStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.DoStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.InsertStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.MergeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.SelectStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.UpdateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.plsql.CreateProcedureTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterGlobalClockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterLocalTransactionRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterReadwriteSplittingStorageUnitStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterSQLTranslatorRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterTransmissionRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.AlterXATransactionRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ApplyDistSQLStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ConvertYamlConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.DiscardDistSQLStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ExportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ExportMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ExportMetaDataToFileStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ExportStorageNodesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ImportDatabaseConfigurationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ImportMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.LabelComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.PrepareDistSQLStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.RefreshTableMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.SetDistVariableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowComputeNodeInfoStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowComputeNodeModeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowComputeNodesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowDistVariableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowDistVariablesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowMigrationListStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowStatusFromReadwriteSplittingRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.ShowTableMetaDataStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.UnlabelComputeNodeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.DropStreamingStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.ShowStreamingListStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.ShowStreamingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.cdc.ShowStreamingStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.CheckMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.CommitMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.DropMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.MigrateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.RegisterMigrationSourceStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.RollbackMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.ShowMigrationCheckAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.ShowMigrationCheckStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.ShowMigrationSourceStorageUnitsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.ShowMigrationStatusStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StartMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StartMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StopMigrationCheckStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.StopMigrationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ral.migration.UnregisterMigrationSourceStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.AlterStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.RegisterStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.resource.UnregisterStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.encrypt.AlterEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.encrypt.CreateEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.encrypt.DropEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.AlterMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.CreateMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.mask.DropMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.readwritesplitting.AlterReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.readwritesplitting.CreateReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.readwritesplitting.DropReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.AlterDefaultShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.AlterShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.CreateDefaultShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.CreateShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.DropShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.shadow.DropShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterShardingAuditorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterShardingAutoTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterShardingTableReferenceRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.AlterShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateBroadcastTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateShardingAuditorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateShardingAutoTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateShardingTableReferenceRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.CreateShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropBroadcastTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingAuditorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingKeyGeneratorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingTableReferenceRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.sharding.DropShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rdl.rule.single.SetDefaultSingleTableStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rl.ChangeReplicationSourceToStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rl.StartReplicaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.authority.ShowAuthorityRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.broadcast.CountBroadcastRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.encrypt.CountEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.encrypt.ShowEncryptRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.globalclock.ShowGlobalClockRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.mask.CountMaskRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.mask.ShowMaskRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.parser.ShowSQLParserRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.readwritesplitting.CountReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.readwritesplitting.ShowReadwriteSplittingRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.shadow.CountShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.shadow.ShowDefaultShadowAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.shadow.ShowShadowAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.shadow.ShowShadowRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.shadow.ShowShadowTableRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.CountShardingRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingAuditorsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingKeyGeneratorsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingTableNodesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingTableReferenceRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingTableRulesUsedAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingTableRulesUsedAuditorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowShardingTableRulesUsedKeyGeneratorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowUnusedShardingAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowUnusedShardingAuditorsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.sharding.ShowUnusedShardingKeyGeneratorsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.single.CountSingleTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.single.ShowDefaultSingleTableStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.transaction.ShowTransactionRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.rule.translator.ShowSQLTranslatorRuleStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.storage.unit.ShowRulesUsedStorageUnitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.storage.unit.ShowStorageUnitsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rql.table.ShowTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rul.ParseStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.rul.PreviewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.BeginTransactionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.CommitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.LockStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.PrepareTransactionTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.RollbackStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SavepointStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetConstraintsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetTransactionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.UnlockStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.XATestCase;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL parser test cases for XML root tag.
 */
@XmlRootElement(name = "sql-parser-test-cases")
@Getter
public final class RootSQLParserTestCases {
    
    @XmlElement(name = "select")
    private final List<SelectStatementTestCase> selectTestCases = new LinkedList<>();
    
    @XmlElement(name = "update")
    private final List<UpdateStatementTestCase> updateTestCases = new LinkedList<>();
    
    @XmlElement(name = "delete")
    private final List<DeleteStatementTestCase> deleteTestCases = new LinkedList<>();
    
    @XmlElement(name = "insert")
    private final List<InsertStatementTestCase> insertTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-cluster")
    private final List<OracleCreateClusterStatementTestCase> createClusterTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-tablespace")
    private final List<DropTableSpaceStatementTestCase> dropTableSpaceTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-table")
    private final List<CreateTableStatementTestCase> createTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-edition")
    private final List<OracleCreateEditionStatementTestCase> createEditionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-table")
    private final List<AlterTableStatementTestCase> alterTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-tablespace")
    private final List<AlterTablespaceStatementTestCase> alterTablespaceTestCases = new LinkedList<>();
    
    @XmlElement(name = "rename-table")
    private final List<RenameTableStatementTestCase> renameTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "resume-job")
    private final List<DorisResumeJobStatementTestCase> resumeJobTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-catalog")
    private final List<AlterCatalogStatementTestCase> alterCatalogTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-table")
    private final List<DropTableStatementTestCase> dropTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-encryptkey")
    private final List<CreateEncryptKeyStatementTestCase> createEncryptKeyTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-encryptkey")
    private final List<DropEncryptKeyStatementTestCase> dropEncryptKeyTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-text-search")
    private final List<PostgreSQLDropTextSearchStatementTestCase> dropTextSearchTestCases = new LinkedList<>();
    
    @XmlElement(name = "truncate")
    private final List<TruncateStatementTestCase> truncateTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-edition")
    private final List<OracleDropEditionStatementTestCase> dropEditionTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-outline")
    private final List<OracleDropOutlineStatementTestCase> dropOutlineTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-outline")
    private final List<OracleAlterOutlineStatementTestCase> alterOutlineTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-index")
    private final List<CreateIndexStatementTestCase> createIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-index")
    private final List<AlterIndexStatementTestCase> alterIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-index")
    private final List<DropIndexStatementTestCase> dropIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-build-index")
    private final List<ShowBuildIndexStatementTestCase> showBuildIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "build-index")
    private final List<BuildIndexStatementTestCase> buildIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "cancel-build-index")
    private final List<CancelBuildIndexStatementTestCase> cancelBuildIndexStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sql-block-rule")
    private final List<DorisAlterSqlBlockRuleStatementTestCase> alterSqlBlockRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-sql-block-rule")
    private final List<DorisDropSqlBlockRuleStatementTestCase> dropSqlBlockRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sql-block-rule")
    private final List<DorisShowSqlBlockRuleStatementTestCase> showSqlBlockRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-routine-load-task")
    private final List<DorisShowRoutineLoadTaskStatementTestCase> showRoutineLoadTaskTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-routine-load")
    private final List<DorisShowRoutineLoadStatementTestCase> showRoutineLoadTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-constraints")
    private final List<SetConstraintsStatementTestCase> setConstraintsTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-transaction")
    private final List<SetTransactionStatementTestCase> setTransactionTestCases = new LinkedList<>();
    
    @XmlElement(name = "begin-transaction")
    private final List<BeginTransactionStatementTestCase> beginTransactionTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-auto-commit")
    private final List<SetAutoCommitStatementTestCase> setAutoCommitTestCases = new LinkedList<>();
    
    @XmlElement(name = "commit")
    private final List<CommitStatementTestCase> commitTestCases = new LinkedList<>();
    
    @XmlElement(name = "rollback")
    private final List<RollbackStatementTestCase> rollbackTestCases = new LinkedList<>();
    
    @XmlElement(name = "savepoint")
    private final List<SavepointStatementTestCase> savepointTestCases = new LinkedList<>();
    
    @XmlElement(name = "grant")
    private final List<GrantStatementTestCase> grantTestCases = new LinkedList<>();
    
    @XmlElement(name = "revoke")
    private final List<RevokeStatementTestCase> revokeTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-user")
    private final List<CreateUserStatementTestCase> createUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-user")
    private final List<AlterUserStatementTestCase> alterUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-user")
    private final List<DropUserStatementTestCase> dropUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "rename-user")
    private final List<MySQLRenameUserStatementTestCase> renameUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "deny-user")
    private final List<SQLServerDenyUserStatementTestCase> denyUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-login")
    private final List<SQLServerCreateLoginStatementTestCase> createLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-login")
    private final List<SQLServerAlterLoginStatementTestCase> alterLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-login")
    private final List<SQLServerDropLoginStatementTestCase> dropLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-role")
    private final List<CreateRoleStatementTestCase> createRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-role")
    private final List<AlterRoleStatementTestCase> alterRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-role")
    private final List<DropRoleStatementTestCase> dropRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-alter-system")
    private final List<DorisAlterSystemStatementTestCase> dorisAlterSystemTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sql-block-rule")
    private final List<DorisCreateSqlBlockRuleStatementTestCase> createSqlBlockRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-default-role")
    private final List<MySQLSetDefaultRoleStatementTestCase> setDefaultRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-role")
    private final List<SetRoleStatementTestCase> setRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-password")
    private final List<MySQLSetPasswordStatementTestCase> setPasswordTestCases = new LinkedList<>();
    
    @XmlElement(name = "use")
    private final List<MySQLUseStatementTestCase> useTestCases = new LinkedList<>();
    
    @XmlElement(name = "switch-catalog")
    private final List<DorisSwitchStatementTestCase> switchCatalogTestCases = new LinkedList<>();
    
    @XmlElement(name = "explain")
    private final List<ExplainStatementTestCase> explainTestCases = new LinkedList<>();
    
    @XmlElement(name = "describe")
    private final List<MySQLDescribeStatementTestCase> describeTestCases = new LinkedList<>();
    
    @XmlElement(name = "refresh")
    private final List<DorisRefreshStatementTestCase> refreshTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-databases")
    private final List<MySQLShowDatabasesStatementTestCase> showDatabasesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-tables")
    private final List<MySQLShowTablesStatementTestCase> showTablesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-function-status")
    private final List<MySQLShowFunctionStatusStatementTestCase> showFunctionStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-procedure-status")
    private final List<MySQLShowProcedureStatusStatementTestCase> showProcedureStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-procedure-code")
    private final List<MySQLShowProcedureCodeStatementTestCase> showProcedureCodeTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-relay-log-events")
    private final List<MySQLShowRelayLogEventsStatementTestCase> showRelayLogEventsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-slave-hosts")
    private final List<MySQLShowSlaveHostsStatementTestCase> showSlaveHostsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-columns")
    private final List<MySQLShowColumnsStatementTestCase> showColumnsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-table")
    private final List<MySQLShowCreateTableStatementTestCase> showCreateTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-trigger")
    private final List<MySQLShowCreateTriggerStatementTestCase> showCreateTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-resource-group")
    private final List<MySQLAlterResourceGroupStatementTestCase> alterResourceGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-resource")
    private final List<DorisAlterResourceStatementTestCase> alterResourceTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-resource-group")
    private final List<MySQLCreateResourceGroupStatementTestCase> createResourceGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-resource-group")
    private final List<MySQLDropResourceGroupStatementTestCase> dropResourceGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "binlog")
    private final List<MySQLBinlogStatementTestCase> binlogTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-user")
    private final List<MySQLShowCreateUserStatementTestCase> showCreateUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-table-status")
    private final List<MySQLShowTableStatusStatementTestCase> showTableStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-index")
    private final List<MySQLShowIndexStatementTestCase> showIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "show")
    private final List<ShowStatementTestCase> showTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-parameter")
    private final List<SetParameterStatementTestCase> setParameterTestCases = new LinkedList<>();
    
    @XmlElement(name = "common")
    private final List<CommonStatementTestCase> commonTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-function")
    private final List<AlterFunctionStatementTestCase> alterFunctionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-database")
    private final List<AlterDatabaseStatementTestCase> alterDatabaseTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-dimension")
    private final List<OracleAlterDimensionStatementTestCase> alterDimensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-procedure")
    private final List<AlterProcedureStatementTestCase> alterProcedureTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-publication")
    private final List<PostgreSQLAlterPublicationStatementTestCase> alterPublicationTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-policy")
    private final List<PostgreSQLAlterPolicyStatementTestCase> alterPolicyTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-routine")
    private final List<PostgreSQLAlterRoutineStatementTestCase> alterRoutineTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-server")
    private final List<AlterServerStatementTestCase> alterServerTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-statistics")
    private final List<PostgreSQLAlterStatisticsStatementTestCase> alterStatisticsTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-subscription")
    private final List<PostgreSQLAlterSubscriptionStatementTestCase> alterSubscriptionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-session")
    private final List<OracleAlterSessionStatementTestCase> alterSessionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-synonym")
    private final List<AlterSynonymStatementTestCase> alterSynonymTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-type")
    private final List<AlterTypeStatementTestCase> alterTypeTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-rule")
    private final List<PostgreSQLAlterRuleStatementTestCase> alterRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-directory")
    private final List<OpengaussAlterDirectoryStatementTestCase> alterDirectoryTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-system")
    private final List<OracleAlterSystemStatementTestCase> alterSystemTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-database")
    private final List<CreateDatabaseStatementTestCase> createDatabaseTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-database-link")
    private final List<OracleCreateDatabaseLinkStatementTestCase> createDatabaseLinkTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-database-link")
    private final List<OracleDropDatabaseLinkStatementTestCase> dropDatabaseLinkTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-database-link")
    private final List<OracleAlterDatabaseLinkStatementTestCase> alterDatabaseLinkTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-database-dictionary")
    private final List<OracleAlterDatabaseDictionaryStatementTestCase> alterDatabaseDictionaryTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-view")
    private final List<AlterViewStatementTestCase> alterViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-package")
    private final List<AlterPackageStatementTestCase> alterPackageTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-storage-policy")
    private final List<DorisAlterStoragePolicyStatementTestCase> alterStoragePolicyTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-package")
    private final List<OracleDropPackageStatementTestCase> dropPackageTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-dimension")
    private final List<OracleCreateDimensionStatementTestCase> createDimensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-function")
    private final List<CreateFunctionStatementTestCase> createFunctionTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-procedure")
    private final List<CreateProcedureTestCase> createProcedureTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-server")
    private final List<CreateServerStatementTestCase> createServerTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-trigger")
    private final List<CreateTriggerStatementTestCase> createTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-view")
    private final List<CreateViewStatementTestCase> createViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "pause-materialized-view")
    private final List<DorisPauseMaterializedViewStatementTestCase> pauseMaterializedViewJobTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-resume-materialized-view")
    private final List<DorisResumeMaterializedViewStatementTestCase> resumeMaterializedViewJobTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-drop-materialized-view")
    private final List<DorisDropMaterializedViewStatementTestCase> dorisDropMaterializedViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-refresh-materialized-view")
    private final List<DorisRefreshMaterializedViewStatementTestCase> dorisRefreshMaterializedViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-view")
    private final List<DorisShowViewStatementTestCase> showViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-show-create-materialized-view")
    private final List<DorisShowCreateMaterializedViewStatementTestCase> showCreateMaterializedViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-show-alter-table-materialized-view")
    private final List<DorisShowAlterTableMaterializedViewStatementTestCase> showAlterTableMaterializedViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-create-materialized-view")
    private final List<DorisCreateMaterializedViewStatementTestCase> dorisCreateMaterializedViewStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-alter-materialized-view")
    private final List<DorisAlterMaterializedViewStatementTestCase> dorisAlterMaterializedViewStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "doris-cancel-materialized-view-task")
    private final List<DorisCancelMaterializedViewTaskStatementTestCase> dorisCancelMaterializedViewTaskStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-synonym")
    private final List<CreateSynonymStatementTestCase> createSynonymTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-aggregate")
    private final List<PostgreSQLCreateAggregateStatementTestCase> createAggregateTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-publication")
    private final List<PostgreSQLCreatePublicationStatementTestCase> createPublicationTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-synonym")
    private final List<DropSynonymStatementTestCase> dropSynonymTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-directory")
    private final List<CreateDirectoryStatementTestCase> createDirectoryTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-domain")
    private final List<CreateDomainStatementTestCase> createDomainTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-rule")
    private final List<PostgreSQLCreateRuleStatementTestCase> createRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-type")
    private final List<CreateTypeStatementTestCase> createTypeTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-extension")
    private final List<PostgreSQLCreateExtensionStatementTestCase> createExtensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-extension")
    private final List<PostgreSQLAlterExtensionStatementTestCase> alterExtensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-extension")
    private final List<PostgreSQLDropExtensionStatementTestCase> dropExtensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "declare")
    private final List<PostgreSQLDeclareStatementTestCase> declareTestCases = new LinkedList<>();
    
    @XmlElement(name = "discard")
    private final List<PostgreSQLDiscardStatementTestCase> discardTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-database")
    private final List<DropDatabaseStatementTestCase> dropDatabaseTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-dimension")
    private final List<OracleDropDimensionStatementTestCase> dropDimensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-profile")
    private final List<OracleDropProfileStatementTestCase> dropProfileStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-directory")
    private final List<DropDirectoryStatementTestCase> dropDirectoryTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-function")
    private final List<DropFunctionStatementTestCase> dropFunctionTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-group")
    private final List<PostgreSQLDropGroupStatementTestCase> dropGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-policy")
    private final List<PostgreSQLDropPolicyStatementTestCase> dropPolicyTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-procedure")
    private final List<DropProcedureStatementTestCase> dropProcedureTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-routine")
    private final List<PostgreSQLDropRoutineStatementTestCase> dropRoutineTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-rule")
    private final List<PostgreSQLDropRuleStatementTestCase> dropRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-statistics")
    private final List<PostgreSQLDropStatisticsStatementTestCase> dropStatisticsTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-publication")
    private final List<PostgreSQLDropPublicationStatementTestCase> dropPublicationTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-subscription")
    private final List<PostgreSQLDropSubscriptionStatementTestCase> dropSubscriptionTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-server")
    private final List<DropServerStatementTestCase> dropServerTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-trigger")
    private final List<DropTriggerStatementTestCase> dropTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-trigger")
    private final List<AlterTriggerStatementTestCase> alterTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-event-trigger")
    private final List<PostgreSQLDropEventTriggerStatementTestCase> dropEventTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-domain")
    private final List<PostgreSQLDropDomainStatementTestCase> dropDomainTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-triggers")
    private final List<MySQLShowTriggersStatementTestCase> showTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-view")
    private final List<DropViewStatementTestCase> dropViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "call")
    private final List<CallStatementTestCase> callProcedureTestCases = new LinkedList<>();
    
    @XmlElement(name = "do")
    private final List<DoStatementTestCase> doTestCases = new LinkedList<>();
    
    @XmlElement(name = "handler")
    private final List<MySQLHandlerStatementTestCase> handlerStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "copy")
    private final List<PostgreSQLCopyStatementTestCase> copyTestCases = new LinkedList<>();
    
    @XmlElement(name = "import")
    private final List<MySQLImportStatementTestCase> importTestCase = new LinkedList<>();
    
    @XmlElement(name = "load-data")
    private final List<MySQLLoadDataStatementTestCase> loadDataTestCases = new LinkedList<>();
    
    @XmlElement(name = "load-xml")
    private final List<MySQLLoadXMLStatementTestCase> loadXmlTestCases = new LinkedList<>();
    
    @XmlElement(name = "xa")
    private final List<XATestCase> xaTestCases = new LinkedList<>();
    
    @XmlElement(name = "merge")
    private final List<MergeStatementTestCase> mergeTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sequence")
    private final List<CreateSequenceStatementTestCase> createSequenceTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sequence")
    private final List<AlterSequenceStatementTestCase> alterSequenceTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-sequence")
    private final List<DropSequenceStatementTestCase> dropSequenceTestCases = new LinkedList<>();
    
    @XmlElement(name = "analyze")
    private final List<OracleAnalyzeStatementTestCase> analyzeTestCases = new LinkedList<>();
    
    @XmlElement(name = "associate-statistics")
    private final List<OracleAssociateStatisticsStatementTestCase> associateStatisticsTestCases = new LinkedList<>();
    
    @XmlElement(name = "disassociate-statistics")
    private final List<OracleDisassociateStatisticsStatementTestCase> disassociateStatisticsTestCases = new LinkedList<>();
    
    @XmlElement(name = "audit")
    private final List<OracleAuditStatementTestCase> auditTestCases = new LinkedList<>();
    
    @XmlElement(name = "no-audit")
    private final List<OracleNoAuditStatementTestCase> noAuditTestCases = new LinkedList<>();
    
    @XmlElement(name = "comment")
    private final List<CommentStatementTestCase> commentTestCases = new LinkedList<>();
    
    @XmlElement(name = "flashback-database")
    private final List<OracleFlashbackDatabaseStatementTestCase> flashbackDatabaseTestCases = new LinkedList<>();
    
    @XmlElement(name = "flashback-table")
    private final List<OracleFlashbackTableStatementTestCase> flashbackTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "purge")
    private final List<OraclePurgeStatementTestCase> purgeTestCases = new LinkedList<>();
    
    @XmlElement(name = "rename")
    private final List<RenameStatementTestCase> renameTestCases = new LinkedList<>();
    
    @XmlElement(name = "register-storage-unit")
    private final List<RegisterStorageUnitStatementTestCase> registerStorageUnitTestCases = new LinkedList<>();
    
    @XmlElement(name = "register-migration-source-storage-unit")
    private final List<RegisterMigrationSourceStorageUnitStatementTestCase> registerMigrationSourceStorageUnitStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-storage-unit")
    private final List<AlterStorageUnitStatementTestCase> alterStorageUnitTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-encrypt-rule")
    private final List<AlterEncryptRuleStatementTestCase> alterEncryptRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-readwrite-splitting-rule")
    private final List<AlterReadwriteSplittingRuleStatementTestCase> alterReadwriteSplittingRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-table-reference-rule")
    private final List<AlterShardingTableReferenceRulesStatementTestCase> alterShardingTableReferenceRulesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-auto-table-rule")
    private final List<AlterShardingAutoTableRuleStatementTestCase> alterShardingAutoTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-table-rule")
    private final List<AlterShardingTableRuleStatementTestCase> alterShardingTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-encrypt-rule")
    private final List<CreateEncryptRuleStatementTestCase> createEncryptRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-mask-rule")
    private final List<CreateMaskRuleStatementTestCase> createMaskRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-mask-rule")
    private final List<AlterMaskRuleStatementTestCase> alterMaskRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-mask-rule")
    private final List<DropMaskRuleStatementTestCase> dropMaskRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-readwrite-splitting-rule")
    private final List<CreateReadwriteSplittingRuleStatementTestCase> createReadwriteSplittingRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-table-reference-rule")
    private final List<CreateShardingTableReferenceRuleStatementTestCase> createShardingTableReferenceRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-broadcast-table-rule")
    private final List<CreateBroadcastTableRuleStatementTestCase> createBroadcastTableRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-auto-table-rule")
    private final List<CreateShardingAutoTableRuleStatementTestCase> createShardingAutoTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-table-rule")
    private final List<CreateShardingTableRuleStatementTestCase> createShardingTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-encrypt-rule")
    private final List<DropEncryptRuleStatementTestCase> dropEncryptRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-readwrite-splitting-rule")
    private final List<DropReadwriteSplittingRuleStatementTestCase> dropReadwriteSplittingRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "unregister-storage-unit")
    private final List<UnregisterStorageUnitStatementTestCase> unregisterStorageUnitStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-table-reference-rule")
    private final List<DropShardingTableReferenceRuleStatementTestCase> dropShardingTableReferenceRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-broadcast-table-rule")
    private final List<DropBroadcastTableRuleStatementTestCase> dropBroadcastTableRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-table-rule")
    private final List<DropShardingTableRuleStatementTestCase> dropShardingTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-encrypt-rules")
    private final List<ShowEncryptRulesStatementTestCase> showEncryptRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-readwrite-splitting-rules")
    private final List<ShowReadwriteSplittingRulesStatementTestCase> showReadwriteSplittingRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-reference-rules")
    private final List<ShowShardingTableReferenceRulesStatementTestCase> showShardingTableReferenceRulesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-broadcast-table-rules")
    private final List<ShowBroadcastTableRulesStatementTestCase> showBroadcastTableRulesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-algorithms")
    private final List<ShowShardingAlgorithmsStatementTestCase> showShardingAlgorithmsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-auditors")
    private final List<ShowShardingAuditorsStatementTestCase> showShardingAuditorsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rules")
    private final List<ShowShardingTableRulesStatementTestCase> showShardingTableRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rule")
    private final List<ShowShardingTableRulesStatementTestCase> showShardingTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-migration-source-storage-units")
    private final List<ShowMigrationSourceStorageUnitsStatementTestCase> showMigrationSourceStorageUnitsStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-migration-list")
    private final List<ShowMigrationListStatementTestCase> showMigrationListTestCases = new LinkedList<>();
    
    @XmlElement(name = "check-migration")
    private final List<CheckMigrationStatementTestCase> checkMigrationTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-migration-status")
    private final List<ShowMigrationStatusStatementTestCase> showMigrationStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-migration-check-status")
    private final List<ShowMigrationCheckStatusStatementTestCase> showMigrationCheckStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-migration-check-algorithms")
    private final List<ShowMigrationCheckAlgorithmsStatementTestCase> showMigrationCheckAlgorithmsStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "commit-migration")
    private final List<CommitMigrationStatementTestCase> commitMigrationTestCases = new LinkedList<>();
    
    @XmlElement(name = "rollback-migration")
    private final List<RollbackMigrationStatementTestCase> rollbackMigrationTestCases = new LinkedList<>();
    
    @XmlElement(name = "stop-migration")
    private final List<StopMigrationStatementTestCase> stopMigrationTestCases = new LinkedList<>();
    
    @XmlElement(name = "start-migration")
    private final List<StartMigrationStatementTestCase> startMigrationTestCases = new LinkedList<>();
    
    @XmlElement(name = "stop-migration-check")
    private final List<StopMigrationCheckStatementTestCase> stopMigrationCheckTestCases = new LinkedList<>();
    
    @XmlElement(name = "start-migration-check")
    private final List<StartMigrationCheckStatementTestCase> startMigrationCheckTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-migration-check")
    private final List<DropMigrationCheckStatementTestCase> dropMigrationCheckTestCases = new LinkedList<>();
    
    @XmlElement(name = "migrate-table")
    private final List<MigrateTableStatementTestCase> migrateTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-streaming-rule")
    private final List<ShowStreamingRuleStatementTestCase> showStreamingRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-streaming-rule")
    private final List<AlterTransmissionRuleStatementTestCase> alterStreamingRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-streaming-list")
    private final List<ShowStreamingListStatementTestCase> showStreamingListTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-streaming-status")
    private final List<ShowStreamingStatusStatementTestCase> showStreamingStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-streaming")
    private final List<DropStreamingStatementTestCase> dropStreamingTestCases = new LinkedList<>();
    
    @XmlElement(name = "preview-sql")
    private final List<PreviewStatementTestCase> previewTestCases = new LinkedList<>();
    
    @XmlElement(name = "parse-sql")
    private final List<ParseStatementTestCase> parseStatementAsserts = new LinkedList<>();
    
    @XmlElement(name = "show-dist-variable")
    private final List<ShowDistVariableStatementTestCase> showVariableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-dist-variables")
    private final List<ShowDistVariablesStatementTestCase> showDistVariableTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-dist-variable")
    private final List<SetDistVariableStatementTestCase> setDistVariableTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-shadow-rule")
    private final List<CreateShadowRuleStatementTestCase> createShadowRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-shadow-rule")
    private final List<DropShadowRuleStatementTestCase> dropShadowRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-shadow-rule")
    private final List<AlterShadowRuleStatementTestCase> alterShadowRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-shadow-rules")
    private final List<ShowShadowRulesStatementTestCase> showShadowRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-shadow-algorithms")
    private final List<ShowShadowAlgorithmsStatementTestCase> showShadowAlgorithmsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-default-shadow-algorithm")
    private final List<ShowDefaultShadowAlgorithmsStatementTestCase> showDefaultShadowAlgorithmsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-shadow-table-rules")
    private final List<ShowShadowTableRulesStatementTestCase> showShadowTableRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-shadow-algorithm")
    private final List<DropShadowAlgorithmStatementTestCase> dropShadowAlgorithmTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-service")
    private final List<SQLServerCreateServiceStatementTestCase> createServiceTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-service")
    private final List<SQLServerAlterServiceStatementTestCase> alterServiceTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-service")
    private final List<SQLServerDropServiceStatementTestCase> dropServiceTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-schema")
    private final List<CreateSchemaStatementTestCase> createSchemaTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-schema")
    private final List<AlterSchemaStatementTestCase> alterSchemaTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-schema")
    private final List<DropSchemaStatementTestCase> dropSchemaTestCases = new LinkedList<>();
    
    @XmlElement(name = "install-component")
    private final List<MySQLInstallComponentStatementTestCase> installComponentTestCases = new LinkedList<>();
    
    @XmlElement(name = "flush")
    private final List<MySQLFlushStatementTestCase> flushTestCases = new LinkedList<>();
    
    @XmlElement(name = "install-plugin")
    private final List<MySQLInstallPluginStatementTestCase> installPluginTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-compute-nodes")
    private final List<ShowComputeNodesStatementTestCase> showInstanceTestCases = new LinkedList<>();
    
    @XmlElement(name = "clone")
    private final List<MySQLCloneStatementTestCase> cloneTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-status-from-readwrite-splitting-rules")
    private final List<ShowStatusFromReadwriteSplittingRulesStatementTestCase> showStatusFromReadwriteSplittingRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "uninstall-component")
    private final List<MySQLUninstallComponentStatementTestCase> uninstallComponentTestCases = new LinkedList<>();
    
    @XmlElement(name = "uninstall-plugin")
    private final List<MySQLUninstallPluginStatementTestCase> uninstallPluginTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-storage-units")
    private final List<ShowStorageUnitsStatementTestCase> showStorageUnitsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-single-tables")
    private final List<ShowTableStatementTestCase> showSingleTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-logical-tables")
    private final List<ShowTableStatementTestCase> showLogicalTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-default-single-table-storage-unit")
    private final List<ShowDefaultSingleTableStorageUnitStatementTestCase> showDefaultSingleTableStorageUnitTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-nodes")
    private final List<ShowShardingTableNodesStatementTestCase> showShardingTableNodesTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-resource-group")
    private final List<MySQLSetResourceGroupStatementTestCase> setResourceGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "optimize-table")
    private final List<MySQLOptimizeTableStatementTestCase> optimizeTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "repair-table")
    private final List<MySQLRepairTableStatementTestCase> repairTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-auditor")
    private final List<CreateShardingAuditorStatementTestCase> createShardingAuditorTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-default-sharding-strategy")
    private final List<CreateDefaultShardingStrategyStatementTestCase> createDefaultShardingStrategyTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-default-sharding-strategy")
    private final List<AlterDefaultShardingStrategyStatementTestCase> alterDefaultShardingStrategyTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-default-shadow-algorithm")
    private final List<CreateDefaultShadowAlgorithmStatementTestCase> createDefaultShadowAlgorithmTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-default-shadow-algorithm")
    private final List<AlterDefaultShadowAlgorithmStatementTestCase> alterDefaultShadowAlgorithmTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-replicas")
    private final List<MySQLShowReplicasStatementTestCase> showReplicasTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-replica-status")
    private final List<MySQLShowReplicaStatusStatementTestCase> showReplicaStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-slave-status")
    private final List<MySQLShowSlaveStatusStatementTestCase> showSlaveStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-auditor")
    private final List<AlterShardingAuditorStatementTestCase> alterShardingAuditorTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-key-generator")
    private final List<DropShardingKeyGeneratorStatementTestCase> dropShardingKeyGeneratorTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-auditor")
    private final List<DropShardingAuditorStatementTestCase> dropShardingAuditorTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-default-sharding-strategy")
    private final List<DropDefaultShardingStrategyStatementTestCase> dropDefaultShardingStrategyTestCases = new LinkedList<>();
    
    @XmlElement(name = "reset")
    private final List<MySQLResetStatementTestCase> resetTestCases = new LinkedList<>();
    
    @XmlElement(name = "reset-persist")
    private final List<MySQLResetPersistStatementTestCase> resetPersistTestCases = new LinkedList<>();
    
    @XmlElement(name = "reset-parameter")
    private final List<PostgreSQLResetParameterStatementTestCase> resetParameterTestCases = new LinkedList<>();
    
    @XmlElement(name = "cache-index")
    private final List<MySQLCacheIndexStatementTestCase> cacheIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "load-index")
    private final List<MySQLLoadIndexInfoStatementTestCase> loadIndexInfoTestCases = new LinkedList<>();
    
    @XmlElement(name = "kill")
    private final List<MySQLKillStatementTestCase> killTestCases = new LinkedList<>();
    
    @XmlElement(name = "restart")
    private final List<MySQLRestartStatementTestCase> restartTestCases = new LinkedList<>();
    
    @XmlElement(name = "shutdown")
    private final List<MySQLShutdownStatementTestCase> shutdownTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-default-single-table-storage-unit")
    private final List<SetDefaultSingleTableStorageUnitStatementTestCase> setDefaultSingleTableStorageUnitTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-default-single-table-storage-unit-random")
    private final List<SetDefaultSingleTableStorageUnitStatementTestCase> setDefaultSingleTableStorageUnitToRandomTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-open-tables")
    private final List<MySQLShowOpenTablesStatementTestCase> showOpenTablesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-query-stats")
    private final List<DorisShowQueryStatsStatementTestCase> showQueryStatsTestCases = new LinkedList<>();
    
    @XmlElement(name = "check-table")
    private final List<MySQLCheckTableStatementTestCase> checkTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "checksum-table")
    private final List<MySQLChecksumTableStatementTestCase> checksumTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-status")
    private final List<MySQLShowStatusStatementTestCase> showStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "refresh-table-metadata")
    private final List<RefreshTableMetaDataStatementTestCase> refreshTableMetaDataTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-events")
    private final List<MySQLShowEventsStatementTestCase> showEventsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-character-set")
    private final List<MySQLShowCharacterSetStatementTestCase> showCharacterSetTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-collation")
    private final List<MySQLShowCollationStatementTestCase> showCollationTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-variables")
    private final List<MySQLShowVariablesStatementTestCase> showVariablesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-key-generators")
    private final List<ShowShardingKeyGeneratorsStatementTestCase> showShardingKeyGeneratorsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-default-sharding-strategy")
    private final List<ShowDefaultShardingStrategyStatementTestCase> showDefaultShardingStrategyTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-mask-rules")
    private final List<ShowMaskRulesStatementTestCase> showMaskRulesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "delimiter")
    private final List<MySQLDelimiterStatementTestCase> delimiterTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sql-parser-rule")
    private final List<ShowSQLParserRuleStatementTestCase> showSQLParserRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-authority-rule")
    private final List<ShowAuthorityRuleStatementTestCase> showAuthorityRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-transaction-rule")
    private final List<ShowTransactionRuleStatementTestCase> showTransactionRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-global-clock-rule")
    private final List<AlterGlobalClockRuleStatementTestCase> alterGlobalClockRulesTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sql-parser-rule")
    private final List<AlterSQLParserRuleStatementTestCase> alterSQLParserRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sql-translator-rule")
    private final List<AlterSQLTranslatorRuleStatementTestCase> alterSQLTranslatorRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-local-transaction-rule")
    private final List<AlterLocalTransactionRuleStatementTestCase> alterLocalTransactionRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-xa-transaction-rule")
    private final List<AlterXATransactionRuleStatementTestCase> alterXATransactionRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "label-compute-node")
    private final List<LabelComputeNodeStatementTestCase> labelTestCases = new LinkedList<>();
    
    @XmlElement(name = "unlabel-compute-node")
    private final List<UnlabelComputeNodeStatementTestCase> unlabelTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-compute-node")
    private final List<AlterComputeNodeStatementTestCase> alterInstanceTestCases = new LinkedList<>();
    
    @XmlElement(name = "prepare-distsql")
    private final List<PrepareDistSQLStatementTestCase> prepareDistSQLTestCases = new LinkedList<>();
    
    @XmlElement(name = "apply-distsql")
    private final List<ApplyDistSQLStatementTestCase> applyDistSQLTestCases = new LinkedList<>();
    
    @XmlElement(name = "discard-distsql")
    private final List<DiscardDistSQLStatementTestCase> discardDistSQLTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-table-metadata")
    private final List<ShowTableMetaDataStatementTestCase> showTableMetaDataTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-conversion")
    private final List<PostgreSQLCreateConversionStatementTestCase> createConversionTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-cast")
    private final List<PostgreSQLCreateCastStatementTestCase> createCastTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-conversion")
    private final List<PostgreSQLDropConversionStatementTestCase> dropConversionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-domain")
    private final List<AlterDomainStatementTestCase> alterDomainTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-aggregate")
    private final List<PostgreSQLAlterAggregateStatementTestCase> alterAggregateTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-conversion")
    private final List<PostgreSQLAlterConversionStatementTestCase> alterConversionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-collation")
    private final List<PostgreSQLAlterCollationStatementTestCase> alterCollationTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-default-privileges")
    private final List<PostgreSQLAlterDefaultPrivilegesTestCase> alterDefaultPrivilegesTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-foreign-data-wrapper")
    private final List<PostgreSQLAlterForeignDataWrapperTestCase> alterForeignDataWrapperTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-foreign-table")
    private final List<PostgreSQLAlterForeignTableStatementTestCase> alterForeignTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-foreign-table")
    private final List<PostgreSQLDropForeignTableStatementTestCase> dropForeignTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-group")
    private final List<PostgreSQLAlterGroupStatementTestCase> alterGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-materialized-view")
    private final List<OracleAlterMaterializedViewStatementTestCase> alterMaterializedViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-materialized-view-log")
    private final List<OracleAlterMaterializedViewLogStatementTestCase> alterMaterializedViewLogTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-pluggable-database")
    private final List<OracleAlterPluggableDatabaseStatementTestCase> alterPluggableDatabaseTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-java")
    private final List<OracleAlterJavaStatementTestCase> alterJavaTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-audit-policy")
    private final List<OracleAlterAuditPolicyStatementTestCase> alterAuditPolicyTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-cluster")
    private final List<OracleAlterClusterStatementTestCase> alterClusterTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-diskgroup")
    private final List<OracleAlterDiskgroupStatementTestCase> alterDiskgroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-hierarchy")
    private final List<OracleAlterHierarchyStatementTestCase> alterHierarchyTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-index-type")
    private final List<OracleAlterIndexTypeStatementTestCase> alterIndexTypeTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-lockdown-profile")
    private final List<OracleAlterLockdownProfileStatementTestCase> alterLockdownProfileTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-operator")
    private final List<AlterOperatorStatementTestCase> alterOperatorTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-profile")
    private final List<OracleAlterProfileStatementTestCase> alterProfileTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-rollback-segment")
    private final List<OracleAlterRollbackSegmentStatementTestCase> alterRollbackSegmentTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-text-search")
    private final List<PostgreSQLCreateTextSearchStatementTestCase> createTextSearchTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-text-search")
    private final List<PostgreSQLAlterTextSearchStatementTestCase> alterTextSearchTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-language")
    private final List<PostgreSQLCreateLanguageStatementTestCase> createLanguageTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-language")
    private final List<PostgreSQLAlterLanguageStatementTestCase> alterLanguageTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-language")
    private final List<PostgreSQLDropLanguageStatementTestCase> dropLanguageTestCases = new LinkedList<>();
    
    @XmlElement(name = "help")
    private final List<MySQLHelpStatementTestCase> helpTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-unused-sharding-algorithms")
    private final List<ShowUnusedShardingAlgorithmsStatementTestCase> showUnusedShardingAlgorithmsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-unused-sharding-key-generators")
    private final List<ShowUnusedShardingKeyGeneratorsStatementTestCase> showUnusedShardingKeyGeneratorsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-unused-sharding-auditors")
    private final List<ShowUnusedShardingAuditorsStatementTestCase> showUnusedShardingAuditorsStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-binlog-events")
    private final List<MySQLShowBinlogEventsStatementTestCase> showBinlogEventsTestCases = new LinkedList<>();
    
    @XmlElement(name = "listen")
    private final List<PostgreSQLListenStatementTestCase> listenTestCases = new LinkedList<>();
    
    @XmlElement(name = "notify")
    private final List<PostgreSQLNotifyStmtStatementTestCase> notifyTestCases = new LinkedList<>();
    
    @XmlElement(name = "refresh-materialized-view")
    private final List<RefreshMatViewStmtStatementTestCase> refreshMatViewStmtStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "reindex")
    private final List<PostgreSQLReindexStatementTestCase> reindexStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "unlisten")
    private final List<PostgreSQLUnlistenStatementTestCase> unlistenTestCases = new LinkedList<>();
    
    @XmlElement(name = "security-label")
    private final List<PostgreSQLSecurityLabelStmtStatementTestCase> securityLabelStmtStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "lock")
    private final List<LockStatementTestCase> lockTestCases = new LinkedList<>();
    
    @XmlElement(name = "unlock")
    private final List<UnlockStatementTestCase> unlockTestCases = new LinkedList<>();
    
    @XmlElement(name = "export-database-config")
    private final List<ExportDatabaseConfigurationStatementTestCase> exportDatabaseConfigurationTestCases = new LinkedList<>();
    
    @XmlElement(name = "export-metadata")
    private final List<ExportMetaDataStatementTestCase> exportMetaDataStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "export-metadata-to-file")
    private final List<ExportMetaDataToFileStatementTestCase> exportMetaDataToFileStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "export-storage-nodes")
    private final List<ExportStorageNodesStatementTestCase> exportStorageNodesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "convert-yaml-config")
    private final List<ConvertYamlConfigurationStatementTestCase> convertYamlConfigurationStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-rules-used-storage-unit")
    private final List<ShowRulesUsedStorageUnitStatementTestCase> showRulesUsedResourceTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rules-used-algorithm")
    private final List<ShowShardingTableRulesUsedAlgorithmStatementTestCase> showShardingTableRulesUsedAlgorithmTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rules-used-key-generator")
    private final List<ShowShardingTableRulesUsedKeyGeneratorStatementTestCase> showShardingTableRulesUsedKeyGeneratorTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rules-used-auditor")
    private final List<ShowShardingTableRulesUsedAuditorStatementTestCase> showShardingTableRulesUsedAuditorTestCases = new LinkedList<>();
    
    @XmlElement(name = "prepared")
    private final List<PreparedStatementTestCase> preparedTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-user")
    private final List<SQLServerSetUserStatementTestCase> setUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-algorithm")
    private final List<DropShardingAlgorithmStatementTestCase> dropShardingAlgorithmTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-owned")
    private final List<PostgreSQLDropOwnedStatementTestCase> dropOwnedTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-tablespace")
    private final List<CreateTablespaceStatementTestCase> createTablespaceTestCases = new LinkedList<>();
    
    @XmlElement(name = "import-database-config")
    private final List<ImportDatabaseConfigurationStatementTestCase> importDatabaseConfigurationTestCases = new LinkedList<>();
    
    @XmlElement(name = "import-metadata")
    private final List<ImportMetaDataStatementTestCase> importMetaDataTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-operator")
    private final List<DropOperatorStatementTestCase> dropOperatorTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-materialized-view")
    private final List<DropMaterializedViewStatementTestCase> dropMaterializedViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-cast")
    private final List<PostgreSQLDropCastStatementTestCase> dropCastTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-aggregate")
    private final List<PostgreSQLDropAggregateStatementTestCase> dropAggregateTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-collation")
    private final List<PostgreSQLDropCollationStatementTestCase> dropCollationTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-foreign-data-wrapper")
    private final List<PostgreSQLDropForeignDataWrapperStatementTestCase> dropForeignDataWrapperTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-type")
    private final List<PostgreSQLDropTypeStatementTestCase> dropTypeTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-operator-class")
    private final List<PostgreSQLDropOperatorClassStatementTestCase> dropOperatorClassTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-operator-family")
    private final List<PostgreSQLDropOperatorFamilyStatementTestCase> dropOperatorFamilyTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-access-method")
    private final List<PostgreSQLDropAccessMethodStatementTestCase> dropAccessMethodTestCases = new LinkedList<>();
    
    @XmlElement(name = "revert")
    private final List<RevertStatementTestCase> revertTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-analytic-view")
    private final List<OracleAlterAnalyticViewStatementTestCase> alterAnalyticViewTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-attribute-dimension")
    private final List<OracleAlterAttributeDimensionStatementTestCase> alterAttributeDimensionTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-context")
    private final List<OracleCreateContextStatementTestCase> createContextTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-spfile")
    private final List<OracleCreateSPFileStatementTestCase> createSPFileTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-pfile")
    private final List<OracleCreatePFileStatementTestCase> createPFileTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-control-file")
    private final List<OracleCreateControlFileStatementTestCase> createControlFileTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-flashback-archive")
    private final List<OracleCreateFlashbackArchiveStatementTestCase> createFlashbackArchiveTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-flashback-archive")
    private final List<OracleAlterFlashbackArchiveStatementTestCase> alterFlashbackArchiveTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-flashback-archive")
    private final List<OracleDropFlashbackArchiveStatementTestCase> dropFlashbackArchiveTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-diskgroup")
    private final List<OracleCreateDiskgroupStatementTestCase> createDiskgroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-diskgroup")
    private final List<OracleDropDiskgroupStatementTestCase> dropDiskgroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-rollback-segment")
    private final List<OracleCreateRollbackSegmentStatementTestCase> createRollbackSegmentTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-rollback-segment")
    private final List<OracleDropRollbackSegmentStatementTestCase> dropRollbackSegmentTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-lockdown-profile")
    private final List<OracleCreateLockdownProfileStatementTestCase> createLockdownProfileTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-lockdown-profile")
    private final List<OracleDropLockdownProfileStatementTestCase> dropLockdownProfileTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-inmemory-join-group")
    private final List<OracleCreateInmemoryJoinGroupStatementTestCase> createInmemoryJoinGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-inmemory-join-group")
    private final List<OracleAlterInmemoryJoinGroupStatementTestCase> alterInmemoryJoinGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-inmemory-join-group")
    private final List<OracleDropInmemoryJoinGroupStatementTestCase> dropInmemoryJoinGroupTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-restore-point")
    private final List<OracleCreateRestorePointStatementTestCase> createRestorePointTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-restore-point")
    private final List<OracleDropRestorePointStatementTestCase> dropRestorePointTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-library")
    private final List<OracleAlterLibraryStatementTestCase> alterLibraryTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-materialized-zonemap")
    private final List<OracleAlterMaterializedZonemapStatementTestCase> alterMaterializedZonemapTestCases = new LinkedList<>();
    
    @XmlElement(name = "cursor")
    private final List<CursorStatementTestCase> cursorTestCases = new LinkedList<>();
    
    @XmlElement(name = "close")
    private final List<CloseStatementTestCase> closeTestCases = new LinkedList<>();
    
    @XmlElement(name = "move")
    private final List<MoveStatementTestCase> moveTestCases = new LinkedList<>();
    
    @XmlElement(name = "execute")
    private final List<ExecuteStatementTestCase> executeTestCases = new LinkedList<>();
    
    @XmlElement(name = "fetch")
    private final List<FetchStatementTestCase> fetchTestCases = new LinkedList<>();
    
    @XmlElement(name = "checkpoint")
    private final List<PostgreSQLCheckpointStatementTestCase> checkpointTestCases = new LinkedList<>();
    
    @XmlElement(name = "cluster")
    private final List<PostgreSQLClusterStatementTestCase> clusterStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-access-method")
    private final List<PostgreSQLCreateAccessMethodStatementTestCase> createAccessMethodTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-single-table")
    private final List<CountSingleTableStatementTestCase> countSingleTableStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-sharding-rule")
    private final List<CountShardingRuleStatementTestCase> countShardingRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-readwrite-splitting-rule")
    private final List<CountReadwriteSplittingRuleStatementTestCase> countReadwriteSplittingRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-encrypt-rule")
    private final List<CountEncryptRuleStatementTestCase> countEncryptRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-shadow-rule")
    private final List<CountShadowRuleStatementTestCase> countShadowRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-mask-rule")
    private final List<CountMaskRuleStatementTestCase> countMaskRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "count-broadcast-rule")
    private final List<CountBroadcastRuleStatementTestCase> countBroadcastRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-collation")
    private final List<CreateCollationStatementTestCase> createCollationStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "empty")
    private final List<EmptyStatementTestCase> emptyStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "prepare-transaction")
    private final List<PrepareTransactionTestCase> prepareTransactionTestCases = new LinkedList<>();
    
    @XmlElement(name = "reassign-owned")
    private final List<PostgreSQLReassignOwnedStatementTestCase> reassignOwnedStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-event-trigger")
    private final List<PostgreSQLCreateEventTriggerStatementTestCase> createEventTriggerStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-foreign-data-wrapper")
    private final List<PostgreSQLCreateForeignDataWrapperStatementTestCase> createForeignDataWrapperStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-sql-translator-rule")
    private final List<ShowSQLTranslatorRuleStatementTestCase> showSQLTranslatorRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-global-clock-rule")
    private final List<ShowGlobalClockRuleStatementTestCase> showGlobalClockRuleStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-foreign-table")
    private final List<PostgreSQLCreateForeignTableStatementTestCase> createForeignTableStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-compute-node-info")
    private final List<ShowComputeNodeInfoStatementTestCase> showComputeNodeInfoStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-compute-node-mode")
    private final List<ShowComputeNodeModeStatementTestCase> showComputeNodeModeStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-group")
    private final List<CreateGroupStatementTestCase> createGroupStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-materialized-view")
    private final List<CreateMaterializedViewStatementTestCase> createMaterializedViewStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-materialized-view-log")
    private final List<OracleCreateMaterializedViewLogStatementTestCase> createMaterializedViewLogStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-operator")
    private final List<CreateOperatorStatementTestCase> createOperatorStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "unregister-migration-source-storage-unit")
    private final List<UnregisterMigrationSourceStorageUnitStatementTestCase> unregisterMigrationSourceStorageUnitStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-policy")
    private final List<PostgreSQLCreatePolicyStatementTestCase> createPolicyStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-index-type")
    private final List<OracleDropIndexTypeStatementTestCase> dropIndexTypeStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-pluggable-database")
    private final List<OracleDropPluggableDatabaseStatementTestCase> dropPluggableDatabaseStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-java")
    private final List<OracleDropJavaStatementTestCase> dropJavaStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-library")
    private final List<OracleDropLibraryStatementTestCase> dropLibraryStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-materialized-view-log")
    private final List<OracleDropMaterializedViewLogStatementTestCase> dropMaterializedViewLogTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-materialized-zonemap")
    private final List<OracleDropMaterializedZonemapStatementTestCase> dropMaterializedZonemapTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-cluster")
    private final List<OracleDropClusterStatementTestCase> dropClusterStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-context")
    private final List<OracleDropContextStatementTestCase> dropContextStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-readwrite-splitting-storage-unit-status-enable")
    private final List<AlterReadwriteSplittingStorageUnitStatusStatementTestCase> alterReadwriteSplittingStorageUnitStatusStatementEnableTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-readwrite-splitting-storage-unit-status-disable")
    private final List<AlterReadwriteSplittingStorageUnitStatusStatementTestCase> alterReadwriteSplittingStorageUnitStatusStatementDisableTestCases = new LinkedList<>();
    
    @XmlElement(name = "change-replication-source-to")
    private final List<ChangeReplicationSourceToStatementTestCase> changeReplicationSourceToStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-java")
    private final List<OracleCreateJavaStatementTestCase> createJavaStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-library")
    private final List<OracleCreateLibraryStatementTestCase> createLibraryStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "switch")
    private final List<OracleSwitchStatementTestCase> switchStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-profile")
    private final List<OracleCreateProfileStatementTestCase> createProfileStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "update-statistics")
    private final List<SQLServerUpdateStatisticsStatementTestCase> updateStatisticsStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "spool")
    private final List<OracleSpoolStatementTestCase> oracleSpoolStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-loadable-function")
    private final List<MySQLCreateLoadableFunctionTestCase> createLoadableFunctionTestCases = new LinkedList<>();
    
    @XmlElement(name = "start-replica")
    private final List<StartReplicaStatementTestCase> startReplicaStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-outline")
    private final List<OracleCreateOutlineStatementTestCase> createOutlineStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "open")
    private final List<PostgreSQLOpenStatementTestCase> openTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-macro")
    private final List<CreateMacroStatementTestCase> createMacroTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-macro")
    private final List<DropMacroStatementTestCase> dropMacroTestCases = new LinkedList<>();
    
    @XmlElement(name = "reload-function")
    private final List<ReloadFunctionStatementTestCase> reloadFunctionStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "abort")
    private final List<HiveAbortStatementTestCase> hiveAbortStatementTestCase = new LinkedList<>();
    
    /**
     * Get all SQL parser test cases.
     *
     * @return got test cases
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(IllegalAccessException.class)
    public Map<String, SQLParserTestCase> getAllCases() {
        Map<String, SQLParserTestCase> result = new HashMap<>();
        for (Field each : RootSQLParserTestCases.class.getDeclaredFields()) {
            if (!isXmlElementField(each)) {
                continue;
            }
            List<? extends SQLParserTestCase> cases = (List<? extends SQLParserTestCase>) Plugins.getMemberAccessor().get(each, this);
            if (cases.isEmpty()) {
                continue;
            }
            Map<String, SQLParserTestCase> caseMap = getTestCaseMap(cases);
            checkDuplicatedTestCases(caseMap, result);
            result.putAll(caseMap);
        }
        return result;
    }
    
    private boolean isXmlElementField(final Field field) {
        return field.isAnnotationPresent(XmlElement.class) && List.class == field.getType() && field.getGenericType() instanceof ParameterizedType
                && SQLParserTestCase.class.isAssignableFrom((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
    }
    
    private Map<String, SQLParserTestCase> getTestCaseMap(final List<? extends SQLParserTestCase> cases) {
        Map<String, SQLParserTestCase> result = new HashMap<>(cases.size(), 1F);
        for (SQLParserTestCase each : cases) {
            checkDuplicatedTestCase(each, result);
            result.put(each.getSqlCaseId(), each);
        }
        return result;
    }
    
    private void checkDuplicatedTestCase(final SQLParserTestCase newTestCase, final Map<String, SQLParserTestCase> existedTestCases) {
        Preconditions.checkState(!existedTestCases.containsKey(newTestCase.getSqlCaseId()), "Find duplicated SQL Case ID: %s.", newTestCase.getSqlCaseId());
    }
    
    private void checkDuplicatedTestCases(final Map<String, SQLParserTestCase> newTestCases, final Map<String, SQLParserTestCase> existedTestCases) {
        Collection<String> caseIds = new HashSet<>(newTestCases.keySet());
        caseIds.retainAll(existedTestCases.keySet());
        Preconditions.checkState(caseIds.isEmpty(), "Find duplicated SQL Case IDs: %s.", caseIds);
    }
}
