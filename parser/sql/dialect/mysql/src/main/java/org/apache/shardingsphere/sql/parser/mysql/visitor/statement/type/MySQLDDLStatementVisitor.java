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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddTableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterAlgorithmOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterCheckContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterCommandsModifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterConvertContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterInstanceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterListItemContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterLockOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterRenameTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterServerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableDropContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CaseStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CharsetNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CompoundStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLikeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateServerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DeallocateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropServerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FieldDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FlowControlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IfStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyListWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyPartContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyPartWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyPartsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoopStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ModifyColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PlaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReferenceDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RepeatStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoutineBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SimpleStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableConstraintDefContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableElementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ValidStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WhileStatementContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AlgorithmOption;
import org.apache.shardingsphere.sql.parser.sql.common.enums.LockTableOption;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.charset.CharsetNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.tablespace.TablespaceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterInstanceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDeallocateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLExecuteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLRenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * DDL statement visitor for MySQL.
 */
public final class MySQLDDLStatementVisitor extends MySQLStatementVisitor implements DDLStatementVisitor {
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        MySQLCreateViewStatement result = new MySQLCreateViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((MySQLSelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        MySQLAlterViewStatement result = new MySQLAlterViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((MySQLSelectStatement) visit(ctx.select()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        MySQLDropViewStatement result = new MySQLDropViewStatement();
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.viewNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        MySQLCreateDatabaseStatement result = new MySQLCreateDatabaseStatement();
        result.setDatabaseName(new IdentifierValue(ctx.schemaName().getText()).getValue());
        result.setIfNotExists(null != ctx.ifNotExists());
        return result;
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new MySQLAlterDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        MySQLDropDatabaseStatement result = new MySQLDropDatabaseStatement();
        result.setDatabaseName(new IdentifierValue(ctx.schemaName().getText()).getValue());
        result.setIfExists(null != ctx.ifExists());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        MySQLCreateTableStatement result = new MySQLCreateTableStatement(null != ctx.ifNotExists());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
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
        MySQLAlterTableStatement result = new MySQLAlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null == ctx.alterTableActions() || null == ctx.alterTableActions().alterCommandList() || null == ctx.alterTableActions().alterCommandList().alterList()) {
            return result;
        }
        for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterTableActions().alterCommandList().alterList())).getValue()) {
            setAlterDefinition(result, each);
        }
        return result;
    }
    
    private void setAlterDefinition(final MySQLAlterTableStatement alterTableStatement, final AlterDefinitionSegment alterDefinitionSegment) {
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
        // TODO parse not null
        return new ColumnDefinitionSegment(column.getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, isPrimaryKey, false);
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
        MySQLRenameTableStatement result = new MySQLRenameTableStatement();
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
        // TODO parse not null
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, isPrimaryKey, false);
        result.getReferencedTables().addAll(getReferencedTables(ctx));
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
            result.getPrimaryKeyColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
            return result;
        }
        if (null != ctx.FOREIGN()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.referenceDefinition()));
            return result;
        }
        if (null != ctx.UNIQUE()) {
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
        MySQLDropTableStatement result = new MySQLDropTableStatement(null != ctx.ifExists());
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        MySQLTruncateStatement result = new MySQLTruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        MySQLCreateIndexStatement result = new MySQLCreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        IndexNameSegment indexName = new IndexNameSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), new IdentifierValue(ctx.indexName().getText()));
        result.setIndex(new IndexSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), indexName));
        result.getColumns().addAll(((CollectionValue) visit(ctx.keyListWithExpression())).getValue());
        if (null != ctx.algorithmOptionAndLockOption()) {
            if (null != ctx.algorithmOptionAndLockOption().alterAlgorithmOption()) {
                result.setAlgorithmSegment((AlgorithmTypeSegment) visit(ctx.algorithmOptionAndLockOption().alterAlgorithmOption()));
            }
            if (null != ctx.algorithmOptionAndLockOption().alterLockOption()) {
                result.setLockTableSegment((LockTableSegment) visit(ctx.algorithmOptionAndLockOption().alterLockOption()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        MySQLDropIndexStatement result = new MySQLDropIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        IndexNameSegment indexName = new IndexNameSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), new IdentifierValue(ctx.indexName().getText()));
        result.getIndexes().add(new IndexSegment(ctx.indexName().start.getStartIndex(), ctx.indexName().stop.getStopIndex(), indexName));
        if (null != ctx.algorithmOptionAndLockOption()) {
            if (null != ctx.algorithmOptionAndLockOption().alterAlgorithmOption()) {
                result.setAlgorithmSegment((AlgorithmTypeSegment) visit(ctx.algorithmOptionAndLockOption().alterAlgorithmOption()));
            }
            if (null != ctx.algorithmOptionAndLockOption().alterLockOption()) {
                result.setLockTableSegment((LockTableSegment) visit(ctx.algorithmOptionAndLockOption().alterLockOption()));
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
        MySQLCreateProcedureStatement result = new MySQLCreateProcedureStatement();
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
        return new MySQLAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new MySQLDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        MySQLCreateFunctionStatement result = new MySQLCreateFunctionStatement();
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
            sqlStatement = (MySQLCreateTableStatement) visit(ctx.createTable());
        } else if (null != ctx.alterTable()) {
            sqlStatement = (MySQLAlterTableStatement) visit(ctx.alterTable());
        } else if (null != ctx.dropTable()) {
            sqlStatement = (MySQLDropTableStatement) visit(ctx.dropTable());
        } else if (null != ctx.truncateTable()) {
            sqlStatement = (MySQLTruncateStatement) visit(ctx.truncateTable());
        } else if (null != ctx.insert()) {
            sqlStatement = (MySQLInsertStatement) visit(ctx.insert());
        } else if (null != ctx.replace()) {
            sqlStatement = (MySQLInsertStatement) visit(ctx.replace());
        } else if (null != ctx.update()) {
            sqlStatement = (MySQLUpdateStatement) visit(ctx.update());
        } else if (null != ctx.delete()) {
            sqlStatement = (MySQLDeleteStatement) visit(ctx.delete());
        } else if (null != ctx.select()) {
            sqlStatement = (MySQLSelectStatement) visit(ctx.select());
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
        return new MySQLAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new MySQLDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateEvent(final CreateEventContext ctx) {
        return new MySQLCreateEventStatement();
    }
    
    @Override
    public ASTNode visitAlterEvent(final AlterEventContext ctx) {
        return new MySQLAlterEventStatement();
    }
    
    @Override
    public ASTNode visitDropEvent(final DropEventContext ctx) {
        return new MySQLDropEventStatement();
    }
    
    @Override
    public ASTNode visitAlterInstance(final AlterInstanceContext ctx) {
        return new MySQLAlterInstanceStatement();
    }
    
    @Override
    public ASTNode visitCreateLogfileGroup(final CreateLogfileGroupContext ctx) {
        return new MySQLCreateLogfileGroupStatement();
    }
    
    @Override
    public ASTNode visitAlterLogfileGroup(final AlterLogfileGroupContext ctx) {
        return new MySQLAlterLogfileGroupStatement();
    }
    
    @Override
    public ASTNode visitDropLogfileGroup(final DropLogfileGroupContext ctx) {
        return new MySQLDropLogfileGroupStatement();
    }
    
    @Override
    public ASTNode visitCreateServer(final CreateServerContext ctx) {
        return new MySQLCreateServerStatement();
    }
    
    @Override
    public ASTNode visitAlterServer(final AlterServerContext ctx) {
        return new MySQLAlterServerStatement();
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new MySQLDropServerStatement();
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new MySQLCreateTriggerStatement();
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new MySQLDropTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new MySQLCreateTablespaceStatement();
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        MySQLAlterTablespaceStatement result = new MySQLAlterTablespaceStatement();
        if (null != ctx.alterTablespaceInnodb().tablespace) {
            result.setTablespaceSegment(new TablespaceSegment(ctx.alterTablespaceInnodb().tablespace.getStart().getStartIndex(),
                    ctx.alterTablespaceInnodb().tablespace.getStop().getStopIndex(),
                    (IdentifierValue) visit(ctx.alterTablespaceInnodb().tablespace)));
        } else if (null != ctx.alterTablespaceNdb().tablespace) {
            result.setTablespaceSegment(new TablespaceSegment(ctx.alterTablespaceNdb().tablespace.getStart().getStartIndex(),
                    ctx.alterTablespaceNdb().tablespace.getStop().getStopIndex(),
                    (IdentifierValue) visit(ctx.alterTablespaceNdb().tablespace)));
        }
        if (null != ctx.alterTablespaceInnodb().renameTablespace) {
            result.setRenameTablespaceSegment(new TablespaceSegment(ctx.alterTablespaceInnodb().renameTablespace.getStart().getStartIndex(),
                    ctx.alterTablespaceInnodb().renameTablespace.getStop().getStopIndex(),
                    (IdentifierValue) visit(ctx.alterTablespaceInnodb().renameTablespace)));
        } else if (null != ctx.alterTablespaceNdb().renameTableSpace) {
            result.setRenameTablespaceSegment(new TablespaceSegment(ctx.alterTablespaceNdb().renameTableSpace.getStart().getStartIndex(),
                    ctx.alterTablespaceNdb().renameTableSpace.getStop().getStopIndex(),
                    (IdentifierValue) visit(ctx.alterTablespaceInnodb().renameTablespace)));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new MySQLDropTablespaceStatement();
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        return new MySQLPrepareStatement();
    }
    
    @Override
    public ASTNode visitExecuteStmt(final ExecuteStmtContext ctx) {
        return new MySQLExecuteStatement();
    }
    
    @Override
    public ASTNode visitDeallocate(final DeallocateContext ctx) {
        return new MySQLDeallocateStatement();
    }
}
