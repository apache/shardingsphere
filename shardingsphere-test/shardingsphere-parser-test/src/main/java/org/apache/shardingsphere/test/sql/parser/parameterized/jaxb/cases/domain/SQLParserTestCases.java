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

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.CommonStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.BinlogStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.CacheIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.CloneStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.CreateResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ExplainStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.FlushStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.InstallComponentStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.InstallPluginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.KillStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.OptimizeTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.RepairTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ResetPersistStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ResetStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.SetParameterStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.SetResourceGroupStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowColumnsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowCreateTriggerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowCreateUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowFunctionStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowProcedureCodeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowProcedureStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowRelaylogEventsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowReplicasStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.UninstallComponentStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.UninstallPluginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.UseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.AlterLoginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.AlterRoleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.AlterUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.CreateLoginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.CreateRoleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.CreateUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.DenyUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.DropLoginStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.DropRoleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.DropUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.GrantStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.RenameUserStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.RevokeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.SetDefaultRoleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.SetPasswordStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dcl.SetRoleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterDatabaseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterFunctionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterProcedureStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSchemaStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSequenceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterServerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterServiceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSessionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSynonymStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterSystemStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AnalyzeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AssociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.AuditStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CommentStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateDatabaseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateFunctionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateProcedureStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateSchemaStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateSequenceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateServerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateServiceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateTriggerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CreateViewStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DisassociateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropDatabaseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropFunctionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropIndexStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropProcedureStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropSchemaStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropSequenceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropServerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropServiceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropTriggerStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.DropViewStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.FlashbackDatabaseStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.FlashbackTableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.NoAuditStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.PurgeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.RenameStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.TruncateStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AddShardingHintDatabaseValueStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AddShardingHintTableValueStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ClearHintStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ClearReadwriteSplittingHintStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ClearShardingHintStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.PreviewStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.SetReadwriteSplittingHintStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.SetShardingHintDatabaseValueStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.SetVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowAllVariablesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowInstanceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowReadwriteSplittingHintStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowReadwriteSplittingReadResourcesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowScalingListStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowShardingHintStatusStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.ShowVariableStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.CheckScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.CheckoutScalingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.ShowScalingCheckAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.scaling.StopScalingSourceWritingStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterDataBaseDiscoveryRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterResourceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingAutoTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.AddResourceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.AlterShardingAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDataBaseDiscoveryRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDefaultShardingStrategyStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingAutoTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropDataBaseDiscoveryRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropEncryptRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropReadwriteSplittingRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropResourceStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropShadowAlgorithmStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropShadowRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.drop.DropShardingTableRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowDataBaseDiscoveryRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowEncryptRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowReadwriteSplittingRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShadowAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShadowRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShadowTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingAlgorithmsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingBindingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingBroadcastTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowSingleTableRulesStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.CallStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.DeleteStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.InsertStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.MergeStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.SelectStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.UpdateStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.BeginTransactionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.CommitStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.RollbackStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.SavepointStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.SetConstraintsStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.SetTransactionStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.tcl.XATestCase;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL parser test cases.
 */
@XmlRootElement(name = "sql-parser-test-cases")
@Getter
public final class SQLParserTestCases {
    
    @XmlElement(name = "select")
    private final List<SelectStatementTestCase> selectTestCases = new LinkedList<>();
    
    @XmlElement(name = "update")
    private final List<UpdateStatementTestCase> updateTestCases = new LinkedList<>();
    
    @XmlElement(name = "delete")
    private final List<DeleteStatementTestCase> deleteTestCases = new LinkedList<>();
    
    @XmlElement(name = "insert")
    private final List<InsertStatementTestCase> insertTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-table")
    private final List<CreateTableStatementTestCase> createTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-table")
    private final List<AlterTableStatementTestCase> alterTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-table")
    private final List<DropTableStatementTestCase> dropTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "truncate")
    private final List<TruncateStatementTestCase> truncateTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-index")
    private final List<CreateIndexStatementTestCase> createIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-index")
    private final List<AlterIndexStatementTestCase> alterIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-index")
    private final List<DropIndexStatementTestCase> dropIndexTestCases = new LinkedList<>();
    
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
    private final List<RenameUserStatementTestCase> renameUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "deny-user")
    private final List<DenyUserStatementTestCase> denyUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-login")
    private final List<CreateLoginStatementTestCase> createLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-login")
    private final List<AlterLoginStatementTestCase> alterLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-login")
    private final List<DropLoginStatementTestCase> dropLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-role")
    private final List<CreateRoleStatementTestCase> createRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-role")
    private final List<AlterRoleStatementTestCase> alterRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-role")
    private final List<DropRoleStatementTestCase> dropRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-default-role")
    private final List<SetDefaultRoleStatementTestCase> setDefaultRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-role")
    private final List<SetRoleStatementTestCase> setRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-password")
    private final List<SetPasswordStatementTestCase> setPasswordTestCases = new LinkedList<>();
    
    @XmlElement(name = "use")
    private final List<UseStatementTestCase> useTestCases = new LinkedList<>();
    
    @XmlElement(name = "describe")
    private final List<ExplainStatementTestCase> describeTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-databases")
    private final List<ShowDatabasesStatementTestCase> showDatabasesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-tables")
    private final List<ShowTablesStatementTestCase> showTablesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-function-status")
    private final List<ShowFunctionStatusStatementTestCase> showFunctionStatusStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-procedure-status")
    private final List<ShowProcedureStatusStatementTestCase> showProcedureStatusStatementTestCases = new LinkedList<>();

    @XmlElement(name = "show-procedure-code")
    private final List<ShowProcedureCodeStatementTestCase> showProcedureCodeStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-relaylog-events")
    private final List<ShowRelaylogEventsStatementTestCase> showRelaylogEventsStatementTestCases = new LinkedList<>();

    @XmlElement(name = "show-columns")
    private final List<ShowColumnsStatementTestCase> showColumnsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-table")
    private final List<ShowCreateTableStatementTestCase> showCreateTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-trigger")
    private final List<ShowCreateTriggerStatementTestCase> showCreateTriggerTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-resource-group")
    private final List<CreateResourceGroupStatementTestCase> createResourceGroupStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "binlog")
    private final List<BinlogStatementTestCase> binlogStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-user")
    private final List<ShowCreateUserStatementTestCase> showCreateUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-table-status")
    private final List<ShowTableStatusStatementTestCase> showTableStatusTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-index")
    private final List<ShowIndexStatementTestCase> showIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "show")
    private final List<ShowStatementTestCase> showTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-parameter")
    private final List<SetParameterStatementTestCase> setVariableTestCases = new LinkedList<>();
    
    @XmlElement(name = "common")
    private final List<CommonStatementTestCase> commonTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-function")
    private final List<AlterFunctionStatementTestCase> alterFunctionTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-database")
    private final List<AlterDatabaseStatementTestCase> alterDatabaseTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-procedure")
    private final List<AlterProcedureStatementTestCase> alterProcedureTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-server")
    private final List<AlterServerStatementTestCase> alterServerTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-session")
    private final List<AlterSessionStatementTestCase> alterSessionTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-synonym")
    private final List<AlterSynonymStatementTestCase> alterSynonymTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-system")
    private final List<AlterSystemStatementTestCase> alterSystemTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-database")
    private final List<CreateDatabaseStatementTestCase> createDatabaseTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-function")
    private final List<CreateFunctionStatementTestCase> createFunctionTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-procedure")
    private final List<CreateProcedureStatementTestCase> createProcedureTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-server")
    private final List<CreateServerStatementTestCase> createServerTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-trigger")
    private final List<CreateTriggerStatementTestCase> createTriggerTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-view")
    private final List<CreateViewStatementTestCase> createViewTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-database")
    private final List<DropDatabaseStatementTestCase> dropDatabaseTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-function")
    private final List<DropFunctionStatementTestCase> dropFunctionTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-procedure")
    private final List<DropProcedureStatementTestCase> dropProcedureTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-server")
    private final List<DropServerStatementTestCase> dropServerTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-trigger")
    private final List<DropTriggerStatementTestCase> dropTriggerTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-view")
    private final List<DropViewStatementTestCase> dropViewTestCase = new LinkedList<>();
    
    @XmlElement(name = "call")
    private final List<CallStatementTestCase> callProcedureTestCase = new LinkedList<>();
    
    @XmlElement(name = "xa")
    private final List<XATestCase> xaTestCase = new LinkedList<>();
    
    @XmlElement(name = "merge")
    private final List<MergeStatementTestCase> mergeTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-sequence")
    private final List<CreateSequenceStatementTestCase> createSequenceTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-sequence")
    private final List<AlterSequenceStatementTestCase> alterSequenceTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-sequence")
    private final List<DropSequenceStatementTestCase> dropSequenceTestCase = new LinkedList<>();
    
    @XmlElement(name = "analyze")
    private final List<AnalyzeStatementTestCase> analyzeTestCase = new LinkedList<>();
    
    @XmlElement(name = "associate-statistics")
    private final List<AssociateStatisticsStatementTestCase> associateStatisticsTestCase = new LinkedList<>();
    
    @XmlElement(name = "disassociate-statistics")
    private final List<DisassociateStatisticsStatementTestCase> disassociateStatisticsTestCase = new LinkedList<>();
    
    @XmlElement(name = "audit")
    private final List<AuditStatementTestCase> auditTestCase = new LinkedList<>();

    @XmlElement(name = "no-audit")
    private final List<NoAuditStatementTestCase> noAuditTestCase = new LinkedList<>();
    
    @XmlElement(name = "comment")
    private final List<CommentStatementTestCase> commentTestCase = new LinkedList<>();
    
    @XmlElement(name = "flashback-database")
    private final List<FlashbackDatabaseStatementTestCase> flashbackDatabaseTestCase = new LinkedList<>();
    
    @XmlElement(name = "flashback-table")
    private final List<FlashbackTableStatementTestCase> flashbackTableTestCase = new LinkedList<>();
    
    @XmlElement(name = "purge")
    private final List<PurgeStatementTestCase> purgeTestCase = new LinkedList<>();
    
    @XmlElement(name = "rename")
    private final List<RenameStatementTestCase> renameTestCase = new LinkedList<>();
    
    @XmlElement(name = "add-resource")
    private final List<AddResourceStatementTestCase> addResourceTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-resource")
    private final List<AlterResourceStatementTestCase> alterResourceTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-database-discovery-rule")
    private final List<AlterDataBaseDiscoveryRuleStatementTestCase> alterDataBaseDiscoveryRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-encrypt-rule")
    private final List<AlterEncryptRuleStatementTestCase> alterEncryptRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-readwrite-splitting-rule")
    private final List<AlterReadwriteSplittingRuleStatementTestCase> alterReadwriteSplittingRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-binding-table-rules")
    private final List<AlterShardingBindingTableRulesStatementTestCase> alterShardingBindingTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-broadcast-table-rules")
    private final List<AlterShardingBroadcastTableRulesStatementTestCase> alterShardingBroadcastTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-auto-table-rule")
    private final List<AlterShardingAutoTableRuleStatementTestCase> alterShardingTableRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-table-rule")
    private final List<AlterShardingTableRuleStatementTestCase> alterShardingTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-database-discovery-rule")
    private final List<CreateDataBaseDiscoveryRuleStatementTestCase> createDataBaseDiscoveryRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-encrypt-rule")
    private final List<CreateEncryptRuleStatementTestCase> createEncryptRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-readwrite-splitting-rule")
    private final List<CreateReadwriteSplittingRuleStatementTestCase> createReadwriteSplittingRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-binding-table-rule")
    private final List<CreateShardingBindingTableRulesStatementTestCase> createShardingBindingTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-broadcast-table-rule")
    private final List<CreateShardingBroadcastTableRulesStatementTestCase> createShardingBroadcastTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-auto-table-rule")
    private final List<CreateShardingAutoTableRuleStatementTestCase> createShardingTableRuleTestCase = new LinkedList<>();
  
    @XmlElement(name = "create-sharding-table-rule")
    private final List<CreateShardingTableRuleStatementTestCase> createShardingTableRuleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-database-discovery-rule")
    private final List<DropDataBaseDiscoveryRuleStatementTestCase> dropDataBaseDiscoveryRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-encrypt-rule")
    private final List<DropEncryptRuleStatementTestCase> dropEncryptRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-readwrite-splitting-rule")
    private final List<DropReadwriteSplittingRuleStatementTestCase> dropReadwriteSplittingRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-resource")
    private final List<DropResourceStatementTestCase> dropResourceTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-binding-table-rules")
    private final List<DropShardingBindingTableRulesStatementTestCase> dropShardingBindingTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-broadcast-table-rules")
    private final List<DropShardingBroadcastTableRulesStatementTestCase> dropShardingBroadcastTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-sharding-table-rule")
    private final List<DropShardingTableRuleStatementTestCase> dropShardingTableRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-db-discovery-rules")
    private final List<ShowDataBaseDiscoveryRulesStatementTestCase> showDataBaseDiscoveryRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-encrypt-rules")
    private final List<ShowEncryptRulesStatementTestCase> showEncryptRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-readwrite-splitting-rules")
    private final List<ShowReadwriteSplittingRulesStatementTestCase> showReadwriteSplittingRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-binding-table-rules")
    private final List<ShowShardingBindingTableRulesStatementTestCase> showShardingBindingTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-broadcast-table-rules")
    private final List<ShowShardingBroadcastTableRulesStatementTestCase> showShardingBroadcastTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-algorithms")
    private final List<ShowShardingAlgorithmsStatementTestCase> showShardingAlgorithmsTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rules")
    private final List<ShowShardingTableRulesStatementTestCase> showShardingTableRulesTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-table-rule")
    private final List<ShowShardingTableRulesStatementTestCase> showShardingTableRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-scaling-list")
    private final List<ShowScalingListStatementTestCase> showScalingListStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "check-scaling")
    private final List<CheckScalingStatementTestCase> checkScalingStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-scaling-check-algorithms")
    private final List<ShowScalingCheckAlgorithmsStatementTestCase> showScalingCheckAlgorithmsStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "stop-scaling-source-writing")
    private final List<StopScalingSourceWritingStatementTestCase> stopScalingSourceWritingStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "checkout-scaling")
    private final List<CheckoutScalingStatementTestCase> checkoutScalingStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "preview-sql")
    private final List<PreviewStatementTestCase> previewStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-variable")
    private final List<ShowVariableStatementTestCase> showVariableStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-all-variables")
    private final List<ShowAllVariablesStatementTestCase> showAllVariablesStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "set-variable")
    private final List<SetVariableStatementTestCase> setVariableStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "set-readwrite-splitting-hint-source")
    private final List<SetReadwriteSplittingHintStatementTestCase> setReadwriteSplittingHintStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "set-sharding-hint-database-value")
    private final List<SetShardingHintDatabaseValueStatementTestCase> setShardingHintDatabaseValueStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "add-sharding-hint-database-value")
    private final List<AddShardingHintDatabaseValueStatementTestCase> addShardingHintDatabaseValueStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "add-sharding-hint-table-value")
    private final List<AddShardingHintTableValueStatementTestCase> addShardingHintTableValueStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-readwrite-splitting-hint-source")
    private final List<ShowReadwriteSplittingHintStatusStatementTestCase> showReadwriteSplittingHintStatusStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-sharding-hint-status")
    private final List<ShowShardingHintStatusStatementTestCase> showShardingHintStatusStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "clear-readwrite-splitting-hint-source")
    private final List<ClearReadwriteSplittingHintStatementTestCase> clearReadwriteSplittingHintStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "clear-sharding-hint")
    private final List<ClearShardingHintStatementTestCase> clearShardingHintStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "clear-hint")
    private final List<ClearHintStatementTestCase> clearHintStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-shadow-rule")
    private final List<CreateShadowRuleStatementTestCase> createShadowRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-shadow-rule")
    private final List<DropShadowRuleStatementTestCase> dropShadowRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-shadow-rule")
    private final List<AlterShadowRuleStatementTestCase> alterShadowRuleTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-shadow-algorithm")
    private final List<AlterShadowAlgorithmStatementTestCase> alterShadowAlgorithmTestCase = new LinkedList<>();
 
    @XmlElement(name = "create-shadow-algorithm")
    private final List<CreateShadowAlgorithmStatementTestCase> createShadowAlgorithmTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-shadow-rules")
    private final List<ShowShadowRulesStatementTestCase> showShadowRulesStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-shadow-algorithms")
    private final List<ShowShadowAlgorithmsStatementTestCase> showShadowAlgorithmsStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-shadow-table-rules")
    private final List<ShowShadowTableRulesStatementTestCase> showShadowTableRulesStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-shadow-algorithm")
    private final List<DropShadowAlgorithmStatementTestCase> dropShadowAlgorithmStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-service")
    private final List<CreateServiceStatementTestCase> createServiceTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-service")
    private final List<AlterServiceStatementTestCase> alterServiceTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-service")
    private final List<DropServiceStatementTestCase> dropServiceTestCase = new LinkedList<>();
    
    @XmlElement(name = "create-schema")
    private final List<CreateSchemaStatementTestCase> createSchemaTestCase = new LinkedList<>();
    
    @XmlElement(name = "alter-schema")
    private final List<AlterSchemaStatementTestCase> alterSchemaTestCase = new LinkedList<>();
    
    @XmlElement(name = "drop-schema")
    private final List<DropSchemaStatementTestCase> dropSchemaTestCase = new LinkedList<>();
    
    @XmlElement(name = "install-component")
    private final List<InstallComponentStatementTestCase> installComponentTestCase = new LinkedList<>();
    
    @XmlElement(name = "flush")
    private final List<FlushStatementTestCase> flushStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "install-plugin")
    private final List<InstallPluginStatementTestCase> installPluginStatementTestCase = new LinkedList<>();
    
    @XmlElement(name = "show-instance")
    private final List<ShowInstanceStatementTestCase> showInstanceStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "clone")
    private final List<CloneStatementTestCase> cloneStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-readwrite-splitting-read-resources")
    private final List<ShowReadwriteSplittingReadResourcesStatementTestCase> showReadwriteSplittingReadResourcesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "uninstall-component")
    private final List<UninstallComponentStatementTestCase> uninstallComponentStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "uninstall-plugin")
    private final List<UninstallPluginStatementTestCase> uninstallPluginStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-single-table-rules")
    private final List<ShowSingleTableRulesStatementTestCase> showSingleTableRulesStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-resource-group")
    private final List<SetResourceGroupStatementTestCase> setResourceGroupStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "optimize-table")
    private final List<OptimizeTableStatementTestCase> optimizeTableStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "repair-table")
    private final List<RepairTableStatementTestCase> repairTableStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-sharding-algorithm")
    private final List<CreateShardingAlgorithmStatementTestCase> createShardingAlgorithmStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-default-sharding-strategy")
    private final List<CreateDefaultShardingStrategyStatementTestCase> createDefaultShardingStrategyStatementTestCases = new LinkedList<>();

    @XmlElement(name = "show-replicas")
    private final List<ShowReplicasStatementTestCase> showReplicasStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-sharding-algorithm")
    private final List<AlterShardingAlgorithmStatementTestCase> alterShardingAlgorithmStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "reset")
    private final List<ResetStatementTestCase> resetStatementTestCases = new LinkedList<>();

    @XmlElement(name = "reset-persist")
    private final List<ResetPersistStatementTestCase> resetPersistStatementTestCases = new LinkedList<>();
    
    @XmlElement(name = "cache-index")
    private final List<CacheIndexStatementTestCase> cacheIndexStatementTestCases = new LinkedList<>();

    @XmlElement(name = "kill")
    private final List<KillStatementTestCase> killStatementTestCases = new LinkedList<>();
    
    /**
     * Get all SQL parser test cases.
     *
     * @return all SQL parser test cases
     */
    // CHECKSTYLE:OFF
    public Map<String, SQLParserTestCase> getAllSQLParserTestCases() {
        Map<String, SQLParserTestCase> result = new HashMap<>();
        putAll(selectTestCases, result);
        putAll(updateTestCases, result);
        putAll(deleteTestCases, result);
        putAll(insertTestCases, result);
        putAll(createTableTestCases, result);
        putAll(alterTableTestCases, result);
        putAll(dropTableTestCases, result);
        putAll(truncateTestCases, result);
        putAll(createIndexTestCases, result);
        putAll(alterIndexTestCases, result);
        putAll(dropIndexTestCases, result);
        putAll(setConstraintsTestCases, result);
        putAll(setTransactionTestCases, result);
        putAll(beginTransactionTestCases, result);
        putAll(setAutoCommitTestCases, result);
        putAll(commitTestCases, result);
        putAll(rollbackTestCases, result);
        putAll(savepointTestCases, result);
        putAll(grantTestCases, result);
        putAll(revokeTestCases, result);
        putAll(createUserTestCases, result);
        putAll(alterUserTestCases, result);
        putAll(dropUserTestCases, result);
        putAll(renameUserTestCases, result);
        putAll(denyUserTestCases, result);
        putAll(createLoginTestCases, result);
        putAll(alterLoginTestCases, result);
        putAll(dropLoginTestCases, result);
        putAll(createRoleTestCases, result);
        putAll(alterRoleTestCases, result);
        putAll(dropRoleTestCases, result);
        putAll(setDefaultRoleTestCases, result);
        putAll(setRoleTestCases, result);
        putAll(setPasswordTestCases, result);
        putAll(useTestCases, result);
        putAll(describeTestCases, result);
        putAll(showDatabasesTestCases, result);
        putAll(showTablesTestCases, result);
        putAll(showFunctionStatusStatementTestCases, result);
        putAll(showProcedureStatusStatementTestCases, result);
        putAll(showRelaylogEventsStatementTestCases, result);
        putAll(showProcedureCodeStatementTestCases, result);
        putAll(showColumnsTestCases, result);
        putAll(showCreateTableTestCases, result);
        putAll(showCreateTriggerTestCases, result);
        putAll(showCreateUserTestCases, result);
        putAll(showTableStatusTestCases, result);
        putAll(showIndexTestCases, result);
        putAll(showTestCases, result);
        putAll(setVariableTestCases, result);
        putAll(commonTestCases, result);
        putAll(alterFunctionTestCases, result);
        putAll(alterServerTestCase, result);
        putAll(alterSessionTestCase, result);
        putAll(alterSynonymTestCase, result);
        putAll(alterSystemTestCase, result);
        putAll(alterProcedureTestCase, result);
        putAll(alterDatabaseTestCase, result);
        putAll(createViewTestCase, result);
        putAll(createTriggerTestCase, result);
        putAll(createServerTestCase, result);
        putAll(createProcedureTestCase, result);
        putAll(createFunctionTestCase, result);
        putAll(createDatabaseTestCase, result);
        putAll(dropViewTestCase, result);
        putAll(dropTriggerTestCase, result);
        putAll(dropServerTestCase, result);
        putAll(dropProcedureTestCase, result);
        putAll(dropFunctionTestCase, result);
        putAll(dropDatabaseTestCase, result);
        putAll(callProcedureTestCase, result);
        putAll(xaTestCase, result);
        putAll(mergeTestCase, result);
        putAll(createSequenceTestCase, result);
        putAll(alterSequenceTestCase, result);
        putAll(dropSequenceTestCase, result);
        putAll(analyzeTestCase, result);
        putAll(associateStatisticsTestCase, result);
        putAll(disassociateStatisticsTestCase, result);
        putAll(auditTestCase, result);
        putAll(noAuditTestCase, result);
        putAll(commentTestCase, result);
        putAll(flashbackDatabaseTestCase, result);
        putAll(flashbackTableTestCase, result);
        putAll(purgeTestCase, result);
        putAll(renameTestCase, result);
        putAll(addResourceTestCase, result);
        putAll(alterResourceTestCase, result);
        putAll(alterDataBaseDiscoveryRuleTestCase, result);
        putAll(alterEncryptRuleTestCase, result);
        putAll(alterReadwriteSplittingRuleTestCase, result);
        putAll(alterShardingBindingTableRulesTestCase, result);
        putAll(alterShardingBroadcastTableRulesTestCase, result);
        putAll(alterShardingTableRuleTestCase, result);
        putAll(createDataBaseDiscoveryRuleTestCase, result);
        putAll(createEncryptRuleTestCase, result);
        putAll(createReadwriteSplittingRuleTestCase, result);
        putAll(createShardingBindingTableRulesTestCase, result);
        putAll(createShardingBroadcastTableRulesTestCase, result);
        putAll(createShardingTableRuleTestCase, result);
        putAll(dropDataBaseDiscoveryRuleTestCase, result);
        putAll(dropResourceTestCase, result);
        putAll(dropEncryptRuleTestCase, result);
        putAll(dropReadwriteSplittingRuleTestCase, result);
        putAll(dropShardingBindingTableRulesTestCase, result);
        putAll(dropShardingBroadcastTableRulesTestCase, result);
        putAll(dropShardingTableRuleTestCase, result);
        putAll(showDataBaseDiscoveryRulesTestCase, result);
        putAll(showEncryptRulesTestCase, result);
        putAll(showReadwriteSplittingRulesTestCase, result);
        putAll(showShardingBindingTableRulesTestCase, result);
        putAll(showShardingBroadcastTableRulesTestCase, result);
        putAll(showShardingAlgorithmsTestCase, result);
        putAll(showShardingTableRulesTestCase, result);
        putAll(showShardingTableRuleTestCase, result);
        putAll(showScalingListStatementTestCase, result);
        putAll(checkScalingStatementTestCase, result);
        putAll(showScalingCheckAlgorithmsStatementTestCase, result);
        putAll(stopScalingSourceWritingStatementTestCase, result);
        putAll(checkoutScalingStatementTestCases, result);
        putAll(showVariableStatementTestCase, result);
        putAll(showAllVariablesStatementTestCase, result);
        putAll(setVariableStatementTestCase, result);
        putAll(previewStatementTestCase, result);
        putAll(setReadwriteSplittingHintStatementTestCase, result);
        putAll(setShardingHintDatabaseValueStatementTestCase, result);
        putAll(addShardingHintDatabaseValueStatementTestCase, result);
        putAll(addShardingHintTableValueStatementTestCase, result);
        putAll(showReadwriteSplittingHintStatusStatementTestCase, result);
        putAll(showShardingHintStatusStatementTestCase, result);
        putAll(clearReadwriteSplittingHintStatementTestCase, result);
        putAll(clearShardingHintStatementTestCase, result);
        putAll(clearHintStatementTestCase, result);
        putAll(createShadowRuleTestCase, result);
        putAll(dropShadowRuleTestCase, result);
        putAll(alterShadowRuleTestCase, result);
        putAll(alterShadowAlgorithmTestCase, result);
        putAll(showShadowRulesStatementTestCase, result);
        putAll(showShadowTableRulesStatementTestCase, result);
        putAll(showShadowAlgorithmsStatementTestCase, result);
        putAll(dropShadowAlgorithmStatementTestCase, result);
        putAll(createServiceTestCase, result);
        putAll(alterServiceTestCase, result);
        putAll(dropServiceTestCase, result);
        putAll(createSchemaTestCase, result);
        putAll(alterSchemaTestCase, result);
        putAll(dropSchemaTestCase, result);
        putAll(installComponentTestCase, result);
        putAll(flushStatementTestCase, result);
        putAll(installPluginStatementTestCase, result);
        putAll(showInstanceStatementTestCases, result);
        putAll(cloneStatementTestCases, result);
        putAll(showReadwriteSplittingReadResourcesStatementTestCases, result);
        putAll(uninstallComponentStatementTestCases, result);
        putAll(createResourceGroupStatementTestCases, result);
        putAll(binlogStatementTestCases, result);
        putAll(uninstallPluginStatementTestCases, result);
        putAll(showSingleTableRulesStatementTestCases, result);
        putAll(setResourceGroupStatementTestCases, result);
        putAll(optimizeTableStatementTestCases, result);
        putAll(repairTableStatementTestCases, result);
        putAll(createShardingAlgorithmStatementTestCases, result);
        putAll(createDefaultShardingStrategyStatementTestCases, result);
        putAll(createShardingTableRuleTestCases, result);
        putAll(alterShardingTableRuleTestCases, result);
        putAll(resetStatementTestCases, result);
        putAll(resetPersistStatementTestCases, result);
        putAll(showReplicasStatementTestCases, result);
        putAll(alterShardingAlgorithmStatementTestCases, result);
        putAll(killStatementTestCases, result);
        putAll(cacheIndexStatementTestCases, result);
        putAll(createShadowAlgorithmTestCase, result);
        return result;
    }
    // CHECKSTYLE:ON
    
    private void putAll(final List<? extends SQLParserTestCase> sqlParserTestCases, final Map<String, SQLParserTestCase> target) {
        Map<String, SQLParserTestCase> sqlParserTestCaseMap = getSQLParserTestCases(sqlParserTestCases);
        Collection<String> sqlParserTestCaseIds = new HashSet<>(sqlParserTestCaseMap.keySet());
        sqlParserTestCaseIds.retainAll(target.keySet());
        Preconditions.checkState(sqlParserTestCaseIds.isEmpty(), "Find duplicated SQL Case IDs: %s", sqlParserTestCaseIds);
        target.putAll(sqlParserTestCaseMap);
    }
    
    private Map<String, SQLParserTestCase> getSQLParserTestCases(final List<? extends SQLParserTestCase> sqlParserTestCases) {
        Map<String, SQLParserTestCase> result = new HashMap<>(sqlParserTestCases.size(), 1);
        for (SQLParserTestCase each : sqlParserTestCases) {
            Preconditions.checkState(!result.containsKey(each.getSqlCaseId()), "Find duplicated SQL Case ID: %s", each.getSqlCaseId());
            result.put(each.getSqlCaseId(), each);
        }
        return result;
    }
}
