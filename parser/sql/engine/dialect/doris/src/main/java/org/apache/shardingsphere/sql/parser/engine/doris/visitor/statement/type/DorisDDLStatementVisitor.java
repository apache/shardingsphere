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

package org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type;

import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterCatalogContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ResumeJobContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AddColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AddTableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterAlgorithmOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterCheckContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterCommandsModifierContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterConvertContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterEventContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterInstanceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterListContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterListItemContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterLockOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterRenameTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterServerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterTableDropContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterTablespaceInnodbContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterTablespaceNdbContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BeginStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropEncryptKeyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CaseStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ChangeColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CharsetNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CompoundStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateEventContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateLikeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateServerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateTableOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateTableOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DeallocateContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropEventContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropServerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BuildIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CancelBuildIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CancelMaterializedViewTaskContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FieldDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FlowControlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IfStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.JobIdContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyListWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyPartContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyPartWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyPartsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoopStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ModifyColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PlaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ReferenceDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RenameColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RenameIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RenameTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RepeatStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RoutineBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.SimpleStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TableConstraintDefContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TableElementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ValidStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WhileStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RenameRollupContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RenamePartitionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterStoragePolicyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PropertiesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateEncryptKeyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisAlterMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisAlterMVRenameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisAlterMVRefreshContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisAlterMVReplaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisAlterMVSetPropertiesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DropMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisCreateMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisMVColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisMVRefreshTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PauseMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.RefreshMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ResumeMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AlgorithmOption;
import org.apache.shardingsphere.sql.parser.statement.core.enums.LockTableOption;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.charset.CharsetNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.engine.EngineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.RenamePartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.policy.PolicyNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.RenameRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.RollupSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.tablespace.TablespaceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.ViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.MaterializedViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateEncryptKeyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeallocateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ExecuteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.catalog.AlterCatalogStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.BuildIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CancelBuildIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.AlterServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.CreateServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.DropServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.DropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropEncryptKeyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterStoragePolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisResumeJobStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisPauseMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisResumeMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisRefreshMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCancelMaterializedViewTaskStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.event.MySQLAlterEventStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.event.MySQLCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.event.MySQLDropEventStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.instance.MySQLAlterInstanceStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.logfile.MySQLAlterLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.logfile.MySQLCreateLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.logfile.MySQLDropLogfileGroupStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * DDL statement visitor for Doris.
 */
public final class DorisDDLStatementVisitor extends DorisStatementVisitor implements DDLStatementVisitor {
    
    public DorisDDLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        CreateViewStatement result = new CreateViewStatement(getDatabaseType());
        result.setReplaceView(null != ctx.REPLACE());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitPauseMaterializedView(final PauseMaterializedViewContext ctx) {
        DorisPauseMaterializedViewStatement result = new DorisPauseMaterializedViewStatement(getDatabaseType());
        result.setMaterializedViewName((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitResumeMaterializedView(final ResumeMaterializedViewContext ctx) {
        DorisResumeMaterializedViewStatement result = new DorisResumeMaterializedViewStatement(getDatabaseType());
        result.setMaterializedViewName((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropMaterializedView(final DropMaterializedViewContext ctx) {
        DorisDropMaterializedViewStatement result = new DorisDropMaterializedViewStatement(getDatabaseType());
        result.setMaterializedView((SimpleTableSegment) visit(ctx.tableName(0)));
        result.setTable((SimpleTableSegment) visit(ctx.tableName(1)));
        result.setIfExists(null != ctx.ifExists());
        return result;
    }
    
    @Override
    public ASTNode visitRefreshMaterializedView(final RefreshMaterializedViewContext ctx) {
        DorisRefreshMaterializedViewStatement result = new DorisRefreshMaterializedViewStatement(getDatabaseType());
        result.setMaterializedViewName((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.refreshType().partitionSpec()) {
            ctx.refreshType().partitionSpec().identifier().forEach(each -> result.getPartitionNames().add(each.getText()));
        } else if (null != ctx.refreshType().COMPLETE()) {
            result.setRefreshType("COMPLETE");
        } else if (null != ctx.refreshType().AUTO()) {
            result.setRefreshType("AUTO");
        }
        return result;
    }
    
    @Override
    public ASTNode visitCancelMaterializedViewTask(final CancelMaterializedViewTaskContext ctx) {
        DorisCancelMaterializedViewTaskStatement result = new DorisCancelMaterializedViewTaskStatement(getDatabaseType());
        result.setTaskId(ctx.taskId().getText());
        result.setMaterializedView((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        AlterViewStatement result = new AlterViewStatement(getDatabaseType());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        if (null != ctx.columnNames()) {
            CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(ctx.columnNames());
            for (ColumnSegment each : columns.getValue()) {
                result.getColumns().add(new ViewColumnSegment(each.getStartIndex(), each.getStopIndex(), each, null));
            }
        }
        if (null != ctx.viewColumnDefinitions()) {
            CollectionValue<ViewColumnSegment> columns = (CollectionValue<ViewColumnSegment>) visit(ctx.viewColumnDefinitions());
            result.getColumns().addAll(columns.getValue());
        }
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        DropViewStatement result = new DropViewStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.viewNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new CreateDatabaseStatement(getDatabaseType(), new IdentifierValue(ctx.databaseName().getText()).getValue(), null != ctx.ifNotExists());
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        AlterDatabaseStatement result = new AlterDatabaseStatement(getDatabaseType());
        if (null != ctx.databaseName()) {
            result.setDatabaseName(new IdentifierValue(ctx.databaseName().getText()).getValue());
        }
        if (null != ctx.RENAME() && null != ctx.identifier()) {
            result.setRenameDatabaseName(new IdentifierValue(ctx.identifier().getText()).getValue());
        }
        if (null != ctx.QUOTA() && null != ctx.NUMBER_()) {
            if (null != ctx.DATA()) {
                result.setQuotaType("DATA");
            } else if (null != ctx.REPLICA()) {
                result.setQuotaType("REPLICA");
            } else if (null != ctx.TRANSACTION()) {
                result.setQuotaType("TRANSACTION");
            }
            result.setQuotaValue(Long.parseLong(ctx.NUMBER_().getText()));
        }
        if (null != ctx.PROPERTIES() && null != ctx.properties()) {
            PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.properties().getStart().getStartIndex(), ctx.properties().getStop().getStopIndex());
            for (int i = 0; i < ctx.properties().property().size(); i++) {
                String key = getPropertyKey(ctx.properties().property(i));
                String value = SQLUtils.getExactlyValue(ctx.properties().property(i).literals().getText());
                PropertySegment propertySegment = new PropertySegment(ctx.properties().property(i).getStart().getStartIndex(), ctx.properties().property(i).getStop().getStopIndex(), key, value);
                propertiesSegment.getProperties().add(propertySegment);
            }
            result.setProperties(propertiesSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(getDatabaseType(), new IdentifierValue(ctx.databaseName().getText()).getValue(), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitAlterCatalog(final AlterCatalogContext ctx) {
        AlterCatalogStatement result = new AlterCatalogStatement(getDatabaseType());
        result.setCatalogName(new IdentifierValue(ctx.catalogName().getText()).getValue());
        if (null != ctx.RENAME()) {
            result.setNewCatalogName(new IdentifierValue(ctx.identifier().getText()).getValue());
        }
        if (null != ctx.PROPERTIES()) {
            for (int i = 0; i < ctx.properties().property().size(); i++) {
                String key = null != ctx.properties().property(i).SINGLE_QUOTED_TEXT()
                        ? SQLUtils.getExactlyValue(ctx.properties().property(i).SINGLE_QUOTED_TEXT().getText())
                        : SQLUtils.getExactlyValue(ctx.properties().property(i).identifier().getText());
                String value = SQLUtils.getExactlyValue(ctx.properties().property(i).literals().getText());
                result.getProperties().put(key, value);
            }
        }
        if (null != ctx.COMMENT()) {
            result.setComment(SQLUtils.getExactlyValue(ctx.string_().getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitResumeJob(final ResumeJobContext ctx) {
        String jobName = SQLUtils.getExactlyValue(ctx.stringLiterals().getText());
        return new DorisResumeJobStatement(getDatabaseType(), jobName);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setIfNotExists(null != ctx.ifNotExists());
        if (null != ctx.createDefinitionClause()) {
            CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) visit(ctx.createDefinitionClause());
            for (CreateDefinitionSegment each : createDefinitions.getValue()) {
                if (each instanceof ColumnDefinitionSegment) {
                    result.getColumnDefinitions().add((ColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getConstraintDefinitions().add((ConstraintDefinitionSegment) each);
                }
            }
        }
        if (null != ctx.createLikeClause()) {
            result.setLikeTable((SimpleTableSegment) visit(ctx.createLikeClause()));
        }
        if (null != ctx.createTableOptions()) {
            result.setCreateTableOption((CreateTableOptionSegment) visit(ctx.createTableOptions()));
        }
        // DORIS ADDED BEGIN
        if (null != ctx.duplicateAsQueryExpression()) {
            result.setSelectStatement((SelectStatement) visit(ctx.duplicateAsQueryExpression().select()));
        }
        // DORIS ADDED END
        return result;
    }
    
    @Override
    public ASTNode visitCreateTableOptions(final CreateTableOptionsContext ctx) {
        CreateTableOptionSegment result = new CreateTableOptionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (CreateTableOptionContext each : ctx.createTableOption()) {
            if (null != each.engineRef()) {
                result.setEngine((EngineSegment) visit(each.engineRef()));
            } else if (null != each.COMMENT()) {
                result.setCommentSegment(new CommentSegment(each.string_().getText(), each.string_().getStart().getStartIndex(), each.string_().getStop().getStopIndex()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        for (TableElementContext each : ctx.tableElementList().tableElement()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.tableConstraintDef()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.tableConstraintDef()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateLikeClause(final CreateLikeClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null == ctx.alterTableActions() || null == ctx.alterTableActions().alterCommandList() || null == ctx.alterTableActions().alterCommandList().alterList()) {
            return result;
        }
        for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterTableActions().alterCommandList().alterList())).getValue()) {
            setAlterDefinition(result, each);
        }
        return result;
    }
    
    private void setAlterDefinition(final AlterTableStatement alterTableStatement, final AlterDefinitionSegment alterDefinitionSegment) {
        if (alterDefinitionSegment instanceof AddColumnDefinitionSegment) {
            alterTableStatement.getAddColumnDefinitions().add((AddColumnDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof ModifyColumnDefinitionSegment) {
            alterTableStatement.getModifyColumnDefinitions().add((ModifyColumnDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof ChangeColumnDefinitionSegment) {
            alterTableStatement.getChangeColumnDefinitions().add((ChangeColumnDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof DropColumnDefinitionSegment) {
            alterTableStatement.getDropColumnDefinitions().add((DropColumnDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof AddConstraintDefinitionSegment) {
            alterTableStatement.getAddConstraintDefinitions().add((AddConstraintDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof DropConstraintDefinitionSegment) {
            alterTableStatement.getDropConstraintDefinitions().add((DropConstraintDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof RenameTableDefinitionSegment) {
            alterTableStatement.setRenameTable(((RenameTableDefinitionSegment) alterDefinitionSegment).getRenameTable());
        } else if (alterDefinitionSegment instanceof ConvertTableDefinitionSegment) {
            alterTableStatement.setConvertTableDefinition((ConvertTableDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof DropIndexDefinitionSegment) {
            alterTableStatement.getDropIndexDefinitions().add((DropIndexDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof RenameIndexDefinitionSegment) {
            alterTableStatement.getRenameIndexDefinitions().add((RenameIndexDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof RenameColumnSegment) {
            alterTableStatement.getRenameColumnDefinitions().add((RenameColumnSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof RenameRollupDefinitionSegment) {
            alterTableStatement.getRenameRollupDefinitions().add((RenameRollupDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof RenamePartitionDefinitionSegment) {
            alterTableStatement.getRenamePartitionDefinitions().add((RenamePartitionDefinitionSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof AlgorithmTypeSegment) {
            alterTableStatement.setAlgorithmSegment((AlgorithmTypeSegment) alterDefinitionSegment);
        } else if (alterDefinitionSegment instanceof LockTableSegment) {
            alterTableStatement.setLockTableSegment((LockTableSegment) alterDefinitionSegment);
        }
    }
    
    private ColumnDefinitionSegment generateColumnDefinitionSegment(final ColumnSegment column, final FieldDefinitionContext ctx) {
        DataTypeSegment dataTypeSegment = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = ctx.columnAttribute().stream().anyMatch(each -> null != each.KEY() && null == each.UNIQUE());
        boolean isAutoIncrement = ctx.columnAttribute().stream().anyMatch(each -> null != each.AUTO_INCREMENT());
        // TODO parse not null
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(column.getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, isPrimaryKey, false, getText(ctx));
        result.setAutoIncrement(isAutoIncrement);
        return result;
    }
    
    private String getText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
    
    @Override
    public ASTNode visitAlterConstraint(final AlterConstraintContext ctx) {
        return new ModifyConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitAlterList(final AlterListContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (ctx.alterListItem().isEmpty()) {
            return result;
        }
        result.getValue().addAll(getAlterDefinitionSegments(ctx));
        for (AlterCommandsModifierContext each : ctx.alterCommandsModifier()) {
            if (null != each.alterAlgorithmOption()) {
                result.getValue().add((AlgorithmTypeSegment) visit(each));
            } else if (null != each.alterLockOption()) {
                result.getValue().add((LockTableSegment) visit(each));
            }
        }
        return result;
    }
    
    private Collection<AlterDefinitionSegment> getAlterDefinitionSegments(final AlterListContext ctx) {
        Collection<AlterDefinitionSegment> result = new LinkedList<>();
        for (AlterListItemContext each : ctx.alterListItem()) {
            getAlterDefinitionSegment(ctx, each).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<AlterDefinitionSegment> getAlterDefinitionSegment(final AlterListContext alterListContext, final AlterListItemContext alterListItemContext) {
        if (alterListItemContext instanceof AddColumnContext) {
            return Optional.of((AddColumnDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof AlterConstraintContext || alterListItemContext instanceof AlterCheckContext) {
            return Optional.of((AlterDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof ChangeColumnContext) {
            return Optional.of(generateModifyColumnDefinitionSegment((ChangeColumnContext) alterListItemContext));
        }
        if (alterListItemContext instanceof ModifyColumnContext) {
            return Optional.of(generateModifyColumnDefinitionSegment((ModifyColumnContext) alterListItemContext));
        }
        if (alterListItemContext instanceof AlterTableDropContext) {
            return getDropItemDefinitionSegment(alterListContext, (AlterTableDropContext) alterListItemContext);
        }
        if (alterListItemContext instanceof AddTableConstraintContext) {
            return Optional.of((AddConstraintDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof AlterRenameTableContext) {
            return Optional.of((RenameTableDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof AlterConvertContext) {
            return Optional.of((ConvertTableDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof RenameColumnContext) {
            return Optional.of((RenameColumnSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof RenameIndexContext) {
            return Optional.of((RenameIndexDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof RenameRollupContext) {
            return Optional.of((RenameRollupDefinitionSegment) visit(alterListItemContext));
        }
        if (alterListItemContext instanceof RenamePartitionContext) {
            return Optional.of((RenamePartitionDefinitionSegment) visit(alterListItemContext));
        }
        return Optional.empty();
    }
    
    private Optional<AlterDefinitionSegment> getDropItemDefinitionSegment(final AlterListContext alterListContext, final AlterTableDropContext alterTableDrop) {
        if (null != alterTableDrop.CHECK() || null != alterTableDrop.CONSTRAINT()) {
            ConstraintSegment constraint = new ConstraintSegment(alterTableDrop.identifier().getStart().getStartIndex(), alterTableDrop.identifier().getStop().getStopIndex(),
                    (IdentifierValue) visit(alterTableDrop.identifier()));
            return Optional.of(new DropConstraintDefinitionSegment(alterListContext.getStart().getStartIndex(), alterListContext.getStop().getStopIndex(), constraint));
        }
        if (null == alterTableDrop.KEY() && null == alterTableDrop.keyOrIndex()) {
            ColumnSegment column = new ColumnSegment(alterTableDrop.columnInternalRef.start.getStartIndex(), alterTableDrop.columnInternalRef.stop.getStopIndex(),
                    (IdentifierValue) visit(alterTableDrop.columnInternalRef));
            return Optional.of(new DropColumnDefinitionSegment(alterTableDrop.getStart().getStartIndex(), alterTableDrop.getStop().getStopIndex(), Collections.singleton(column)));
        }
        if (null != alterTableDrop.keyOrIndex()) {
            return Optional.of(
                    new DropIndexDefinitionSegment(alterListContext.getStart().getStartIndex(), alterListContext.getStop().getStopIndex(), (IndexSegment) visit(alterTableDrop.indexName())));
        }
        return Optional.empty();
    }
    
    @Override
    public ASTNode visitAlterAlgorithmOption(final AlterAlgorithmOptionContext ctx) {
        AlgorithmOption algorithmOption = null;
        if (null != ctx.INSTANT()) {
            algorithmOption = AlgorithmOption.INSTANT;
        } else if (null != ctx.DEFAULT()) {
            algorithmOption = AlgorithmOption.DEFAULT;
        } else if (null != ctx.INPLACE()) {
            algorithmOption = AlgorithmOption.INPLACE;
        } else if (null != ctx.COPY()) {
            algorithmOption = AlgorithmOption.COPY;
        }
        return new AlgorithmTypeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), algorithmOption);
    }
    
    @Override
    public ASTNode visitAlterLockOption(final AlterLockOptionContext ctx) {
        LockTableOption lockOption = null;
        if (null != ctx.DEFAULT()) {
            lockOption = LockTableOption.DEFAULT;
        } else if (null != ctx.NONE()) {
            lockOption = LockTableOption.NONE;
        } else if (null != ctx.SHARED()) {
            lockOption = LockTableOption.SHARED;
        } else if (null != ctx.EXCLUSIVE()) {
            lockOption = LockTableOption.EXCLUSIVE;
        }
        return new LockTableSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), lockOption);
    }
    
    @Override
    public ASTNode visitAlterConvert(final AlterConvertContext ctx) {
        ConvertTableDefinitionSegment result = new ConvertTableDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (CharsetNameSegment) visit(ctx.charsetName()));
        if (null != ctx.collateClause()) {
            result.setCollateValue((SimpleExpressionSegment) visit(ctx.collateClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCharsetName(final CharsetNameContext ctx) {
        return new CharsetNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitAddTableConstraint(final AddTableConstraintContext ctx) {
        return new AddConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintDefinitionSegment) visit(ctx.tableConstraintDef()));
    }
    
    @Override
    public ASTNode visitAlterCheck(final AlterCheckContext ctx) {
        return new ModifyConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitAlterRenameTable(final AlterRenameTableContext ctx) {
        RenameTableDefinitionSegment result = new RenameTableDefinitionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setRenameTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitRenameTable(final RenameTableContext ctx) {
        Collection<RenameTableDefinitionSegment> renameTables = new LinkedList<>();
        for (int i = 0, len = ctx.tableName().size(); i < len; i += 2) {
            TableNameContext tableName = ctx.tableName(i);
            TableNameContext renameTableName = ctx.tableName(i + 1);
            renameTables.add(createRenameTableDefinitionSegment(tableName, renameTableName));
        }
        return new RenameTableStatement(getDatabaseType(), renameTables);
    }
    
    private RenameTableDefinitionSegment createRenameTableDefinitionSegment(final TableNameContext tableName, final TableNameContext renameTableName) {
        RenameTableDefinitionSegment result = new RenameTableDefinitionSegment(tableName.start.getStartIndex(), renameTableName.stop.getStopIndex());
        result.setTable((SimpleTableSegment) visit(tableName));
        result.setRenameTable((SimpleTableSegment) visit(renameTableName));
        return result;
    }
    
    private ModifyColumnDefinitionSegment generateModifyColumnDefinitionSegment(final ModifyColumnContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.columnInternalRef.start.getStartIndex(), ctx.columnInternalRef.stop.getStopIndex(), (IdentifierValue) visit(ctx.columnInternalRef));
        ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), generateColumnDefinitionSegment(column, ctx.fieldDefinition()));
        if (null != ctx.place()) {
            result.setColumnPosition((ColumnPositionSegment) visit(ctx.place()));
        }
        return result;
    }
    
    private ChangeColumnDefinitionSegment generateModifyColumnDefinitionSegment(final ChangeColumnContext ctx) {
        ChangeColumnDefinitionSegment result = new ChangeColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnDefinitionSegment) visit(ctx.columnDefinition()));
        result.setPreviousColumn(new ColumnSegment(ctx.columnInternalRef.getStart().getStartIndex(), ctx.columnInternalRef.getStop().getStopIndex(),
                new IdentifierValue(ctx.columnInternalRef.getText())));
        if (null != ctx.place()) {
            result.setColumnPosition((ColumnPositionSegment) visit(ctx.place()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumn(final AddColumnContext ctx) {
        Collection<ColumnDefinitionSegment> columnDefinitions = new LinkedList<>();
        if (null != ctx.columnDefinition()) {
            columnDefinitions.add((ColumnDefinitionSegment) visit(ctx.columnDefinition()));
        }
        if (null != ctx.tableElementList()) {
            for (TableElementContext each : ctx.tableElementList().tableElement()) {
                if (null != each.columnDefinition()) {
                    columnDefinitions.add((ColumnDefinitionSegment) visit(each.columnDefinition()));
                }
            }
        }
        AddColumnDefinitionSegment result = new AddColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinitions);
        if (null != ctx.place()) {
            Preconditions.checkState(1 == columnDefinitions.size());
            result.setColumnPosition((ColumnPositionSegment) visit(ctx.place()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRenameColumn(final RenameColumnContext ctx) {
        ColumnSegment oldColumnSegment = (ColumnSegment) visit(ctx.oldColumn());
        ColumnSegment newColumnSegment = (ColumnSegment) visit(ctx.newColumn());
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), oldColumnSegment, newColumnSegment);
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.column_name.start.getStartIndex(), ctx.column_name.stop.getStopIndex(), (IdentifierValue) visit(ctx.column_name));
        DataTypeSegment dataTypeSegment = (DataTypeSegment) visit(ctx.fieldDefinition().dataType());
        boolean isPrimaryKey = ctx.fieldDefinition().columnAttribute().stream().anyMatch(each -> null != each.KEY() && null == each.UNIQUE());
        boolean isAutoIncrement = ctx.fieldDefinition().columnAttribute().stream().anyMatch(each -> null != each.AUTO_INCREMENT());
        boolean isNotNull = ctx.fieldDefinition().columnAttribute().stream().anyMatch(each -> null != each.NOT() && null != each.NULL());
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, isPrimaryKey, isNotNull, getText(ctx));
        result.getReferencedTables().addAll(getReferencedTables(ctx));
        result.setAutoIncrement(isAutoIncrement);
        return result;
    }
    
    private Collection<SimpleTableSegment> getReferencedTables(final ColumnDefinitionContext ctx) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != ctx.referenceDefinition()) {
            result.add((SimpleTableSegment) visit(ctx.referenceDefinition()));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitTableConstraintDef(final TableConstraintDefContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintClause() && null != ctx.constraintClause().constraintName()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintClause().constraintName()));
        }
        if (null != ctx.KEY() && null != ctx.PRIMARY()) {
            result.setPrimaryKey(true);
            result.getPrimaryKeyColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
            return result;
        }
        if (null != ctx.FOREIGN()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.referenceDefinition()));
            return result;
        }
        if (null != ctx.UNIQUE()) {
            result.setUniqueKey(true);
            result.getIndexColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
            if (null != ctx.indexName()) {
                result.setIndexName((IndexSegment) visit(ctx.indexName()));
            }
            return result;
        }
        if (null != ctx.checkConstraint()) {
            return result;
        }
        result.getIndexColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
        if (null != ctx.indexName()) {
            result.setIndexName((IndexSegment) visit(ctx.indexName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitKeyListWithExpression(final KeyListWithExpressionContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (KeyPartWithExpressionContext each : ctx.keyPartWithExpression()) {
            if (null != each.keyPart()) {
                result.getValue().add((ColumnSegment) visit(each.keyPart().columnName()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitReferenceDefinition(final ReferenceDefinitionContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitPlace(final PlaceContext ctx) {
        ColumnSegment columnName = null;
        if (null != ctx.columnName()) {
            columnName = (ColumnSegment) visit(ctx.columnName());
        }
        return null == ctx.columnName()
                ? new ColumnFirstPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName)
                : new ColumnAfterPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        return new TruncateStatement(getDatabaseType(), Collections.singleton((SimpleTableSegment) visit(ctx.tableName())));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setIfNotExists(null != ctx.ifNotExists());
        IndexNameSegment indexName = new IndexNameSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), new IdentifierValue(ctx.indexName().getText()));
        result.setIndex(new IndexSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), indexName));
        result.getColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
        if (null != ctx.dorisIndexTypeClause()) {
            result.setIndexType(ctx.dorisIndexTypeClause().getStop().getText());
        }
        if (null != ctx.propertiesClause()) {
            result.setProperties(extractPropertiesSegment(ctx.propertiesClause()));
        }
        if (null != ctx.commentClause() && null != ctx.commentClause().literals()) {
            String commentText = SQLUtils.getExactlyValue(ctx.commentClause().literals().getText());
            result.setComment(commentText);
            CommentSegment commentSegment = new CommentSegment(commentText, ctx.commentClause().literals().getStart().getStartIndex(), ctx.commentClause().literals().getStop().getStopIndex());
            result.getComments().add(commentSegment);
        }
        if (null != ctx.algorithmOptionAndLockOption()) {
            if (null != ctx.algorithmOptionAndLockOption().alterAlgorithmOption()) {
                result.setAlgorithmType((AlgorithmTypeSegment) visit(ctx.algorithmOptionAndLockOption().alterAlgorithmOption()));
            }
            if (null != ctx.algorithmOptionAndLockOption().alterLockOption()) {
                result.setLockTable((LockTableSegment) visit(ctx.algorithmOptionAndLockOption().alterLockOption()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.setSimpleTable((SimpleTableSegment) visit(ctx.tableName()));
        IndexNameSegment indexName = new IndexNameSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), new IdentifierValue(ctx.indexName().getText()));
        result.getIndexes().add(new IndexSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), indexName));
        if (null != ctx.algorithmOptionAndLockOption()) {
            if (null != ctx.algorithmOptionAndLockOption().alterAlgorithmOption()) {
                result.setAlgorithmType((AlgorithmTypeSegment) visit(ctx.algorithmOptionAndLockOption().alterAlgorithmOption()));
            }
            if (null != ctx.algorithmOptionAndLockOption().alterLockOption()) {
                result.setLockTable((LockTableSegment) visit(ctx.algorithmOptionAndLockOption().alterLockOption()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitRenameIndex(final RenameIndexContext ctx) {
        IndexSegment indexNameSegment = (IndexSegment) visitIndexName(ctx.indexName(0));
        IndexSegment renameIndexName = (IndexSegment) visitIndexName(ctx.indexName(1));
        return new RenameIndexDefinitionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), indexNameSegment, renameIndexName);
    }
    
    @Override
    public ASTNode visitBuildIndex(final BuildIndexContext ctx) {
        BuildIndexStatement result = new BuildIndexStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        IndexNameSegment indexName = new IndexNameSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), new IdentifierValue(ctx.indexName().getText()));
        result.setIndex(new IndexSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), indexName));
        if (null != ctx.partitionNames()) {
            for (IdentifierContext each : ctx.partitionNames().identifier()) {
                PartitionSegment partitionSegment = new PartitionSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), (IdentifierValue) visit(each));
                result.getPartitions().add(partitionSegment);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCancelBuildIndex(final CancelBuildIndexContext ctx) {
        CancelBuildIndexStatement result = new CancelBuildIndexStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.jobIdList()) {
            for (JobIdContext each : ctx.jobIdList().jobId()) {
                result.getJobIds().add(each.getText());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitRenameRollup(final RenameRollupContext ctx) {
        RollupSegment oldRollupSegment = new RollupSegment(ctx.oldRollupName.getStart().getStartIndex(), ctx.oldRollupName.getStop().getStopIndex(),
                (IdentifierValue) visit(ctx.oldRollupName));
        RollupSegment newRollupSegment = new RollupSegment(ctx.newRollupName.getStart().getStartIndex(), ctx.newRollupName.getStop().getStopIndex(),
                (IdentifierValue) visit(ctx.newRollupName));
        return new RenameRollupDefinitionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), oldRollupSegment, newRollupSegment);
    }
    
    @Override
    public ASTNode visitRenamePartition(final RenamePartitionContext ctx) {
        PartitionSegment oldPartitionSegment = new PartitionSegment(ctx.oldPartitionName.getStart().getStartIndex(), ctx.oldPartitionName.getStop().getStopIndex(),
                (IdentifierValue) visit(ctx.oldPartitionName));
        PartitionSegment newPartitionSegment = new PartitionSegment(ctx.newPartitionName.getStart().getStartIndex(), ctx.newPartitionName.getStop().getStopIndex(),
                (IdentifierValue) visit(ctx.newPartitionName));
        return new RenamePartitionDefinitionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), oldPartitionSegment, newPartitionSegment);
    }
    
    @Override
    public ASTNode visitAlterStoragePolicy(final AlterStoragePolicyContext ctx) {
        IdentifierValue identifier = (IdentifierValue) visit(ctx.identifier());
        PolicyNameSegment policyNameSegment = new PolicyNameSegment(ctx.identifier().getStart().getStartIndex(), ctx.identifier().getStop().getStopIndex(), identifier);
        PropertiesSegment propertiesSegment = extractPropertiesSegment(ctx.propertiesClause());
        return new DorisAlterStoragePolicyStatement(getDatabaseType(), policyNameSegment, propertiesSegment);
    }
    
    private PropertiesSegment extractPropertiesSegment(final PropertiesClauseContext ctx) {
        PropertiesSegment result = new PropertiesSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (PropertyContext each : ctx.properties().property()) {
            String key = getPropertyKey(each);
            String value = getPropertyValue(each);
            PropertySegment propertySegment = new PropertySegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), key, value);
            result.getProperties().add(propertySegment);
        }
        return result;
    }
    
    private String getPropertyValue(final PropertyContext ctx) {
        return SQLUtils.getExactlyValue(ctx.literals().getText());
    }
    
    @Override
    public ASTNode visitKeyParts(final KeyPartsContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        List<KeyPartContext> keyParts = ctx.keyPart();
        for (KeyPartContext each : keyParts) {
            if (null != each.columnName()) {
                result.getValue().add((ColumnSegment) visit(each.columnName()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        CreateProcedureStatement result = new CreateProcedureStatement(getDatabaseType());
        result.setProcedureName((FunctionNameSegment) visit(ctx.functionName()));
        result.setRoutineBody((RoutineBodySegment) visit(ctx.routineBody()));
        return result;
    }
    
    @Override
    public ASTNode visitFunctionName(final FunctionNameContext ctx) {
        FunctionNameSegment result = new FunctionNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        if (null != ctx.owner()) {
            result.setOwner((OwnerSegment) visit(ctx.owner()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new AlterProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        DorisCreateFunctionStatement result = new DorisCreateFunctionStatement(getDatabaseType());
        result.setFunctionName((FunctionNameSegment) visit(ctx.functionName()));
        if (null != ctx.routineBody()) {
            int paramIndex = 0;
            for (int i = 0; i < ctx.dataType().size(); i++) {
                DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType(i));
                if (i == ctx.dataType().size() - 1 && null != ctx.RETURNS()) {
                    result.setReturnType(dataType);
                } else if (paramIndex < ctx.identifier().size()) {
                    IdentifierValue paramName = (IdentifierValue) visit(ctx.identifier(paramIndex));
                    result.getNamedParameters().put(paramName, dataType);
                    paramIndex++;
                }
            }
            result.setRoutineBody((RoutineBodySegment) visit(ctx.routineBody()));
        } else {
            if (null != ctx.GLOBAL()) {
                result.setGlobal(true);
            }
            if (null != ctx.AGGREGATE()) {
                result.setFunctionType(DorisCreateFunctionStatement.FunctionType.AGGREGATE);
            } else if (null != ctx.TABLES()) {
                result.setFunctionType(DorisCreateFunctionStatement.FunctionType.TABLES);
            } else if (null != ctx.ALIAS()) {
                result.setFunctionType(DorisCreateFunctionStatement.FunctionType.ALIAS);
            }
            int dataTypeCount = ctx.dataType().size();
            int parameterCount = dataTypeCount;
            if (null != ctx.INTERMEDIATE()) {
                parameterCount--;
            }
            if (null != ctx.RETURNS()) {
                parameterCount--;
            }
            for (int i = 0; i < parameterCount; i++) {
                result.getParameterDataTypes().add((DataTypeSegment) visit(ctx.dataType(i)));
            }
            if (null != ctx.RETURNS()) {
                result.setReturnType((DataTypeSegment) visit(ctx.dataType(parameterCount)));
            }
            if (null != ctx.INTERMEDIATE()) {
                int intermediateTypeIndex = dataTypeCount - 1;
                result.setIntermediateType((DataTypeSegment) visit(ctx.dataType(intermediateTypeIndex)));
            }
            if (null != ctx.PARAMETER()) {
                for (int i = 0; i < ctx.identifier().size(); i++) {
                    result.getWithParameters().add((IdentifierValue) visit(ctx.identifier(i)));
                }
            }
            if (null != ctx.expr()) {
                result.setAliasExpression((ExpressionSegment) visit(ctx.expr()));
            }
            if (null != ctx.PROPERTIES()) {
                fillCreateFunctionProperties(ctx, result);
            }
        }
        return result;
    }
    
    private void fillCreateFunctionProperties(final CreateFunctionContext ctx, final DorisCreateFunctionStatement statement) {
        for (int i = 0; i < ctx.properties().property().size(); i++) {
            String key = getPropertyKey(ctx.properties().property(i));
            String value = SQLUtils.getExactlyValue(ctx.properties().property(i).literals().getText());
            statement.getProperties().put(key, value);
        }
    }
    
    private String getPropertyKey(final PropertyContext property) {
        if (null != property.SINGLE_QUOTED_TEXT()) {
            return SQLUtils.getExactlyValue(property.SINGLE_QUOTED_TEXT().getText());
        }
        if (null != property.DOUBLE_QUOTED_TEXT()) {
            return SQLUtils.getExactlyValue(property.DOUBLE_QUOTED_TEXT().getText());
        }
        return SQLUtils.getExactlyValue(property.identifier().getText());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRoutineBody(final RoutineBodyContext ctx) {
        RoutineBodySegment result = new RoutineBodySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        CollectionValue<ValidStatementSegment> validStatements = null == ctx.simpleStatement()
                ? (CollectionValue<ValidStatementSegment>) visit(ctx.compoundStatement())
                : (CollectionValue<ValidStatementSegment>) visit(ctx.simpleStatement());
        result.getValidStatements().addAll(validStatements.getValue());
        return result;
    }
    
    @Override
    public ASTNode visitSimpleStatement(final SimpleStatementContext ctx) {
        return visit(ctx.validStatement());
    }
    
    @Override
    public ASTNode visitCompoundStatement(final CompoundStatementContext ctx) {
        return visit(ctx.beginStatement());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitBeginStatement(final BeginStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        for (ValidStatementContext each : ctx.validStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitValidStatement(final ValidStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        ValidStatementSegment validStatement = createValidStatementSegment(ctx);
        if (null != validStatement.getSqlStatement()) {
            result.getValue().add(validStatement);
        }
        if (null != ctx.beginStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.beginStatement()));
        }
        if (null != ctx.flowControlStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.flowControlStatement()));
        }
        return result;
    }
    
    private ValidStatementSegment createValidStatementSegment(final ValidStatementContext ctx) {
        ValidStatementSegment result = new ValidStatementSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        SQLStatement sqlStatement = null;
        if (null != ctx.createTable()) {
            sqlStatement = (CreateTableStatement) visit(ctx.createTable());
        } else if (null != ctx.alterTable()) {
            sqlStatement = (AlterTableStatement) visit(ctx.alterTable());
        } else if (null != ctx.dropTable()) {
            sqlStatement = (DropTableStatement) visit(ctx.dropTable());
        } else if (null != ctx.truncateTable()) {
            sqlStatement = (TruncateStatement) visit(ctx.truncateTable());
        } else if (null != ctx.insert()) {
            sqlStatement = (InsertStatement) visit(ctx.insert());
        } else if (null != ctx.replace()) {
            sqlStatement = (InsertStatement) visit(ctx.replace());
        } else if (null != ctx.update()) {
            sqlStatement = (UpdateStatement) visit(ctx.update());
        } else if (null != ctx.delete()) {
            sqlStatement = (DeleteStatement) visit(ctx.delete());
        } else if (null != ctx.select()) {
            sqlStatement = (SelectStatement) visit(ctx.select());
        }
        result.setSqlStatement(sqlStatement);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitFlowControlStatement(final FlowControlStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        if (null != ctx.caseStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.caseStatement()));
        }
        if (null != ctx.ifStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.ifStatement()));
        }
        if (null != ctx.loopStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.loopStatement()));
        }
        if (null != ctx.repeatStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.repeatStatement()));
        }
        if (null != ctx.whileStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(ctx.whileStatement()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCaseStatement(final CaseStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        for (ValidStatementContext each : ctx.validStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitIfStatement(final IfStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        for (ValidStatementContext each : ctx.validStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitLoopStatement(final LoopStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        for (ValidStatementContext each : ctx.validStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRepeatStatement(final RepeatStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        for (ValidStatementContext each : ctx.validStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitWhileStatement(final WhileStatementContext ctx) {
        CollectionValue<ValidStatementSegment> result = new CollectionValue<>();
        for (ValidStatementContext each : ctx.validStatement()) {
            result.combine((CollectionValue<ValidStatementSegment>) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new AlterFunctionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new DropFunctionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateEvent(final CreateEventContext ctx) {
        return new MySQLCreateEventStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterEvent(final AlterEventContext ctx) {
        return new MySQLAlterEventStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropEvent(final DropEventContext ctx) {
        return new MySQLDropEventStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterInstance(final AlterInstanceContext ctx) {
        return new MySQLAlterInstanceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateLogfileGroup(final CreateLogfileGroupContext ctx) {
        return new MySQLCreateLogfileGroupStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterLogfileGroup(final AlterLogfileGroupContext ctx) {
        return new MySQLAlterLogfileGroupStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropLogfileGroup(final DropLogfileGroupContext ctx) {
        return new MySQLDropLogfileGroupStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateServer(final CreateServerContext ctx) {
        return new CreateServerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterServer(final AlterServerContext ctx) {
        return new AlterServerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new DropServerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new CreateTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new DropTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new CreateTablespaceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        if (null != ctx.alterTablespaceInnodb()) {
            return visit(ctx.alterTablespaceInnodb());
        } else {
            return visit(ctx.alterTablespaceNdb());
        }
    }
    
    @Override
    public ASTNode visitAlterTablespaceInnodb(final AlterTablespaceInnodbContext ctx) {
        return new AlterTablespaceStatement(
                getDatabaseType(), null == ctx.tablespace ? null : createTablespaceSegment(ctx.tablespace), null == ctx.renameTablespace ? null : createTablespaceSegment(ctx.renameTablespace));
    }
    
    @Override
    public ASTNode visitAlterTablespaceNdb(final AlterTablespaceNdbContext ctx) {
        return new AlterTablespaceStatement(
                getDatabaseType(), null == ctx.tablespace ? null : createTablespaceSegment(ctx.tablespace), null == ctx.renameTableSpace ? null : createTablespaceSegment(ctx.renameTableSpace));
    }
    
    private TablespaceSegment createTablespaceSegment(final IdentifierContext ctx) {
        return new TablespaceSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx));
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new DropTablespaceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        return new PrepareStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitExecuteStmt(final ExecuteStmtContext ctx) {
        return new ExecuteStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDeallocate(final DeallocateContext ctx) {
        return new DeallocateStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDorisCreateMaterializedView(final DorisCreateMaterializedViewContext ctx) {
        DorisCreateMaterializedViewStatement result = new DorisCreateMaterializedViewStatement(getDatabaseType());
        setMaterializedViewTable(ctx, result);
        result.setIfNotExists(null != ctx.ifNotExists());
        processColumnDefinitions(ctx, result);
        processBuildMode(ctx, result);
        processRefresh(ctx, result);
        processKeyClause(ctx, result);
        processComment(ctx, result);
        processPartitionClause(ctx, result);
        processDistributedClause(ctx, result);
        processProperties(ctx, result);
        result.setSelectStatement((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    private void setMaterializedViewTable(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        statement.setMaterializedView((SimpleTableSegment) visit(ctx.tableName()));
    }
    
    private void processColumnDefinitions(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVColumnDefinitions()) {
            return;
        }
        for (DorisMVColumnDefinitionContext each : ctx.dorisMVColumnDefinitions().dorisMVColumnDefinition()) {
            ColumnSegment column = (ColumnSegment) visit(each.columnName());
            CommentSegment commentSegment = null;
            if (null != each.COMMENT()) {
                String commentText = SQLUtils.getExactlyValue(each.string_().getText());
                commentSegment = new CommentSegment(commentText, each.string_().getStart().getStartIndex(), each.string_().getStop().getStopIndex());
            }
            MaterializedViewColumnSegment mvColumn = new MaterializedViewColumnSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), column, commentSegment);
            statement.getColumns().add(mvColumn);
        }
    }
    
    private void processBuildMode(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVBuildMode()) {
            return;
        }
        if (null != ctx.dorisMVBuildMode().IMMEDIATE()) {
            statement.setBuildMode("IMMEDIATE");
        } else if (null != ctx.dorisMVBuildMode().DEFERRED()) {
            statement.setBuildMode("DEFERRED");
        }
    }
    
    private void processRefresh(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVRefresh()) {
            return;
        }
        processRefreshMethod(ctx, statement);
        processRefreshTrigger(ctx, statement);
    }
    
    private void processRefreshMethod(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVRefresh().dorisMVRefreshMethod()) {
            return;
        }
        if (null != ctx.dorisMVRefresh().dorisMVRefreshMethod().COMPLETE()) {
            statement.setRefreshMethod("COMPLETE");
        } else if (null != ctx.dorisMVRefresh().dorisMVRefreshMethod().AUTO()) {
            statement.setRefreshMethod("AUTO");
        }
    }
    
    private void processRefreshTrigger(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVRefresh().dorisMVRefreshTrigger()) {
            return;
        }
        if (null != ctx.dorisMVRefresh().dorisMVRefreshTrigger().MANUAL()) {
            statement.setRefreshTrigger("MANUAL");
        } else if (null != ctx.dorisMVRefresh().dorisMVRefreshTrigger().SCHEDULE()) {
            processScheduleTrigger(ctx, statement);
        } else if (null != ctx.dorisMVRefresh().dorisMVRefreshTrigger().COMMIT()) {
            statement.setRefreshTrigger("COMMIT");
        }
    }
    
    private void processScheduleTrigger(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        statement.setRefreshTrigger("SCHEDULE");
        if (null != ctx.dorisMVRefresh().dorisMVRefreshTrigger().intervalValue()) {
            ExpressionSegment intervalExpr = (ExpressionSegment) visit(ctx.dorisMVRefresh().dorisMVRefreshTrigger().intervalValue().expr());
            statement.setRefreshIntervalExpression(intervalExpr);
            statement.setRefreshUnit(ctx.dorisMVRefresh().dorisMVRefreshTrigger().intervalValue().intervalUnit().getText());
        }
        if (null != ctx.dorisMVRefresh().dorisMVRefreshTrigger().STARTS()) {
            statement.setStartTime(SQLUtils.getExactlyValue(ctx.dorisMVRefresh().dorisMVRefreshTrigger().timestampValue().getText()));
        }
    }
    
    private void processKeyClause(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVKeyClause()) {
            return;
        }
        statement.setDuplicateKey(null != ctx.dorisMVKeyClause().DUPLICATE());
        for (IdentifierContext each : ctx.dorisMVKeyClause().identifierList().identifier()) {
            IdentifierValue identifierValue = (IdentifierValue) visit(each);
            ColumnSegment column = new ColumnSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), identifierValue);
            statement.getKeyColumns().add(column);
        }
    }
    
    private void processComment(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.commentClause()) {
            return;
        }
        String commentText = SQLUtils.getExactlyValue(ctx.commentClause().literals().getText());
        CommentSegment commentSegment = new CommentSegment(commentText, ctx.commentClause().literals().getStart().getStartIndex(), ctx.commentClause().literals().getStop().getStopIndex());
        statement.getComments().add(commentSegment);
        statement.setComment(commentSegment);
    }
    
    private void processPartitionClause(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVPartitionClause()) {
            return;
        }
        if (null != ctx.dorisMVPartitionClause().columnName()) {
            statement.setPartitionColumn((ColumnSegment) visit(ctx.dorisMVPartitionClause().columnName()));
        } else if (null != ctx.dorisMVPartitionClause().dorisMVPartitionFunction()) {
            statement.setPartitionFunctionName("DATE_TRUNC");
            statement.setPartitionFunctionColumn((ColumnSegment) visit(ctx.dorisMVPartitionClause().dorisMVPartitionFunction().columnName()));
            statement.setPartitionFunctionUnit(SQLUtils.getExactlyValue(ctx.dorisMVPartitionClause().dorisMVPartitionFunction().string_().getText()));
        }
    }
    
    private void processDistributedClause(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null == ctx.dorisMVDistributedClause()) {
            return;
        }
        if (null != ctx.dorisMVDistributedClause().HASH()) {
            statement.setDistributeType("HASH");
            for (IdentifierContext each : ctx.dorisMVDistributedClause().identifierList().identifier()) {
                IdentifierValue identifierValue = (IdentifierValue) visit(each);
                statement.getDistributeColumns().add(identifierValue);
            }
        } else if (null != ctx.dorisMVDistributedClause().RANDOM()) {
            statement.setDistributeType("RANDOM");
        }
        if (null != ctx.dorisMVDistributedClause().BUCKETS()) {
            if (null != ctx.dorisMVDistributedClause().NUMBER_()) {
                statement.setBucketCount(Integer.parseInt(ctx.dorisMVDistributedClause().NUMBER_().getText()));
            } else if (null != ctx.dorisMVDistributedClause().AUTO()) {
                statement.setAutoBucket(true);
            }
        }
    }
    
    private void processProperties(final DorisCreateMaterializedViewContext ctx, final DorisCreateMaterializedViewStatement statement) {
        if (null != ctx.propertiesClause()) {
            statement.setProperties(extractPropertiesSegment(ctx.propertiesClause()));
        }
    }
    
    @Override
    public ASTNode visitDorisAlterMaterializedView(final DorisAlterMaterializedViewContext ctx) {
        DorisAlterMaterializedViewStatement result = new DorisAlterMaterializedViewStatement(getDatabaseType());
        result.setMaterializedView((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.dorisAlterMVRename()) {
            processAlterMVRename(ctx.dorisAlterMVRename(), result);
        } else if (null != ctx.dorisAlterMVRefresh()) {
            processAlterMVRefresh(ctx.dorisAlterMVRefresh(), result);
        } else if (null != ctx.dorisAlterMVReplace()) {
            processAlterMVReplace(ctx.dorisAlterMVReplace(), result);
        } else if (null != ctx.dorisAlterMVSetProperties()) {
            processAlterMVSetProperties(ctx.dorisAlterMVSetProperties(), result);
        }
        return result;
    }
    
    private void processAlterMVRename(final DorisAlterMVRenameContext ctx, final DorisAlterMaterializedViewStatement statement) {
        statement.setRenameValue((IdentifierValue) visit(ctx.identifier()));
    }
    
    private void processAlterMVRefresh(final DorisAlterMVRefreshContext ctx, final DorisAlterMaterializedViewStatement statement) {
        if (null != ctx.dorisMVRefreshMethod()) {
            if (null != ctx.dorisMVRefreshMethod().COMPLETE()) {
                statement.setRefreshMethod("COMPLETE");
            } else if (null != ctx.dorisMVRefreshMethod().AUTO()) {
                statement.setRefreshMethod("AUTO");
            }
        }
        if (null != ctx.dorisMVRefreshTrigger()) {
            processAlterMVRefreshTrigger(ctx.dorisMVRefreshTrigger(), statement);
        }
    }
    
    private void processAlterMVRefreshTrigger(final DorisMVRefreshTriggerContext ctx, final DorisAlterMaterializedViewStatement statement) {
        if (null != ctx.MANUAL()) {
            statement.setRefreshTrigger("MANUAL");
        } else if (null != ctx.SCHEDULE()) {
            statement.setRefreshTrigger("SCHEDULE");
            if (null != ctx.intervalValue()) {
                ExpressionSegment intervalExpr = (ExpressionSegment) visit(ctx.intervalValue().expr());
                statement.setRefreshIntervalExpression(intervalExpr);
                statement.setRefreshUnit(ctx.intervalValue().intervalUnit().getText());
            }
            if (null != ctx.STARTS()) {
                statement.setStartTime(SQLUtils.getExactlyValue(ctx.timestampValue().getText()));
            }
        } else if (null != ctx.COMMIT()) {
            statement.setRefreshTrigger("COMMIT");
        }
    }
    
    private void processAlterMVReplace(final DorisAlterMVReplaceContext ctx, final DorisAlterMaterializedViewStatement statement) {
        statement.setReplaceWithView((IdentifierValue) visit(ctx.identifier()));
        if (null != ctx.propertiesClause()) {
            statement.setReplaceProperties(extractPropertiesSegment(ctx.propertiesClause()));
        }
    }
    
    private void processAlterMVSetProperties(final DorisAlterMVSetPropertiesContext ctx, final DorisAlterMaterializedViewStatement statement) {
        PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (PropertyContext each : ctx.propertyItemList().property()) {
            String key = getPropertyKey(each);
            String value = SQLUtils.getExactlyValue(each.literals().getText());
            PropertySegment propertySegment = new PropertySegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), key, value);
            propertiesSegment.getProperties().add(propertySegment);
        }
        statement.setSetProperties(propertiesSegment);
    }
    
    @Override
    public ASTNode visitCreateEncryptKey(final CreateEncryptKeyContext ctx) {
        return new CreateEncryptKeyStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropEncryptKey(final DropEncryptKeyContext ctx) {
        return new DropEncryptKeyStatement(getDatabaseType());
    }
}
