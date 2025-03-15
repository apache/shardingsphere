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

import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateMaterializedViewContext;
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
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FieldDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FlowControlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.FunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IfStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyListWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyPartContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyPartWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.KeyPartsContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoopStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ModifyColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PlaceContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PrepareContext;
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
import org.apache.shardingsphere.sql.parser.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AlgorithmOption;
import org.apache.shardingsphere.sql.parser.statement.core.enums.LockTableOption;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.tablespace.TablespaceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterEventStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterInstanceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterServerStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateServerStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDeallocateStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropEventStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropServerStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisDropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisExecuteStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisRenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisTruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisInsertStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisUpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * DDL statement visitor for Doris.
 */
public final class DorisDDLStatementVisitor extends DorisStatementVisitor implements DDLStatementVisitor {
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        DorisCreateViewStatement result = new DorisCreateViewStatement();
        result.setReplaceView(null != ctx.REPLACE());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((DorisSelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        DorisAlterViewStatement result = new DorisAlterViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((DorisSelectStatement) visit(ctx.select()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        DorisDropViewStatement result = new DorisDropViewStatement();
        result.setIfExists(null != ctx.ifExists());
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.viewNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        DorisCreateDatabaseStatement result = new DorisCreateDatabaseStatement();
        result.setDatabaseName(new IdentifierValue(ctx.databaseName().getText()).getValue());
        result.setIfNotExists(null != ctx.ifNotExists());
        return result;
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new DorisAlterDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        DorisDropDatabaseStatement result = new DorisDropDatabaseStatement();
        result.setDatabaseName(new IdentifierValue(ctx.databaseName().getText()).getValue());
        result.setIfExists(null != ctx.ifExists());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        DorisCreateTableStatement result = new DorisCreateTableStatement();
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
        DorisAlterTableStatement result = new DorisAlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null == ctx.alterTableActions() || null == ctx.alterTableActions().alterCommandList() || null == ctx.alterTableActions().alterCommandList().alterList()) {
            return result;
        }
        for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterTableActions().alterCommandList().alterList())).getValue()) {
            setAlterDefinition(result, each);
        }
        return result;
    }
    
    private void setAlterDefinition(final DorisAlterTableStatement alterTableStatement, final AlterDefinitionSegment alterDefinitionSegment) {
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
        DorisRenameTableStatement result = new DorisRenameTableStatement();
        for (int i = 0, len = ctx.tableName().size(); i < len; i += 2) {
            TableNameContext tableName = ctx.tableName(i);
            TableNameContext renameTableName = ctx.tableName(i + 1);
            result.getRenameTables().add(createRenameTableDefinitionSegment(tableName, renameTableName));
        }
        return result;
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
        DorisDropTableStatement result = new DorisDropTableStatement();
        result.setIfExists(null != ctx.ifExists());
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        DorisTruncateStatement result = new DorisTruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        DorisCreateIndexStatement result = new DorisCreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        IndexNameSegment indexName = new IndexNameSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), new IdentifierValue(ctx.indexName().getText()));
        result.setIndex(new IndexSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), indexName));
        result.getColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
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
        DorisDropIndexStatement result = new DorisDropIndexStatement();
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
        DorisCreateProcedureStatement result = new DorisCreateProcedureStatement();
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
        return new DorisAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DorisDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        DorisCreateFunctionStatement result = new DorisCreateFunctionStatement();
        result.setFunctionName((FunctionNameSegment) visit(ctx.functionName()));
        result.setRoutineBody((RoutineBodySegment) visit(ctx.routineBody()));
        return result;
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
            sqlStatement = (DorisCreateTableStatement) visit(ctx.createTable());
        } else if (null != ctx.alterTable()) {
            sqlStatement = (DorisAlterTableStatement) visit(ctx.alterTable());
        } else if (null != ctx.dropTable()) {
            sqlStatement = (DorisDropTableStatement) visit(ctx.dropTable());
        } else if (null != ctx.truncateTable()) {
            sqlStatement = (DorisTruncateStatement) visit(ctx.truncateTable());
        } else if (null != ctx.insert()) {
            sqlStatement = (DorisInsertStatement) visit(ctx.insert());
        } else if (null != ctx.replace()) {
            sqlStatement = (DorisInsertStatement) visit(ctx.replace());
        } else if (null != ctx.update()) {
            sqlStatement = (DorisUpdateStatement) visit(ctx.update());
        } else if (null != ctx.delete()) {
            sqlStatement = (DorisDeleteStatement) visit(ctx.delete());
        } else if (null != ctx.select()) {
            sqlStatement = (DorisSelectStatement) visit(ctx.select());
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
        return new DorisAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new DorisDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateEvent(final CreateEventContext ctx) {
        return new DorisCreateEventStatement();
    }
    
    @Override
    public ASTNode visitAlterEvent(final AlterEventContext ctx) {
        return new DorisAlterEventStatement();
    }
    
    @Override
    public ASTNode visitDropEvent(final DropEventContext ctx) {
        return new DorisDropEventStatement();
    }
    
    @Override
    public ASTNode visitAlterInstance(final AlterInstanceContext ctx) {
        return new DorisAlterInstanceStatement();
    }
    
    @Override
    public ASTNode visitCreateLogfileGroup(final CreateLogfileGroupContext ctx) {
        return new DorisCreateLogfileGroupStatement();
    }
    
    @Override
    public ASTNode visitAlterLogfileGroup(final AlterLogfileGroupContext ctx) {
        return new DorisAlterLogfileGroupStatement();
    }
    
    @Override
    public ASTNode visitDropLogfileGroup(final DropLogfileGroupContext ctx) {
        return new DorisDropLogfileGroupStatement();
    }
    
    @Override
    public ASTNode visitCreateServer(final CreateServerContext ctx) {
        return new DorisCreateServerStatement();
    }
    
    @Override
    public ASTNode visitAlterServer(final AlterServerContext ctx) {
        return new DorisAlterServerStatement();
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new DorisDropServerStatement();
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new DorisCreateTriggerStatement();
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new DorisDropTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new DorisCreateTablespaceStatement();
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
        DorisAlterTablespaceStatement result = new DorisAlterTablespaceStatement();
        if (null != ctx.tablespace) {
            result.setTablespaceSegment(createTablespaceSegment(ctx.tablespace));
        }
        if (null != ctx.renameTablespace) {
            result.setRenameTablespaceSegment(createTablespaceSegment(ctx.renameTablespace));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTablespaceNdb(final AlterTablespaceNdbContext ctx) {
        DorisAlterTablespaceStatement result = new DorisAlterTablespaceStatement();
        if (null != ctx.tablespace) {
            result.setTablespaceSegment(createTablespaceSegment(ctx.tablespace));
        }
        if (null != ctx.renameTableSpace) {
            result.setRenameTablespaceSegment(createTablespaceSegment(ctx.renameTableSpace));
        }
        return result;
    }
    
    private TablespaceSegment createTablespaceSegment(final IdentifierContext ctx) {
        return new TablespaceSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx));
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new DorisDropTablespaceStatement();
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        return new DorisPrepareStatement();
    }
    
    @Override
    public ASTNode visitExecuteStmt(final ExecuteStmtContext ctx) {
        return new DorisExecuteStatement();
    }
    
    @Override
    public ASTNode visitDeallocate(final DeallocateContext ctx) {
        return new DorisDeallocateStatement();
    }
    
    @Override
    public ASTNode visitCreateMaterializedView(final CreateMaterializedViewContext ctx) {
        return new DorisCreateMaterializedViewStatement();
    }
}
