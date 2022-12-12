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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BinaryLogFileIndexNumberContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BinlogContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CacheIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CacheTableIndexListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChannelOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CheckTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChecksumTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CloneActionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CloneContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CloneInstanceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ComponentNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLoadableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DelimiterContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExplainableStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FlushContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.HelpContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InstallComponentContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KillContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadIndexInfoContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadTableIndexListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptimizeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptionTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptionValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptionValueListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptionValueNoOptionTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RepairTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ResetOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ResetPersistContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ResetStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RestartContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetCharacterContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetResourceGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowBinaryLogsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowBinlogEventsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCharacterSetContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCharsetContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCollationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowEngineContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowEnginesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowErrorsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowEventsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowFilterContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowFunctionCodeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowFunctionStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowGrantsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowMasterStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowOpenTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowPluginsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowProcedureCodeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowProcedureStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowProcesslistContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowProfilesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowRelaylogEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowReplicaStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowReplicasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowSlaveHostsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowSlaveStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTriggersContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowVariablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowWarningsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowWhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShutdownContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SystemVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TablesOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UninstallComponentContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UninstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UserVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.VariableContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLAlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLBinlogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCheckTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCloneStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateLoadableFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDelimiterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLHelpStatement;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCharacterSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCollationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowEngineStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowEventsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionCodeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowGrantsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowMasterStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOpenTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowPluginsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureCodeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProfilesStatement;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShutdownStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallComponentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.CacheTableIndexSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.CloneActionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.CloneInstanceSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.LoadTableIndexSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.PartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.PartitionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.ResetMasterOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.ResetOptionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.ResetSlaveOptionSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * DAL Statement SQL visitor for MySQL.
 */
@NoArgsConstructor
public final class MySQLDALStatementSQLVisitor extends MySQLStatementSQLVisitor implements DALSQLVisitor, SQLStatementVisitor {
    
    public MySQLDALStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitUninstallPlugin(final UninstallPluginContext ctx) {
        MySQLUninstallPluginStatement result = new MySQLUninstallPluginStatement();
        result.setPluginName(((IdentifierValue) visit(ctx.pluginName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateDatabase(final ShowCreateDatabaseContext ctx) {
        MySQLShowCreateDatabaseStatement result = new MySQLShowCreateDatabaseStatement();
        result.setDatabaseName(((DatabaseSegment) visit(ctx.schemaName())).getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowBinaryLogs(final ShowBinaryLogsContext ctx) {
        return new MySQLShowBinaryLogsStatement();
    }
    
    @Override
    public ASTNode visitShowStatus(final ShowStatusContext ctx) {
        MySQLShowStatusStatement result = new MySQLShowStatusStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateView(final ShowCreateViewContext ctx) {
        return new MySQLShowCreateViewStatement();
    }
    
    @Override
    public ASTNode visitShowEngines(final ShowEnginesContext ctx) {
        return new MySQLShowOtherStatement();
    }
    
    @Override
    public ASTNode visitShowEngine(final ShowEngineContext ctx) {
        MySQLShowEngineStatement result = new MySQLShowEngineStatement();
        result.setEngineName(ctx.engineRef().getText());
        return result;
    }
    
    @Override
    public ASTNode visitShowCharset(final ShowCharsetContext ctx) {
        return new MySQLShowOtherStatement();
    }
    
    @Override
    public ASTNode visitShowCreateEvent(final ShowCreateEventContext ctx) {
        MySQLShowCreateEventStatement result = new MySQLShowCreateEventStatement();
        result.setEventName(((IdentifierValue) visit(ctx.eventName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateFunction(final ShowCreateFunctionContext ctx) {
        MySQLShowCreateFunctionStatement result = new MySQLShowCreateFunctionStatement();
        result.setFunctionName(((FunctionSegment) visit(ctx.functionName())).getFunctionName());
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateProcedure(final ShowCreateProcedureContext ctx) {
        MySQLShowCreateProcedureStatement result = new MySQLShowCreateProcedureStatement();
        result.setProcedureName(((IdentifierValue) visit(ctx.procedureName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowBinlogEvents(final ShowBinlogEventsContext ctx) {
        MySQLShowBinlogEventsStatement result = new MySQLShowBinlogEventsStatement();
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
        MySQLShowErrorsStatement result = new MySQLShowErrorsStatement();
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowWarnings(final ShowWarningsContext ctx) {
        MySQLShowWarningsStatement result = new MySQLShowWarningsStatement();
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
        MySQLResetStatement result = new MySQLResetStatement();
        for (ResetOptionContext each : ctx.resetOption()) {
            if (null != each.MASTER() || null != each.SLAVE()) {
                result.getOptions().add((ResetOptionSegment) (visit(each)));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitResetPersist(final ResetPersistContext ctx) {
        return new MySQLResetPersistStatement(null != ctx.ifExists(), null == ctx.identifier() ? null : new IdentifierValue(ctx.identifier().getText()));
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
        return new MySQLShowReplicasStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRepairTable(final RepairTableContext ctx) {
        MySQLRepairTableStatement result = new MySQLRepairTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        MySQLAnalyzeTableStatement result = new MySQLAnalyzeTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCacheIndex(final CacheIndexContext ctx) {
        MySQLCacheIndexStatement result = new MySQLCacheIndexStatement();
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
        result.setName((IdentifierValue) visit(ctx.identifier()));
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
        MySQLChecksumTableStatement result = new MySQLChecksumTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitFlush(final FlushContext ctx) {
        if (null != ctx.tablesOption()) {
            return visit(ctx.tablesOption());
        }
        return new MySQLFlushStatement();
    }
    
    @Override
    public ASTNode visitTablesOption(final TablesOptionContext ctx) {
        MySQLFlushStatement result = new MySQLFlushStatement();
        result.setFlushTable(true);
        for (TableNameContext each : ctx.tableName()) {
            result.getTables().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitKill(final KillContext ctx) {
        MySQLKillStatement result = new MySQLKillStatement();
        result.setProcesslistId(ctx.IDENTIFIER_().getText());
        return result;
    }
    
    @Override
    public ASTNode visitLoadIndexInfo(final LoadIndexInfoContext ctx) {
        MySQLLoadIndexInfoStatement result = new MySQLLoadIndexInfoStatement();
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
        MySQLInstallPluginStatement result = new MySQLInstallPluginStatement();
        result.setPluginName(((IdentifierValue) visit(ctx.pluginName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitClone(final CloneContext ctx) {
        MySQLCloneStatement result = new MySQLCloneStatement();
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
        MySQLOptimizeTableStatement result = new MySQLOptimizeTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        MySQLUseStatement result = new MySQLUseStatement();
        result.setSchema(((DatabaseSegment) visit(ctx.schemaName())).getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        MySQLExplainStatement result = new MySQLExplainStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
            if (null != ctx.columnRef()) {
                result.setColumnWild((ColumnSegment) visit(ctx.columnRef()));
            } else if (null != ctx.textString()) {
                result.setColumnWild((ColumnSegment) visit(ctx.textString()));
            }
        } else if (null != ctx.explainableStatement()) {
            result.setStatement((SQLStatement) visit(ctx.explainableStatement()));
        } else if (null != ctx.select()) {
            result.setStatement((SQLStatement) visit(ctx.select()));
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
        MySQLShowProcedureCodeStatement result = new MySQLShowProcedureCodeStatement();
        result.setFunction((FunctionSegment) visit(ctx.functionName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowProfile(final ShowProfileContext ctx) {
        MySQLShowProfileStatement result = new MySQLShowProfileStatement();
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowProfiles(final ShowProfilesContext ctx) {
        return new MySQLShowProfilesStatement();
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        MySQLShowDatabasesStatement result = new MySQLShowDatabasesStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowEvents(final ShowEventsContext ctx) {
        MySQLShowEventsStatement result = new MySQLShowEventsStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        MySQLShowTablesStatement result = new MySQLShowTablesStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowTriggers(final ShowTriggersContext ctx) {
        MySQLShowTriggersStatement result = new MySQLShowTriggersStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowWhereClause(final ShowWhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        MySQLShowTableStatusStatement result = new MySQLShowTableStatusStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        MySQLShowColumnsStatement result = new MySQLShowColumnsStatement();
        if (null != ctx.fromTable()) {
            result.setTable(((FromTableSegment) visit(ctx.fromTable())).getTable());
        }
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
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
        MySQLShowIndexStatement result = new MySQLShowIndexStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.fromTable()) {
            result.setTable(((FromTableSegment) visitFromTable(ctx.fromTable())).getTable());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        MySQLShowCreateTableStatement result = new MySQLShowCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowCreateTrigger(final ShowCreateTriggerContext ctx) {
        MySQLShowCreateTriggerStatement result = new MySQLShowCreateTriggerStatement();
        result.setName(((IdentifierValue) visit(ctx.triggerName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShowRelaylogEvent(final ShowRelaylogEventContext ctx) {
        MySQLShowRelayLogEventsStatement result = new MySQLShowRelayLogEventsStatement();
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
        MySQLShowFunctionCodeStatement result = new MySQLShowFunctionCodeStatement();
        result.setFunctionName(((FunctionSegment) visit(ctx.functionName())).getFunctionName());
        return result;
    }
    
    @Override
    public ASTNode visitShowGrants(final ShowGrantsContext ctx) {
        return new MySQLShowGrantsStatement();
    }
    
    @Override
    public ASTNode visitShowMasterStatus(final ShowMasterStatusContext ctx) {
        return new MySQLShowMasterStatusStatement();
    }
    
    @Override
    public ASTNode visitShowSlaveHosts(final ShowSlaveHostsContext ctx) {
        return new MySQLShowSlaveHostsStatement();
    }
    
    @Override
    public ASTNode visitShowReplicaStatus(final ShowReplicaStatusContext ctx) {
        MySQLShowReplicaStatusStatement result = new MySQLShowReplicaStatusStatement();
        if (null != ctx.channelName()) {
            result.setChannel(((IdentifierValue) visit(ctx.channelName())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowSlaveStatus(final ShowSlaveStatusContext ctx) {
        MySQLShowSlaveStatusStatement result = new MySQLShowSlaveStatusStatement();
        if (null != ctx.channelName()) {
            result.setChannel(((IdentifierValue) visit(ctx.channelName())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateResourceGroup(final CreateResourceGroupContext ctx) {
        MySQLCreateResourceGroupStatement result = new MySQLCreateResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitBinlog(final BinlogContext ctx) {
        return new MySQLBinlogStatement(((StringLiteralValue) visit(ctx.stringLiterals())).getValue());
    }
    
    @Override
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowVariables(final ShowVariablesContext ctx) {
        MySQLShowVariablesStatement result = new MySQLShowVariablesStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCharacterSet(final ShowCharacterSetContext ctx) {
        MySQLShowCharacterSetStatement result = new MySQLShowCharacterSetStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowCollation(final ShowCollationContext ctx) {
        MySQLShowCollationStatement result = new MySQLShowCollationStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowFunctionStatus(final ShowFunctionStatusContext ctx) {
        MySQLShowFunctionStatusStatement result = new MySQLShowFunctionStatusStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowProcedureStatus(final ShowProcedureStatusContext ctx) {
        MySQLShowProcedureStatusStatement result = new MySQLShowProcedureStatusStatement();
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowOpenTables(final ShowOpenTablesContext ctx) {
        MySQLShowOpenTablesStatement result = new MySQLShowOpenTablesStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        if (null != ctx.showFilter()) {
            result.setFilter((ShowFilterSegment) visit(ctx.showFilter()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitShowPlugins(final ShowPluginsContext ctx) {
        return new MySQLShowPluginsStatement();
    }
    
    @Override
    public ASTNode visitShowPrivileges(final ShowPrivilegesContext ctx) {
        return new MySQLShowPrivilegesStatement();
    }
    
    @Override
    public ASTNode visitShutdown(final ShutdownContext ctx) {
        return new MySQLShutdownStatement();
    }
    
    @Override
    public ASTNode visitShowProcesslist(final ShowProcesslistContext ctx) {
        return new MySQLShowProcessListStatement();
    }
    
    @Override
    public ASTNode visitShowCreateUser(final ShowCreateUserContext ctx) {
        MySQLShowCreateUserStatement result = new MySQLShowCreateUserStatement();
        result.setName(((IdentifierValue) visit(ctx.username())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        MySQLSetStatement result = new MySQLSetStatement();
        Collection<VariableAssignSegment> variableAssigns = getVariableAssigns(ctx.optionValueList());
        result.getVariableAssigns().addAll(variableAssigns);
        return result;
    }
    
    private Collection<VariableAssignSegment> getVariableAssigns(final OptionValueListContext ctx) {
        Collection<VariableAssignSegment> result = new LinkedList<>();
        if (null == ctx.optionValueNoOptionType()) {
            VariableAssignSegment variableAssign = new VariableAssignSegment();
            variableAssign.setStartIndex(ctx.start.getStartIndex());
            variableAssign.setStopIndex(ctx.setExprOrDefault().stop.getStopIndex());
            VariableSegment variable = new VariableSegment();
            variable.setScope(ctx.optionType().getText());
            variable.setVariable(ctx.internalVariableName().getText());
            variableAssign.setVariable(variable);
            variableAssign.setAssignValue(ctx.setExprOrDefault().getText());
            result.add(variableAssign);
        } else {
            result.add(getVariableAssign(ctx.optionValueNoOptionType()));
        }
        for (OptionValueContext each : ctx.optionValue()) {
            result.add(getVariableAssign(each));
        }
        return result;
    }
    
    private VariableAssignSegment getVariableAssign(final OptionValueNoOptionTypeContext ctx) {
        VariableAssignSegment result = new VariableAssignSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        VariableSegment variable = new VariableSegment();
        if (null != ctx.NAMES()) {
            variable.setVariable("charset");
            result.setVariable(variable);
            result.setAssignValue(ctx.charsetName().getText());
        } else if (null != ctx.internalVariableName()) {
            variable.setVariable(ctx.internalVariableName().getText());
            result.setVariable(variable);
            result.setAssignValue(ctx.setExprOrDefault().getText());
        } else if (null != ctx.userVariable()) {
            variable.setVariable(ctx.userVariable().getText());
            result.setVariable(variable);
            result.setAssignValue(ctx.expr().getText());
        } else if (null != ctx.setSystemVariable()) {
            variable.setVariable(ctx.setSystemVariable().internalVariableName().getText());
            result.setVariable(variable);
            result.setAssignValue(ctx.setExprOrDefault().getText());
            OptionTypeContext optionType = ctx.setSystemVariable().optionType();
            variable.setScope(null != optionType ? optionType.getText() : "SESSION");
        }
        return result;
    }
    
    private VariableAssignSegment getVariableAssign(final OptionValueContext ctx) {
        VariableAssignSegment result = new VariableAssignSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        VariableSegment variable = new VariableSegment();
        if (null != ctx.optionValueNoOptionType()) {
            return getVariableAssign(ctx.optionValueNoOptionType());
        }
        variable.setScope(ctx.optionType().getText());
        variable.setVariable(ctx.internalVariableName().getText());
        result.setVariable(variable);
        result.setAssignValue(ctx.setExprOrDefault().getText());
        return result;
    }
    
    @Override
    public ASTNode visitSetCharacter(final SetCharacterContext ctx) {
        VariableAssignSegment characterSet = new VariableAssignSegment();
        VariableSegment variable = new VariableSegment();
        String variableName = (null != ctx.CHARSET()) ? ctx.CHARSET().getText() : "charset";
        variable.setVariable(variableName);
        characterSet.setVariable(variable);
        String assignValue = (null != ctx.DEFAULT()) ? ctx.DEFAULT().getText() : ctx.charsetName().getText();
        characterSet.setAssignValue(assignValue);
        MySQLSetStatement result = new MySQLSetStatement();
        result.getVariableAssigns().add(characterSet);
        return result;
    }
    
    @Override
    public ASTNode visitVariable(final VariableContext ctx) {
        return super.visitVariable(ctx);
    }
    
    @Override
    public ASTNode visitUserVariable(final UserVariableContext ctx) {
        VariableSegment result = new VariableSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setVariable(ctx.textOrIdentifier().getText());
        return result;
    }
    
    @Override
    public ASTNode visitSystemVariable(final SystemVariableContext ctx) {
        VariableSegment result = new VariableSegment();
        result.setScope(ctx.systemVariableScope.getText());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setVariable(ctx.textOrIdentifier().getText());
        return result;
    }
    
    @Override
    public ASTNode visitFromSchema(final FromSchemaContext ctx) {
        return new FromSchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (DatabaseSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getValue());
    }
    
    @Override
    public ASTNode visitCreateLoadableFunction(final CreateLoadableFunctionContext ctx) {
        return new MySQLCreateLoadableFunctionStatement();
    }
    
    @Override
    public ASTNode visitInstallComponent(final InstallComponentContext ctx) {
        MySQLInstallComponentStatement result = new MySQLInstallComponentStatement();
        List<String> components = new LinkedList<>();
        for (ComponentNameContext each : ctx.componentName()) {
            components.add(((StringLiteralValue) visit(each.string_())).getValue());
        }
        result.getComponents().addAll(components);
        return result;
    }
    
    @Override
    public ASTNode visitUninstallComponent(final UninstallComponentContext ctx) {
        MySQLUninstallComponentStatement result = new MySQLUninstallComponentStatement();
        List<String> components = new LinkedList<>();
        for (ComponentNameContext each : ctx.componentName()) {
            components.add(((StringLiteralValue) visit(each.string_())).getValue());
        }
        result.getComponents().addAll(components);
        return result;
    }
    
    @Override
    public ASTNode visitRestart(final RestartContext ctx) {
        return new MySQLRestartStatement();
    }
    
    @Override
    public ASTNode visitSetResourceGroup(final SetResourceGroupContext ctx) {
        MySQLSetResourceGroupStatement result = new MySQLSetResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCheckTable(final CheckTableContext ctx) {
        MySQLCheckTableStatement result = new MySQLCheckTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropResourceGroup(final DropResourceGroupContext ctx) {
        MySQLDropResourceGroupStatement result = new MySQLDropResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitAlterResourceGroup(final AlterResourceGroupContext ctx) {
        MySQLAlterResourceGroupStatement result = new MySQLAlterResourceGroupStatement();
        result.setGroupName(((IdentifierValue) visit(ctx.groupName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDelimiter(final DelimiterContext ctx) {
        MySQLDelimiterStatement result = new MySQLDelimiterStatement();
        result.setDelimiterName(ctx.delimiterName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitHelp(final HelpContext ctx) {
        MySQLHelpStatement result = new MySQLHelpStatement();
        result.setSearchString(ctx.textOrIdentifier().getText());
        return result;
    }
}
