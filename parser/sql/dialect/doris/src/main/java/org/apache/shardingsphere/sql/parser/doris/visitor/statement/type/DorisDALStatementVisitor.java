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

package org.apache.shardingsphere.sql.parser.doris.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BinaryLogFileIndexNumberContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BinlogContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CacheIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CacheTableIndexListContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ChannelOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CheckTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ChecksumTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CloneActionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CloneContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CloneInstanceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ComponentNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateLoadableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DelimiterContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ExplainableStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FlushContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FromDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FromTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.HelpContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.InstallComponentContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.InstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KillContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadIndexInfoContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadTableIndexListContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.OptimizeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.OptionTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.OptionValueContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.OptionValueListContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.OptionValueNoOptionTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PartitionListContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PartitionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RepairTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ResetOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ResetPersistContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ResetStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RestartContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SetCharacterContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SetResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SetVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowBinaryLogsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowBinlogEventsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCharacterSetContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCharsetContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCollationContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateEventContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowCreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowEngineContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowEnginesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowErrorsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowEventsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowFilterContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowFunctionCodeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowFunctionStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowGrantsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowMasterStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowOpenTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowPluginsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowProcedureCodeContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowProcedureStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowProcesslistContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowProfilesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowRelaylogEventContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowReplicaStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowReplicasContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowSlaveHostsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowSlaveStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowTriggersContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowVariablesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowWarningsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShowWhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ShutdownContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TablesOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.UninstallComponentContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.UninstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.CacheTableIndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.CloneActionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.CloneInstanceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.LoadTableIndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ResetMasterOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ResetOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ResetSlaveOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisBinlogStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCheckTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCloneStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateLoadableFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisDelimiterStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisDropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisFlushStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisHelpStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisInstallComponentStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisKillStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisRepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisResetPersistStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisResetStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisRestartStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisSetStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCharacterSetStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowEngineStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowFunctionCodeStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowGrantsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowMasterStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowOpenTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowOtherStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowPluginsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowProcedureCodeStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowProfileStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowProfilesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowRelayLogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowReplicaStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowReplicasStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowSlaveHostsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowSlaveStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowTriggersStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowVariablesStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShutdownStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisUninstallComponentStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisUseStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * DAL statement visitor for Doris.
 */
public final class DorisDALStatementVisitor extends DorisStatementVisitor implements DALStatementVisitor {
    
    @Override
    public ASTNode visitUninstallPlugin(final UninstallPluginContext ctx) {
        DorisUninstallPluginStatement result = new DorisUninstallPluginStatement();
        result.setPluginName(((IdentifierValue) visit(ctx.pluginName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateDatabase(final ShowCreateDatabaseContext ctx) {
        DorisShowCreateDatabaseStatement result = new DorisShowCreateDatabaseStatement();
        result.setDatabaseName(((DatabaseSegment) visit(ctx.databaseName())).getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowBinaryLogs(final ShowBinaryLogsContext ctx) {
        return new DorisShowBinaryLogsStatement();
    }
    
    @Override
    public ASTNode visitShowStatus(final ShowStatusContext ctx) {
        DorisShowStatusStatement result = new DorisShowStatusStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateView(final ShowCreateViewContext ctx) {
        return new DorisShowCreateViewStatement();
    }
    
    @Override
    public ASTNode visitShowEngines(final ShowEnginesContext ctx) {
        return new DorisShowOtherStatement();
    }
    
    @Override
    public ASTNode visitShowEngine(final ShowEngineContext ctx) {
        DorisShowEngineStatement result = new DorisShowEngineStatement();
        result.setEngineName(ctx.engineRef().getText());
        return result;
    }
    
    @Override
    public ASTNode visitShowCharset(final ShowCharsetContext ctx) {
        return new DorisShowOtherStatement();
    }
    
    @Override
    public ASTNode visitShowCreateEvent(final ShowCreateEventContext ctx) {
        DorisShowCreateEventStatement result = new DorisShowCreateEventStatement();
        result.setEventName(((IdentifierValue) visit(ctx.eventName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateFunction(final ShowCreateFunctionContext ctx) {
        DorisShowCreateFunctionStatement result = new DorisShowCreateFunctionStatement();
        result.setFunctionName(((FunctionSegment) visit(ctx.functionName())).getFunctionName());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateProcedure(final ShowCreateProcedureContext ctx) {
        DorisShowCreateProcedureStatement result = new DorisShowCreateProcedureStatement();
        result.setProcedureName(((IdentifierValue) visit(ctx.procedureName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowBinlogEvents(final ShowBinlogEventsContext ctx) {
        DorisShowBinlogEventsStatement result = new DorisShowBinlogEventsStatement();
        if (null != ctx.logName()) {
            result.setLogName(ctx.logName().getText());
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowErrors(final ShowErrorsContext ctx) {
        DorisShowErrorsStatement result = new DorisShowErrorsStatement();
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowWarnings(final ShowWarningsContext ctx) {
        DorisShowWarningsStatement result = new DorisShowWarningsStatement();
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitResetStatement(final ResetStatementContext ctx) {
        ResetPersistContext persistContext = ctx.resetPersist();
        if (null != persistContext) {
            return visit(persistContext);
        }
        DorisResetStatement result = new DorisResetStatement();
        for (ResetOptionContext each : ctx.resetOption()) {
            if (null != each.MASTER() || null != each.SLAVE()) {
                result.getOptions().add((ResetOptionSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitResetPersist(final ResetPersistContext ctx) {
        return new DorisResetPersistStatement(null != ctx.ifExists(), null == ctx.identifier() ? null : new IdentifierValue(ctx.identifier().getText()));
    }
    
    @Override
    public ASTNode visitResetOption(final ResetOptionContext ctx) {
        if (null != ctx.MASTER()) {
            ResetMasterOptionSegment result = new ResetMasterOptionSegment();
            if (null != ctx.binaryLogFileIndexNumber()) {
                result.setBinaryLogFileIndexNumber(((NumberLiteralValue) visit(ctx.binaryLogFileIndexNumber())).getValue().longValue());
            }
            result.setStartIndex(ctx.start.getStartIndex());
            result.setStopIndex(ctx.stop.getStopIndex());
            return result;
        }
        ResetSlaveOptionSegment result = new ResetSlaveOptionSegment();
        if (null != ctx.ALL()) {
            result.setAll(true);
        }
        if (null != ctx.channelOption()) {
            result.setChannelOption(((StringLiteralValue) visit(ctx.channelOption())).getValue());
        }
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        return result;
    }
    
    @Override
    public ASTNode visitChannelOption(final ChannelOptionContext ctx) {
        return visit(ctx.string_());
    }
    
    @Override
    public ASTNode visitBinaryLogFileIndexNumber(final BinaryLogFileIndexNumberContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitShowReplicas(final ShowReplicasContext ctx) {
        return new DorisShowReplicasStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRepairTable(final RepairTableContext ctx) {
        DorisRepairTableStatement result = new DorisRepairTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        DorisAnalyzeTableStatement result = new DorisAnalyzeTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCacheIndex(final CacheIndexContext ctx) {
        DorisCacheIndexStatement result = new DorisCacheIndexStatement();
        if (null != ctx.cacheTableIndexList()) {
            for (CacheTableIndexListContext each : ctx.cacheTableIndexList()) {
                result.getTableIndexes().add((CacheTableIndexSegment) visit(each));
            }
        }
        if (null != ctx.partitionList()) {
            SimpleTableSegment table = (SimpleTableSegment) visit(ctx.tableName());
            PartitionDefinitionSegment segment = new PartitionDefinitionSegment(ctx.tableName().getStart().getStartIndex(), ctx.partitionList().getStop().getStopIndex(), table);
            segment.getPartitions().addAll(((CollectionValue<PartitionSegment>) visit(ctx.partitionList())).getValue());
            result.setPartitionDefinition(segment);
        }
        if (null != ctx.DEFAULT()) {
            result.setName(new IdentifierValue(ctx.DEFAULT().getText()));
        } else {
            result.setName((IdentifierValue) visit(ctx.identifier()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCacheTableIndexList(final CacheTableIndexListContext ctx) {
        CacheTableIndexSegment result = new CacheTableIndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SimpleTableSegment) visit(ctx.tableName()));
        for (IndexNameContext each : ctx.indexName()) {
            result.getIndexes().add((IndexSegment) visitIndexName(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitPartitionList(final PartitionListContext ctx) {
        CollectionValue<PartitionSegment> result = new CollectionValue<>();
        for (PartitionNameContext each : ctx.partitionName()) {
            result.getValue().add((PartitionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitPartitionName(final PartitionNameContext ctx) {
        return new PartitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public ASTNode visitChecksumTable(final ChecksumTableContext ctx) {
        DorisChecksumTableStatement result = new DorisChecksumTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitFlush(final FlushContext ctx) {
        if (null != ctx.tablesOption()) {
            return visit(ctx.tablesOption());
        }
        return new DorisFlushStatement();
    }
    
    @Override
    public ASTNode visitTablesOption(final TablesOptionContext ctx) {
        DorisFlushStatement result = new DorisFlushStatement();
        result.setFlushTable(true);
        for (TableNameContext each : ctx.tableName()) {
            result.getTables().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitKill(final KillContext ctx) {
        DorisKillStatement result = new DorisKillStatement();
        if (null != ctx.AT_()) {
            result.setProcessId(ctx.AT_().getText() + ctx.IDENTIFIER_().getText());
        } else {
            result.setProcessId(ctx.IDENTIFIER_().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitLoadIndexInfo(final LoadIndexInfoContext ctx) {
        DorisLoadIndexInfoStatement result = new DorisLoadIndexInfoStatement();
        for (LoadTableIndexListContext each : ctx.loadTableIndexList()) {
            result.getTableIndexes().add((LoadTableIndexSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitLoadTableIndexList(final LoadTableIndexListContext ctx) {
        LoadTableIndexSegment result = new LoadTableIndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.indexName()) {
            for (IndexNameContext each : ctx.indexName()) {
                result.getIndexes().add((IndexSegment) visitIndexName(each));
            }
        }
        if (null != ctx.partitionList()) {
            result.getPartitions().addAll(((CollectionValue<PartitionSegment>) visit(ctx.partitionList())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitInstallPlugin(final InstallPluginContext ctx) {
        DorisInstallPluginStatement result = new DorisInstallPluginStatement();
        result.setPluginName(((IdentifierValue) visit(ctx.pluginName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitClone(final CloneContext ctx) {
        DorisCloneStatement result = new DorisCloneStatement();
        result.setCloneActionSegment((CloneActionSegment) visit(ctx.cloneAction()));
        return result;
    }
    
    @Override
    public ASTNode visitCloneAction(final CloneActionContext ctx) {
        CloneActionSegment result = new CloneActionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.cloneInstance()) {
            CloneInstanceContext cloneInstance = ctx.cloneInstance();
            CloneInstanceSegment cloneInstanceSegment = new CloneInstanceSegment(cloneInstance.start.getStartIndex(), cloneInstance.stop.getStopIndex());
            cloneInstanceSegment.setUsername(((StringLiteralValue) visitUsername(cloneInstance.username())).getValue());
            cloneInstanceSegment.setHostname(((StringLiteralValue) visit(cloneInstance.hostname())).getValue());
            cloneInstanceSegment.setPort(new NumberLiteralValue(cloneInstance.port().NUMBER_().getText()).getValue().intValue());
            cloneInstanceSegment.setPassword(((StringLiteralValue) visit(ctx.string_())).getValue());
            if (null != ctx.SSL() && null == ctx.NO()) {
                cloneInstanceSegment.setSslRequired(true);
            }
            result.setCloneInstance(cloneInstanceSegment);
        }
        if (null != ctx.cloneDir()) {
            result.setCloneDir(((StringLiteralValue) visit(ctx.cloneDir())).getValue());
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitOptimizeTable(final OptimizeTableContext ctx) {
        DorisOptimizeTableStatement result = new DorisOptimizeTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        DorisUseStatement result = new DorisUseStatement();
        result.setDatabase(((DatabaseSegment) visit(ctx.databaseName())).getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        DorisExplainStatement result = new DorisExplainStatement();
        if (null != ctx.tableName()) {
            result.setSimpleTable((SimpleTableSegment) visit(ctx.tableName()));
            if (null != ctx.columnRef()) {
                result.setColumnWild((ColumnSegment) visit(ctx.columnRef()));
            } else if (null != ctx.textString()) {
                result.setColumnWild((ColumnSegment) visit(ctx.textString()));
            }
        } else if (null != ctx.explainableStatement()) {
            result.setSqlStatement((SQLStatement) visit(ctx.explainableStatement()));
        } else if (null != ctx.select()) {
            result.setSqlStatement((SQLStatement) visit(ctx.select()));
        } else if (null != ctx.delete()) {
            result.setSqlStatement((SQLStatement) visit(ctx.delete()));
        } else if (null != ctx.update()) {
            result.setSqlStatement((SQLStatement) visit(ctx.update()));
        } else if (null != ctx.insert()) {
            result.setSqlStatement((SQLStatement) visit(ctx.insert()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitExplainableStatement(final ExplainableStatementContext ctx) {
        if (null != ctx.select()) {
            return visit(ctx.select());
        }
        if (null != ctx.delete()) {
            return visit(ctx.delete());
        }
        if (null != ctx.insert()) {
            return visit(ctx.insert());
        }
        if (null != ctx.replace()) {
            return visit(ctx.replace());
        }
        return visit(ctx.update());
    }
    
    @Override
    public ASTNode visitShowProcedureCode(final ShowProcedureCodeContext ctx) {
        DorisShowProcedureCodeStatement result = new DorisShowProcedureCodeStatement();
        result.setFunction((FunctionSegment) visit(ctx.functionName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowProfile(final ShowProfileContext ctx) {
        DorisShowProfileStatement result = new DorisShowProfileStatement();
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowProfiles(final ShowProfilesContext ctx) {
        return new DorisShowProfilesStatement();
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        DorisShowDatabasesStatement result = new DorisShowDatabasesStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowEvents(final ShowEventsContext ctx) {
        DorisShowEventsStatement result = new DorisShowEventsStatement();
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        DorisShowTablesStatement result = new DorisShowTablesStatement();
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setContainsFull(null != ctx.FULL());
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowTriggers(final ShowTriggersContext ctx) {
        DorisShowTriggersStatement result = new DorisShowTriggersStatement();
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowWhereClause(final ShowWhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        DorisShowTableStatusStatement result = new DorisShowTableStatusStatement();
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        DorisShowColumnsStatement result = new DorisShowColumnsStatement();
        if (null != ctx.fromTable()) {
            result.setTable(((FromTableSegment) visit(ctx.fromTable())).getTable());
        }
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowFilter(final ShowFilterContext ctx) {
        ShowFilterSegment result = new ShowFilterSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.showLike()) {
            result.setLike((ShowLikeSegment) visit(ctx.showLike()));
        }
        if (null != ctx.showWhereClause()) {
            result.setWhere((WhereSegment) visit(ctx.showWhereClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowIndex(final ShowIndexContext ctx) {
        DorisShowIndexStatement result = new DorisShowIndexStatement();
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.fromTable()) {
            result.setTable(((FromTableSegment) visitFromTable(ctx.fromTable())).getTable());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        DorisShowCreateTableStatement result = new DorisShowCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateTrigger(final ShowCreateTriggerContext ctx) {
        DorisShowCreateTriggerStatement result = new DorisShowCreateTriggerStatement();
        result.setName(((IdentifierValue) visit(ctx.triggerName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowRelaylogEvent(final ShowRelaylogEventContext ctx) {
        DorisShowRelayLogEventsStatement result = new DorisShowRelayLogEventsStatement();
        if (null != ctx.logName()) {
            result.setLogName(((StringLiteralValue) visit(ctx.logName().stringLiterals().string_())).getValue());
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        if (null != ctx.channelName()) {
            result.setChannel(((IdentifierValue) visit(ctx.channelName())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowFunctionCode(final ShowFunctionCodeContext ctx) {
        DorisShowFunctionCodeStatement result = new DorisShowFunctionCodeStatement();
        result.setFunctionName(((FunctionSegment) visit(ctx.functionName())).getFunctionName());
        return result;
    }
    
    @Override
    public ASTNode visitShowGrants(final ShowGrantsContext ctx) {
        return new DorisShowGrantsStatement();
    }
    
    @Override
    public ASTNode visitShowMasterStatus(final ShowMasterStatusContext ctx) {
        return new DorisShowMasterStatusStatement();
    }
    
    @Override
    public ASTNode visitShowSlaveHosts(final ShowSlaveHostsContext ctx) {
        return new DorisShowSlaveHostsStatement();
    }
    
    @Override
    public ASTNode visitShowReplicaStatus(final ShowReplicaStatusContext ctx) {
        DorisShowReplicaStatusStatement result = new DorisShowReplicaStatusStatement();
        if (null != ctx.channelName()) {
            result.setChannel(((IdentifierValue) visit(ctx.channelName())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowSlaveStatus(final ShowSlaveStatusContext ctx) {
        DorisShowSlaveStatusStatement result = new DorisShowSlaveStatusStatement();
        if (null != ctx.channelName()) {
            result.setChannel(((IdentifierValue) visit(ctx.channelName())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateResourceGroup(final CreateResourceGroupContext ctx) {
        DorisCreateResourceGroupStatement result = new DorisCreateResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitBinlog(final BinlogContext ctx) {
        return new DorisBinlogStatement(((StringLiteralValue) visit(ctx.stringLiterals())).getValue());
    }
    
    @Override
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowVariables(final ShowVariablesContext ctx) {
        DorisShowVariablesStatement result = new DorisShowVariablesStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCharacterSet(final ShowCharacterSetContext ctx) {
        DorisShowCharacterSetStatement result = new DorisShowCharacterSetStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCollation(final ShowCollationContext ctx) {
        DorisShowCollationStatement result = new DorisShowCollationStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowFunctionStatus(final ShowFunctionStatusContext ctx) {
        DorisShowFunctionStatusStatement result = new DorisShowFunctionStatusStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowProcedureStatus(final ShowProcedureStatusContext ctx) {
        DorisShowProcedureStatusStatement result = new DorisShowProcedureStatusStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowOpenTables(final ShowOpenTablesContext ctx) {
        DorisShowOpenTablesStatement result = new DorisShowOpenTablesStatement();
        if (null != ctx.fromDatabase()) {
            result.setFromDatabase((FromDatabaseSegment) visit(ctx.fromDatabase()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowPlugins(final ShowPluginsContext ctx) {
        return new DorisShowPluginsStatement();
    }
    
    @Override
    public ASTNode visitShowPrivileges(final ShowPrivilegesContext ctx) {
        return new DorisShowPrivilegesStatement();
    }
    
    @Override
    public ASTNode visitShutdown(final ShutdownContext ctx) {
        return new DorisShutdownStatement();
    }
    
    @Override
    public ASTNode visitShowProcesslist(final ShowProcesslistContext ctx) {
        return new DorisShowProcessListStatement(null != ctx.FULL());
    }
    
    @Override
    public ASTNode visitShowCreateUser(final ShowCreateUserContext ctx) {
        DorisShowCreateUserStatement result = new DorisShowCreateUserStatement();
        result.setName(((IdentifierValue) visit(ctx.username())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        DorisSetStatement result = new DorisSetStatement();
        Collection<VariableAssignSegment> variableAssigns = getVariableAssigns(ctx.optionValueList());
        result.getVariableAssigns().addAll(variableAssigns);
        return result;
    }
    
    private Collection<VariableAssignSegment> getVariableAssigns(final OptionValueListContext ctx) {
        Collection<VariableAssignSegment> result = new LinkedList<>();
        result.add(null == ctx.optionValueNoOptionType() ? getVariableAssignSegment(ctx) : getVariableAssignSegment(ctx.optionValueNoOptionType()));
        for (OptionValueContext each : ctx.optionValue()) {
            result.add(getVariableAssignSegment(each));
        }
        return result;
    }
    
    private VariableAssignSegment getVariableAssignSegment(final OptionValueContext ctx) {
        if (null != ctx.optionValueNoOptionType()) {
            return getVariableAssignSegment(ctx.optionValueNoOptionType());
        }
        VariableSegment variable = new VariableSegment(
                ctx.internalVariableName().start.getStartIndex(), ctx.internalVariableName().stop.getStopIndex(), ctx.internalVariableName().getText(), ctx.optionType().getText());
        return new VariableAssignSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), variable, ctx.setExprOrDefault().getText());
    }
    
    private VariableAssignSegment getVariableAssignSegment(final OptionValueListContext ctx) {
        VariableSegment variable = new VariableSegment(
                ctx.internalVariableName().start.getStartIndex(), ctx.internalVariableName().stop.getStopIndex(), ctx.internalVariableName().getText(), ctx.optionType().getText());
        return new VariableAssignSegment(ctx.start.getStartIndex(), ctx.setExprOrDefault().stop.getStopIndex(), variable, ctx.setExprOrDefault().getText());
    }
    
    private VariableAssignSegment getVariableAssignSegment(final OptionValueNoOptionTypeContext ctx) {
        return new VariableAssignSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getVariableSegment(ctx), getAssignValue(ctx));
    }
    
    private VariableSegment getVariableSegment(final OptionValueNoOptionTypeContext ctx) {
        if (null != ctx.NAMES()) {
            // TODO Consider setting all three system variables: character_set_client, character_set_results, character_set_connection
            return new VariableSegment(ctx.NAMES().getSymbol().getStartIndex(), ctx.NAMES().getSymbol().getStopIndex(), "character_set_client");
        }
        if (null != ctx.internalVariableName()) {
            return new VariableSegment(ctx.internalVariableName().start.getStartIndex(), ctx.internalVariableName().stop.getStopIndex(), ctx.internalVariableName().getText());
        }
        if (null != ctx.userVariable()) {
            return new VariableSegment(ctx.userVariable().start.getStartIndex(), ctx.userVariable().stop.getStopIndex(), ctx.userVariable().getText());
        }
        if (null != ctx.setSystemVariable()) {
            VariableSegment result = new VariableSegment(
                    ctx.setSystemVariable().start.getStartIndex(), ctx.setSystemVariable().stop.getStopIndex(), ctx.setSystemVariable().internalVariableName().getText());
            OptionTypeContext optionType = ctx.setSystemVariable().optionType();
            result.setScope(null == optionType ? "SESSION" : optionType.getText());
            return result;
        }
        return null;
    }
    
    private String getAssignValue(final OptionValueNoOptionTypeContext ctx) {
        if (null != ctx.NAMES()) {
            return ctx.charsetName().getText();
        }
        if (null != ctx.internalVariableName()) {
            return ctx.setExprOrDefault().getText();
        }
        if (null != ctx.userVariable()) {
            return ctx.expr().getText();
        }
        if (null != ctx.setSystemVariable()) {
            return ctx.setExprOrDefault().getText();
        }
        return null;
    }
    
    @Override
    public ASTNode visitSetCharacter(final SetCharacterContext ctx) {
        int startIndex = null == ctx.CHARSET() ? ctx.CHARACTER().getSymbol().getStartIndex() : ctx.CHARSET().getSymbol().getStartIndex();
        int stopIndex = null == ctx.CHARSET() ? ctx.SET(1).getSymbol().getStopIndex() : ctx.CHARSET().getSymbol().getStopIndex();
        // TODO Consider setting all three system variables: character_set_client, character_set_results, character_set_connection
        VariableSegment variable = new VariableSegment(startIndex, stopIndex, (null == ctx.CHARSET()) ? "character_set_client" : ctx.CHARSET().getText());
        String assignValue = null == ctx.DEFAULT() ? ctx.charsetName().getText() : ctx.DEFAULT().getText();
        VariableAssignSegment characterSet = new VariableAssignSegment(startIndex, stopIndex, variable, assignValue);
        DorisSetStatement result = new DorisSetStatement();
        result.getVariableAssigns().add(characterSet);
        return result;
    }
    
    @Override
    public ASTNode visitFromDatabase(final FromDatabaseContext ctx) {
        return new FromDatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getValue());
    }
    
    @Override
    public ASTNode visitCreateLoadableFunction(final CreateLoadableFunctionContext ctx) {
        return new DorisCreateLoadableFunctionStatement();
    }
    
    @Override
    public ASTNode visitInstallComponent(final InstallComponentContext ctx) {
        DorisInstallComponentStatement result = new DorisInstallComponentStatement();
        List<String> components = new LinkedList<>();
        for (ComponentNameContext each : ctx.componentName()) {
            components.add(((StringLiteralValue) visit(each.string_())).getValue());
        }
        result.getComponents().addAll(components);
        return result;
    }
    
    @Override
    public ASTNode visitUninstallComponent(final UninstallComponentContext ctx) {
        DorisUninstallComponentStatement result = new DorisUninstallComponentStatement();
        List<String> components = new LinkedList<>();
        for (ComponentNameContext each : ctx.componentName()) {
            components.add(((StringLiteralValue) visit(each.string_())).getValue());
        }
        result.getComponents().addAll(components);
        return result;
    }
    
    @Override
    public ASTNode visitRestart(final RestartContext ctx) {
        return new DorisRestartStatement();
    }
    
    @Override
    public ASTNode visitSetResourceGroup(final SetResourceGroupContext ctx) {
        DorisSetResourceGroupStatement result = new DorisSetResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCheckTable(final CheckTableContext ctx) {
        DorisCheckTableStatement result = new DorisCheckTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropResourceGroup(final DropResourceGroupContext ctx) {
        DorisDropResourceGroupStatement result = new DorisDropResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitAlterResourceGroup(final AlterResourceGroupContext ctx) {
        DorisAlterResourceGroupStatement result = new DorisAlterResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDelimiter(final DelimiterContext ctx) {
        DorisDelimiterStatement result = new DorisDelimiterStatement();
        result.setDelimiterName(ctx.delimiterName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitHelp(final HelpContext ctx) {
        DorisHelpStatement result = new DorisHelpStatement();
        result.setSearchString(ctx.textOrIdentifier().getText());
        return result;
    }
}
