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
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.BinlogStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CheckTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CloneStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CreateLoadableFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DelimiterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.HelpStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.InstallComponentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.InstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.KillStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.LoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.RepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ResetPersistStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ResetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.RestartStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCharacterSetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowEngineStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowFunctionCodeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowGrantsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowMasterStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowOpenTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowOtherStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowPluginsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowProcedureCodeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowProfileStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowProfilesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowRelayLogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowReplicaStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowReplicasStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowSlaveHostsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowSlaveStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTriggersStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowVariablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShutdownStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.UninstallComponentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.UninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.UseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAL statement visitor for Doris.
 */
public final class DorisDALStatementVisitor extends DorisStatementVisitor implements DALStatementVisitor {
    
    @Override
    public ASTNode visitUninstallPlugin(final UninstallPluginContext ctx) {
        return new UninstallPluginStatement(((IdentifierValue) visit(ctx.pluginName())).getValue());
    }
    
    @Override
    public ASTNode visitShowCreateDatabase(final ShowCreateDatabaseContext ctx) {
        return new ShowCreateDatabaseStatement(((DatabaseSegment) visit(ctx.databaseName())).getIdentifier().getValue());
    }
    
    @Override
    public ASTNode visitShowBinaryLogs(final ShowBinaryLogsContext ctx) {
        return new ShowBinaryLogsStatement();
    }
    
    @Override
    public ASTNode visitShowStatus(final ShowStatusContext ctx) {
        ShowStatusStatement result = new ShowStatusStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateView(final ShowCreateViewContext ctx) {
        return new ShowCreateViewStatement(null);
    }
    
    @Override
    public ASTNode visitShowEngines(final ShowEnginesContext ctx) {
        return new ShowOtherStatement();
    }
    
    @Override
    public ASTNode visitShowEngine(final ShowEngineContext ctx) {
        return new ShowEngineStatement(ctx.engineRef().getText());
    }
    
    @Override
    public ASTNode visitShowCharset(final ShowCharsetContext ctx) {
        return new ShowOtherStatement();
    }
    
    @Override
    public ASTNode visitShowCreateEvent(final ShowCreateEventContext ctx) {
        return new ShowCreateEventStatement(((IdentifierValue) visit(ctx.eventName())).getValue());
    }
    
    @Override
    public ASTNode visitShowCreateFunction(final ShowCreateFunctionContext ctx) {
        return new ShowCreateFunctionStatement(((FunctionSegment) visit(ctx.functionName())).getFunctionName());
    }
    
    @Override
    public ASTNode visitShowCreateProcedure(final ShowCreateProcedureContext ctx) {
        return new ShowCreateProcedureStatement(((IdentifierValue) visit(ctx.procedureName())).getValue());
    }
    
    @Override
    public ASTNode visitShowBinlogEvents(final ShowBinlogEventsContext ctx) {
        return new ShowBinlogEventsStatement(null == ctx.logName() ? null : ctx.logName().getText(), null == ctx.limitClause() ? null : (LimitSegment) visit(ctx.limitClause()));
    }
    
    @Override
    public ASTNode visitShowErrors(final ShowErrorsContext ctx) {
        return new ShowErrorsStatement(null == ctx.limitClause() ? null : (LimitSegment) visit(ctx.limitClause()));
    }
    
    @Override
    public ASTNode visitShowWarnings(final ShowWarningsContext ctx) {
        return new ShowWarningsStatement(null == ctx.limitClause() ? null : (LimitSegment) visit(ctx.limitClause()));
    }
    
    @Override
    public ASTNode visitResetStatement(final ResetStatementContext ctx) {
        ResetPersistContext persistContext = ctx.resetPersist();
        return null == persistContext
                ? new ResetStatement(
                        ctx.resetOption().stream().filter(each -> null != each.MASTER() || null != each.SLAVE()).map(each -> (ResetOptionSegment) visit(each)).collect(Collectors.toList()))
                : visit(persistContext);
    }
    
    @Override
    public ASTNode visitResetPersist(final ResetPersistContext ctx) {
        return new ResetPersistStatement(null != ctx.ifExists(), null == ctx.identifier() ? null : new IdentifierValue(ctx.identifier().getText()));
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
        return new ShowReplicasStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRepairTable(final RepairTableContext ctx) {
        return new RepairTableStatement(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        return new AnalyzeTableStatement(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
    }
    
    @Override
    public ASTNode visitCacheIndex(final CacheIndexContext ctx) {
        CacheIndexStatement result = new CacheIndexStatement();
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
        return new ChecksumTableStatement(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
    }
    
    @Override
    public ASTNode visitFlush(final FlushContext ctx) {
        return null == ctx.tablesOption() ? new FlushStatement(Collections.emptyList(), false) : visit(ctx.tablesOption());
    }
    
    @Override
    public ASTNode visitTablesOption(final TablesOptionContext ctx) {
        return new FlushStatement(ctx.tableName().stream().map(each -> (SimpleTableSegment) visit(each)).collect(Collectors.toList()), true);
    }
    
    @Override
    public ASTNode visitKill(final KillContext ctx) {
        return new KillStatement(null == ctx.AT_() ? ctx.IDENTIFIER_().getText() : ctx.AT_().getText() + ctx.IDENTIFIER_().getText(), null);
    }
    
    @Override
    public ASTNode visitLoadIndexInfo(final LoadIndexInfoContext ctx) {
        return new LoadIndexInfoStatement(ctx.loadTableIndexList().stream().map(each -> (LoadTableIndexSegment) visit(each)).collect(Collectors.toList()));
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
        return new InstallPluginStatement(((IdentifierValue) visit(ctx.pluginName())).getValue());
    }
    
    @Override
    public ASTNode visitClone(final CloneContext ctx) {
        return new CloneStatement((CloneActionSegment) visit(ctx.cloneAction()));
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
        return new OptimizeTableStatement(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
    }
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        return new UseStatement(((DatabaseSegment) visit(ctx.databaseName())).getIdentifier().getValue());
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        ExplainStatement result = new ExplainStatement();
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
        return new ShowProcedureCodeStatement((FunctionSegment) visit(ctx.functionName()));
    }
    
    @Override
    public ASTNode visitShowProfile(final ShowProfileContext ctx) {
        return new ShowProfileStatement(null == ctx.limitClause() ? null : (LimitSegment) visit(ctx.limitClause()));
    }
    
    @Override
    public ASTNode visitShowProfiles(final ShowProfilesContext ctx) {
        return new ShowProfilesStatement();
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        ShowDatabasesStatement result = new ShowDatabasesStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowEvents(final ShowEventsContext ctx) {
        ShowEventsStatement result = new ShowEventsStatement(
                null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()), null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        ShowTablesStatement result = new ShowTablesStatement(null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()),
                null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()), null != ctx.FULL());
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowTriggers(final ShowTriggersContext ctx) {
        ShowTriggersStatement result = new ShowTriggersStatement(
                null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()), null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowWhereClause(final ShowWhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        ShowTableStatusStatement result = new ShowTableStatusStatement(
                null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()), null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        ShowColumnsStatement result = new ShowColumnsStatement(null == ctx.fromTable() ? null : ((FromTableSegment) visit(ctx.fromTable())).getTable(),
                null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()), null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
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
        return new ShowIndexStatement(null == ctx.fromTable() ? null : ((FromTableSegment) visitFromTable(ctx.fromTable())).getTable(),
                null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()));
    }
    
    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        return new ShowCreateTableStatement((SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitShowCreateTrigger(final ShowCreateTriggerContext ctx) {
        return new ShowCreateTriggerStatement(((IdentifierValue) visit(ctx.triggerName())).getValue());
    }
    
    @Override
    public ASTNode visitShowRelaylogEvent(final ShowRelaylogEventContext ctx) {
        return new ShowRelayLogEventsStatement(null == ctx.logName() ? null : ((StringLiteralValue) visit(ctx.logName().stringLiterals().string_())).getValue(),
                null == ctx.limitClause() ? null : (LimitSegment) visit(ctx.limitClause()), null == ctx.channelName() ? null : ((IdentifierValue) visit(ctx.channelName())).getValue());
    }
    
    @Override
    public ASTNode visitShowFunctionCode(final ShowFunctionCodeContext ctx) {
        return new ShowFunctionCodeStatement(((FunctionSegment) visit(ctx.functionName())).getFunctionName());
    }
    
    @Override
    public ASTNode visitShowGrants(final ShowGrantsContext ctx) {
        return new ShowGrantsStatement();
    }
    
    @Override
    public ASTNode visitShowMasterStatus(final ShowMasterStatusContext ctx) {
        return new ShowMasterStatusStatement();
    }
    
    @Override
    public ASTNode visitShowSlaveHosts(final ShowSlaveHostsContext ctx) {
        return new ShowSlaveHostsStatement();
    }
    
    @Override
    public ASTNode visitShowReplicaStatus(final ShowReplicaStatusContext ctx) {
        return new ShowReplicaStatusStatement(null == ctx.channelName() ? null : ((IdentifierValue) visit(ctx.channelName())).getValue());
    }
    
    @Override
    public ASTNode visitShowSlaveStatus(final ShowSlaveStatusContext ctx) {
        return new ShowSlaveStatusStatement(null == ctx.channelName() ? null : ((IdentifierValue) visit(ctx.channelName())).getValue());
    }
    
    @Override
    public ASTNode visitCreateResourceGroup(final CreateResourceGroupContext ctx) {
        return new CreateResourceGroupStatement(((IdentifierValue) visit(ctx.groupName())).getValue());
    }
    
    @Override
    public ASTNode visitBinlog(final BinlogContext ctx) {
        return new BinlogStatement(((StringLiteralValue) visit(ctx.stringLiterals())).getValue());
    }
    
    @Override
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowVariables(final ShowVariablesContext ctx) {
        ShowVariablesStatement result = new ShowVariablesStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCharacterSet(final ShowCharacterSetContext ctx) {
        ShowCharacterSetStatement result = new ShowCharacterSetStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCollation(final ShowCollationContext ctx) {
        ShowCollationStatement result = new ShowCollationStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowFunctionStatus(final ShowFunctionStatusContext ctx) {
        ShowFunctionStatusStatement result = new ShowFunctionStatusStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowProcedureStatus(final ShowProcedureStatusContext ctx) {
        ShowProcedureStatusStatement result = new ShowProcedureStatusStatement(null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowOpenTables(final ShowOpenTablesContext ctx) {
        ShowOpenTablesStatement result = new ShowOpenTablesStatement(
                null == ctx.fromDatabase() ? null : (FromDatabaseSegment) visit(ctx.fromDatabase()), null == ctx.showFilter() ? null : (ShowFilterSegment) visit(ctx.showFilter()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowPlugins(final ShowPluginsContext ctx) {
        return new ShowPluginsStatement();
    }
    
    @Override
    public ASTNode visitShowPrivileges(final ShowPrivilegesContext ctx) {
        return new ShowPrivilegesStatement();
    }
    
    @Override
    public ASTNode visitShutdown(final ShutdownContext ctx) {
        return new ShutdownStatement();
    }
    
    @Override
    public ASTNode visitShowProcesslist(final ShowProcesslistContext ctx) {
        return new ShowProcessListStatement(null != ctx.FULL());
    }
    
    @Override
    public ASTNode visitShowCreateUser(final ShowCreateUserContext ctx) {
        return new ShowCreateUserStatement(((IdentifierValue) visit(ctx.username())).getValue());
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        return new SetStatement(getVariableAssigns(ctx.optionValueList()));
    }
    
    private List<VariableAssignSegment> getVariableAssigns(final OptionValueListContext ctx) {
        List<VariableAssignSegment> result = new LinkedList<>();
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
        return new SetStatement(Collections.singletonList(new VariableAssignSegment(startIndex, stopIndex, variable, assignValue)));
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
        return new CreateLoadableFunctionStatement();
    }
    
    @Override
    public ASTNode visitInstallComponent(final InstallComponentContext ctx) {
        return new InstallComponentStatement(ctx.componentName().stream().map(each -> ((StringLiteralValue) visit(each.string_())).getValue()).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitUninstallComponent(final UninstallComponentContext ctx) {
        return new UninstallComponentStatement(ctx.componentName().stream().map(each -> ((StringLiteralValue) visit(each.string_())).getValue()).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitRestart(final RestartContext ctx) {
        return new RestartStatement();
    }
    
    @Override
    public ASTNode visitSetResourceGroup(final SetResourceGroupContext ctx) {
        return new SetResourceGroupStatement(((IdentifierValue) visit(ctx.groupName())).getValue());
    }
    
    @Override
    public ASTNode visitCheckTable(final CheckTableContext ctx) {
        return new CheckTableStatement(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
    }
    
    @Override
    public ASTNode visitDropResourceGroup(final DropResourceGroupContext ctx) {
        return new DropResourceGroupStatement(((IdentifierValue) visit(ctx.groupName())).getValue());
    }
    
    @Override
    public ASTNode visitAlterResourceGroup(final AlterResourceGroupContext ctx) {
        return new AlterResourceGroupStatement(((IdentifierValue) visit(ctx.groupName())).getValue());
    }
    
    @Override
    public ASTNode visitDelimiter(final DelimiterContext ctx) {
        return new DelimiterStatement(ctx.delimiterName().getText());
    }
    
    @Override
    public ASTNode visitHelp(final HelpContext ctx) {
        return new HelpStatement(ctx.textOrIdentifier().getText());
    }
}
