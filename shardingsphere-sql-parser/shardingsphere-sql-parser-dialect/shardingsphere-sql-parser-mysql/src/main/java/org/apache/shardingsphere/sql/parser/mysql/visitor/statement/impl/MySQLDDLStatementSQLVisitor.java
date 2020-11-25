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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterInstanceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterServerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableDropContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CaseStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeColumnContext;
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
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropEventContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropLogfileGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropServerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FlowControlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IfStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyListWithExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyPartContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.KeyPartsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoopStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ModifyColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PlaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReferenceDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RepeatStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RoutineBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SimpleStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableConstraintDefContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableElementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ValidStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WhileStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FieldDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterListContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropEventStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropLogfileGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DDL Statement SQL visitor for MySQL.
 */
public final class MySQLDDLStatementSQLVisitor extends MySQLStatementSQLVisitor implements DDLSQLVisitor, SQLStatementVisitor {
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        MySQLCreateViewStatement result = new MySQLCreateViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setSelect((MySQLSelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        MySQLAlterViewStatement result = new MySQLAlterViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
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
        result.setDatabaseName(ctx.schemaName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new MySQLAlterDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        MySQLDropDatabaseStatement result = new MySQLDropDatabaseStatement();
        result.setDatabaseName(ctx.schemaName().getText());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        MySQLCreateTableStatement result = new MySQLCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setNotExisted(null != ctx.notExistClause());
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
        if (null != ctx.alterTableActions() && null != ctx.alterTableActions().alterCommandList() && null != ctx.alterTableActions().alterCommandList().alterList()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterTableActions().alterCommandList().alterList())).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.getAddColumnDefinitions().add((AddColumnDefinitionSegment) each);
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.getModifyColumnDefinitions().add((ModifyColumnDefinitionSegment) each);
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.getDropColumnDefinitions().add((DropColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getAddConstraintDefinitions().add((ConstraintDefinitionSegment) each);
                }
            }
        }
        return result;
    }

    private ColumnDefinitionSegment generateColumnDefinitionSegment(final ColumnSegment column, final FieldDefinitionContext ctx) {
        DataTypeSegment dataTypeSegment = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                column.getStartIndex(), dataTypeSegment.getStopIndex(), column, dataTypeSegment, isPrimaryKey);
        return result;
    }

    @Override
    public ASTNode visitAlterList(final AlterListContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (ctx.alterListItem().isEmpty()) {
            return result;
        }
        for (MySQLStatementParser.AlterListItemContext each : ctx.alterListItem()) {
            if (each instanceof AddColumnContext) {
                result.getValue().add((AddColumnDefinitionSegment) visit(each));
            }
            if (each instanceof ChangeColumnContext) {
                ModifyColumnDefinitionSegment modifyColumnDefinition = new ModifyColumnDefinitionSegment(
                        each.getStart().getStartIndex(), each.getStop().getStopIndex(),
                        (ColumnDefinitionSegment) visit(((ChangeColumnContext) each).columnDefinition()));
                if (null != ((MySQLStatementParser.ChangeColumnContext) each).place()) {
                    modifyColumnDefinition.setColumnPosition((ColumnPositionSegment) visit(((MySQLStatementParser.ChangeColumnContext) each).place()));
                }
                result.getValue().add(modifyColumnDefinition);
            }
            if (each instanceof ModifyColumnContext) {
                ColumnSegment column = new ColumnSegment(((ModifyColumnContext) each).columnInternalRef.start.getStartIndex(), ((ModifyColumnContext) each).columnInternalRef.stop.getStopIndex(),
                        (IdentifierValue) visit(((ModifyColumnContext) each).columnInternalRef));
                ModifyColumnDefinitionSegment modifyColumnDefinition = new ModifyColumnDefinitionSegment(
                        each.getStart().getStartIndex(), each.getStop().getStopIndex(),
                        generateColumnDefinitionSegment(column, ((MySQLStatementParser.ModifyColumnContext) each).fieldDefinition()));
                if (null != ((MySQLStatementParser.ModifyColumnContext) each).place()) {
                    modifyColumnDefinition.setColumnPosition((ColumnPositionSegment) visit(((MySQLStatementParser.ModifyColumnContext) each).place()));
                }
                result.getValue().add(modifyColumnDefinition);
            }
            if (each instanceof AlterTableDropContext) {
                AlterTableDropContext alterTableDrop = (AlterTableDropContext) each;
                if (null == alterTableDrop.KEY() && null == alterTableDrop.CHECK() && null == alterTableDrop.CONSTRAINT() && null == alterTableDrop.keyOrIndex()) {
                    ColumnSegment column = new ColumnSegment(alterTableDrop.columnInternalRef.start.getStartIndex(), alterTableDrop.columnInternalRef.stop.getStopIndex(),
                            (IdentifierValue) visit(alterTableDrop.columnInternalRef));
                    result.getValue().add(new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList(column)));
                }
            }
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
            for (MySQLStatementParser.TableElementContext each : ctx.tableElementList().tableElement()) {
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
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.column_name.start.getStartIndex(), ctx.column_name.stop.getStopIndex(), (IdentifierValue) visit(ctx.column_name));
        DataTypeSegment dataTypeSegment = (DataTypeSegment) visit(ctx.fieldDefinition().dataType());
        boolean isPrimaryKey = isPrimaryKey(ctx.fieldDefinition());
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, isPrimaryKey);
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
    
    private boolean isPrimaryKey(final FieldDefinitionContext ctx) {
        for (MySQLStatementParser.ColumnAttributeContext each : ctx.columnAttribute()) {
            if (null != each.KEY() && null == each.UNIQUE()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ASTNode visitTableConstraintDef(final TableConstraintDefContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.KEY() && null != ctx.PRIMARY()) {
            result.getPrimaryKeyColumns().addAll(getKeyColumnsFromKeyListWithExpression(ctx.keyListWithExpression()));
            return result;
        }
        if (null != ctx.FOREIGN()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.referenceDefinition()));
            return result;
        }
        if (null != ctx.UNIQUE()) {
            result.getIndexColumns().addAll(getKeyColumnsFromKeyListWithExpression(ctx.keyListWithExpression()));
            if (null != ctx.indexNameAndType()) {
                result.setIndexName((IndexSegment) visit(ctx.indexNameAndType().indexName()));
            }
            return result;
        }
        if (null != ctx.checkConstraint()) {
            return result;
        }
        result.getIndexColumns().addAll(getKeyColumnsFromKeyListWithExpression(ctx.keyListWithExpression()));
        if (null != ctx.indexName()) {
            result.setIndexName((IndexSegment) visit(ctx.indexName()));
        }
        if (null != ctx.indexNameAndType()) {
            result.setIndexName((IndexSegment) visit(ctx.indexNameAndType().indexName()));
        }
        return result;
    }

    private Collection<ColumnSegment> getKeyColumnsFromKeyListWithExpression(final KeyListWithExpressionContext ctx) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (MySQLStatementParser.KeyPartWithExpressionContext each : ctx.keyPartWithExpression()) {
            if (null != each.keyPart()) {
                result.add((ColumnSegment) visit(each.keyPart().columnName()));
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
        return null == ctx.columnName() ? new ColumnFirstPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName)
                : new ColumnAfterPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        MySQLDropTableStatement result = new MySQLDropTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        MySQLTruncateStatement result = new MySQLTruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        MySQLCreateIndexStatement result = new MySQLCreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        MySQLDropIndexStatement result = new MySQLDropIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
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
        result.setRoutineBody((RoutineBodySegment) visit(ctx.routineBody()));
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
        result.setRoutineBody((RoutineBodySegment) visit(ctx.routineBody()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitRoutineBody(final RoutineBodyContext ctx) {
        RoutineBodySegment result = new RoutineBodySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        CollectionValue<ValidStatementSegment> validStatements;
        if (null != ctx.simpleStatement()) {
            validStatements = (CollectionValue<ValidStatementSegment>) visit(ctx.simpleStatement());
        } else {
            validStatements = (CollectionValue<ValidStatementSegment>) visit(ctx.compoundStatement());
        }
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
}
