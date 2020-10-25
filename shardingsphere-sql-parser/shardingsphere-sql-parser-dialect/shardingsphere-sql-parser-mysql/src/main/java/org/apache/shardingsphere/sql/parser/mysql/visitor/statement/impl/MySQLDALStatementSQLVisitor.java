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

import org.apache.shardingsphere.sql.parser.api.visitor.statement.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetCharacterContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.VariableAssignContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CacheIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChecksumTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FlushContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KillContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadIndexInfoContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OptimizeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RepairTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ResetStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowBinaryLogsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowBinlogEventsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowErrorsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowOtherContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowWarningsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UninstallPluginContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.VariableContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowBinlogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * DAL Statement SQL visitor for MySQL.
 */
public final class MySQLDALStatementSQLVisitor extends MySQLStatementSQLVisitor implements DALStatementSQLVisitor {
    
    @Override
    public ASTNode visitUninstallPlugin(final UninstallPluginContext ctx) {
        return new MySQLUninstallPluginStatement();
    }
    
    @Override
    public ASTNode visitShowBinaryLogs(final ShowBinaryLogsContext ctx) {
        return new MySQLShowBinaryLogsStatement();
    }
    
    @Override
    public ASTNode visitShowStatus(final ShowStatusContext ctx) {
        return new MySQLShowStatusStatement();
    }
    
    @Override
    public ASTNode visitShowCreateView(final ShowCreateViewContext ctx) {
        return new MySQLShowCreateViewStatement();
    }
    
    @Override
    public ASTNode visitShowCreateEvent(final ShowCreateEventContext ctx) {
        return new MySQLShowCreateEventStatement();
    }
    
    @Override
    public ASTNode visitShowCreateFunction(final ShowCreateFunctionContext ctx) {
        return new MySQLShowCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitShowCreateProcedure(final ShowCreateProcedureContext ctx) {
        return new MySQLShowCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitShowBinlogEvents(final ShowBinlogEventsContext ctx) {
        return new MySQLShowBinlogStatement();
    }
    
    @Override
    public ASTNode visitShowErrors(final ShowErrorsContext ctx) {
        return new MySQLShowErrorsStatement();
    }
    
    @Override
    public ASTNode visitShowWarnings(final ShowWarningsContext ctx) {
        return new MySQLShowWarningsStatement();
    }
    
    @Override
    public ASTNode visitResetStatement(final ResetStatementContext ctx) {
        return new MySQLResetStatement();
    }
    
    @Override
    public ASTNode visitRepairTable(final RepairTableContext ctx) {
        return new MySQLRepairTableStatement();
    }
    
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        return new MySQLAnalyzeTableStatement();
    }
    
    @Override
    public ASTNode visitCacheIndex(final CacheIndexContext ctx) {
        return new MySQLCacheIndexStatement();
    }
    
    @Override
    public ASTNode visitChecksumTable(final ChecksumTableContext ctx) {
        return new MySQLChecksumTableStatement();
    }
    
    @Override
    public ASTNode visitFlush(final FlushContext ctx) {
        return new MySQLFlushStatement();
    }
    
    @Override
    public ASTNode visitKill(final KillContext ctx) {
        return new MySQLKillStatement();
    }
    
    @Override
    public ASTNode visitLoadIndexInfo(final LoadIndexInfoContext ctx) {
        return new MySQLLoadIndexInfoStatement();
    }
    
    @Override
    public ASTNode visitInstallPlugin(final InstallPluginContext ctx) {
        return new MySQLInstallPluginStatement();
    }
    
    @Override
    public ASTNode visitOptimizeTable(final OptimizeTableContext ctx) {
        return new MySQLOptimizeTableStatement();
    }
    
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        MySQLUseStatement result = new MySQLUseStatement();
        result.setSchema(((IdentifierValue) visit(ctx.schemaName())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        MySQLDescribeStatement result = new MySQLDescribeStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        return new MySQLShowDatabasesStatement();
    }
    
    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        MySQLShowTablesStatement result = new MySQLShowTablesStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        MySQLShowTableStatusStatement result = new MySQLShowTableStatusStatement();
        if (null != ctx.fromSchema()) {
            result.setFromSchema((FromSchemaSegment) visit(ctx.fromSchema()));
        }
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
        return result;
    }
    
    @Override
    public ASTNode visitShowIndex(final ShowIndexContext ctx) {
        MySQLShowIndexStatement result = new MySQLShowIndexStatement();
        if (null != ctx.fromSchema()) {
            SchemaNameContext schemaNameContext = ctx.fromSchema().schemaName();
            // TODO visitSchemaName
            result.setSchema(new SchemaSegment(schemaNameContext.getStart().getStartIndex(), schemaNameContext.getStop().getStopIndex(), (IdentifierValue) visit(schemaNameContext)));
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
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitShowOther(final ShowOtherContext ctx) {
        return new MySQLShowOtherStatement();
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        MySQLSetStatement result = new MySQLSetStatement();
        Collection<VariableAssignSegment> variableAssigns = new LinkedList<>();
        for (VariableAssignContext each : ctx.variableAssign()) {
            variableAssigns.add((VariableAssignSegment) visit(each));
        }
        result.getVariableAssigns().addAll(variableAssigns);
        return result;
    }
    
    @Override
    public ASTNode visitSetName(final SetNameContext ctx) {
        MySQLSetStatement result = new MySQLSetStatement();
        if (null != ctx.characterSetName() || null != ctx.DEFAULT()) {
            VariableAssignSegment characterSet = new VariableAssignSegment();
            VariableSegment variable = new VariableSegment();
            variable.setVariable("charset");
            characterSet.setVariable(variable);
            String assignValue = (null != ctx.DEFAULT()) ? ctx.DEFAULT().getText() : ctx.characterSetName().getText();
            characterSet.setAssignValue(assignValue);
        }
        if (null != ctx.collationName_()) {
            VariableAssignSegment collation = new VariableAssignSegment();
            VariableSegment variable = new VariableSegment();
            variable.setVariable(ctx.COLLATE().getText());
            collation.setVariable(variable);
            collation.setAssignValue(ctx.collationName_().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitSetCharacter(final SetCharacterContext ctx) {
        MySQLSetStatement result = new MySQLSetStatement();
        VariableAssignSegment characterSet = new VariableAssignSegment();
        VariableSegment variable = new VariableSegment();
        String variableName = (null != ctx.CHARSET()) ? ctx.CHARSET().getText() : "charset";
        variable.setVariable(variableName);
        String assignValue = (null != ctx.DEFAULT()) ? ctx.DEFAULT().getText() : ctx.characterSetName().getText();
        characterSet.setAssignValue(assignValue);
        return result;
    }
    
    @Override
    public ASTNode visitVariableAssign(final VariableAssignContext ctx) {
        VariableAssignSegment result = new VariableAssignSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setVariable((VariableSegment) visit(ctx.variable()));
        result.setAssignValue(ctx.setExprOrDefault().getText());
        return result;
    }
    
    @Override
    public ASTNode visitVariable(final VariableContext ctx) {
        VariableSegment result = new VariableSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.scope()) {
            result.setScope(ctx.scope().getText());
        }
        result.setVariable(ctx.internalVariableName().getText());
        return result;
    }

    @Override
    public ASTNode visitFromSchema(final FromSchemaContext ctx) {
        return new FromSchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getValue());
    }
}
