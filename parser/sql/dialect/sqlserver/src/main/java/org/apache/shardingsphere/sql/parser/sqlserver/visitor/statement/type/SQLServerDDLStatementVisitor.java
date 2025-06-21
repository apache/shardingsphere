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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterCheckConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterColumnAddOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterServiceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterTableDropConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnDefinitionOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNameWithSortContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateServiceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableDefinitionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropServiceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ViewNameContext;
import org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.SQLServerStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.SQLServerAlterServiceStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.SQLServerCreateServiceStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.SQLServerDropServiceStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * DDL statement visitor for SQLServer.
 */
public final class SQLServerDDLStatementVisitor extends SQLServerStatementVisitor implements DDLStatementVisitor {
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        return null == ctx.createTableClause() ? visit(ctx.createTableAsSelectClause()) : visit(ctx.createTableClause());
    }
    
    @Override
    public ASTNode visitCreateTableClause(final CreateTableClauseContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) generateCreateDefinitionSegment(ctx.createDefinitionClause().createTableDefinitions());
        for (CreateDefinitionSegment each : createDefinitions.getValue()) {
            if (each instanceof ColumnDefinitionSegment) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) each);
            } else if (each instanceof ConstraintDefinitionSegment) {
                result.getConstraintDefinitions().add((ConstraintDefinitionSegment) each);
            }
        }
        return result;
    }
    
    private ASTNode generateCreateDefinitionSegment(final CreateTableDefinitionsContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        for (CreateTableDefinitionContext each : ctx.createTableDefinition()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.tableConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.tableConstraint()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        // TODO parse not null
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey, false, getText(ctx));
        for (ColumnDefinitionOptionContext each : ctx.columnDefinitionOption()) {
            for (ColumnConstraintContext columnConstraint : each.columnConstraint()) {
                if (null != columnConstraint.columnForeignKeyConstraint()) {
                    result.getReferencedTables().add((SimpleTableSegment) visit(columnConstraint.columnForeignKeyConstraint().tableName()));
                }
            }
        }
        for (ColumnConstraintContext each : ctx.columnConstraints().columnConstraint()) {
            if (null != each.columnForeignKeyConstraint()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.columnForeignKeyConstraint().tableName()));
            }
        }
        return result;
    }
    
    private String getText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
    
    private boolean isPrimaryKey(final ColumnDefinitionContext ctx) {
        for (ColumnDefinitionOptionContext each : ctx.columnDefinitionOption()) {
            for (ColumnConstraintContext columnConstraint : each.columnConstraint()) {
                if (null != columnConstraint.primaryKeyConstraint() && null != columnConstraint.primaryKeyConstraint().primaryKey()) {
                    return true;
                }
            }
        }
        for (ColumnConstraintContext each : ctx.columnConstraints().columnConstraint()) {
            if (null != each.primaryKeyConstraint() && null != each.primaryKeyConstraint().primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTableConstraint(final TableConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintName()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintName()));
        }
        if (null != ctx.tablePrimaryConstraint() && null != ctx.tablePrimaryConstraint().primaryKeyUnique().primaryKey()) {
            if (null != ctx.tablePrimaryConstraint().diskTablePrimaryConstraintOption()) {
                result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.tablePrimaryConstraint().diskTablePrimaryConstraintOption().columnNames())).getValue());
            }
            if (null != ctx.tablePrimaryConstraint().memoryTablePrimaryConstraintOption()) {
                result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.tablePrimaryConstraint().memoryTablePrimaryConstraintOption().columnNames())).getValue());
            }
        }
        if (null != ctx.tableForeignKeyConstraint()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.tableForeignKeyConstraint().tableName()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        for (AlterDefinitionClauseContext alterDefinitionClauseContext : ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(alterDefinitionClauseContext)).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.getAddColumnDefinitions().add((AddColumnDefinitionSegment) each);
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.getModifyColumnDefinitions().add((ModifyColumnDefinitionSegment) each);
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.getDropColumnDefinitions().add((DropColumnDefinitionSegment) each);
                } else if (each instanceof AddConstraintDefinitionSegment) {
                    result.getAddConstraintDefinitions().add((AddConstraintDefinitionSegment) each);
                } else if (each instanceof ModifyConstraintDefinitionSegment) {
                    result.getModifyConstraintDefinitions().add((ModifyConstraintDefinitionSegment) each);
                } else if (each instanceof DropConstraintDefinitionSegment) {
                    result.getDropConstraintDefinitions().add((DropConstraintDefinitionSegment) each);
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.addColumnSpecification()) {
            result.getValue().addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(ctx.addColumnSpecification())).getValue());
        }
        if (null != ctx.modifyColumnSpecification()) {
            result.getValue().add((ModifyColumnDefinitionSegment) visit(ctx.modifyColumnSpecification()));
        }
        if (null != ctx.alterDrop() && null != ctx.alterDrop().dropColumnSpecification()) {
            result.getValue().add((DropColumnDefinitionSegment) visit(ctx.alterDrop().dropColumnSpecification()));
        }
        if (null != ctx.alterDrop() && null != ctx.alterDrop().alterTableDropConstraint()) {
            result.combine((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterDrop().alterTableDropConstraint()));
        }
        if (null != ctx.alterCheckConstraint()) {
            result.getValue().add((AlterDefinitionSegment) visit(ctx.alterCheckConstraint()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.alterColumnAddOptions()) {
            for (AlterColumnAddOptionContext each : ctx.alterColumnAddOptions().alterColumnAddOption()) {
                if (null != each.columnDefinition()) {
                    AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                            each.columnDefinition().getStart().getStartIndex(), each.columnDefinition().getStop().getStopIndex(),
                            Collections.singletonList((ColumnDefinitionSegment) visit(each.columnDefinition())));
                    result.getValue().add(addColumnDefinition);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO visit pk and table ref
        ColumnSegment column = (ColumnSegment) visit(ctx.alterColumnOperation().columnName());
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType());
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false, getText(ctx));
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        Collection<ColumnSegment> columns = new LinkedList<>();
        for (ColumnNameContext each : ctx.columnName()) {
            columns.add((ColumnSegment) visit(each));
        }
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        result.setContainsCascade(null != ctx.ifExists());
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setIndex((IndexSegment) visit(ctx.indexName()));
        result.getColumns().addAll(((CollectionValue) visit(ctx.columnNamesWithSort())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitColumnNameWithSort(final ColumnNameWithSortContext ctx) {
        return visit(ctx.columnName());
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        AlterIndexStatement result = new AlterIndexStatement();
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        }
        result.setSimpleTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement();
        result.setIfExists(null != ctx.ifExists());
        result.getIndexes().add((IndexSegment) visit(ctx.indexName()));
        result.setSimpleTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterCheckConstraint(final AlterCheckConstraintContext ctx) {
        return new ModifyConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitAlterTableDropConstraint(final AlterTableDropConstraintContext ctx) {
        CollectionValue<DropConstraintDefinitionSegment> result = new CollectionValue<>();
        for (DropConstraintNameContext each : ctx.dropConstraintName()) {
            result.getValue().add(new DropConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(each.constraintName())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new CreateDatabaseStatement(ctx.databaseName().getText(), false);
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new CreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new CreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        CreateViewStatement result = new CreateViewStatement();
        result.setReplaceView(null != ctx.ALTER());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.createOrAlterViewClause().select()));
        result.setSelect((SelectStatement) visit(ctx.createOrAlterViewClause().select()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new CreateTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        return new CreateSequenceStatement(ctx.sequenceName().name().getText());
    }
    
    @Override
    public ASTNode visitCreateSchema(final CreateSchemaContext ctx) {
        return new CreateSchemaStatement();
    }
    
    @Override
    public ASTNode visitCreateService(final CreateServiceContext ctx) {
        return new SQLServerCreateServiceStatement();
    }
    
    @Override
    public ASTNode visitAlterSchema(final AlterSchemaContext ctx) {
        return new AlterSchemaStatement();
    }
    
    @Override
    public ASTNode visitAlterService(final AlterServiceContext ctx) {
        return new SQLServerAlterServiceStatement();
    }
    
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        return new DropSchemaStatement();
    }
    
    @Override
    public ASTNode visitDropService(final DropServiceContext ctx) {
        return new SQLServerDropServiceStatement();
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new AlterTriggerStatement();
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        return new AlterSequenceStatement(null);
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new AlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new AlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        AlterViewStatement result = new AlterViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.createOrAlterViewClause().select()));
        result.setSelect((SelectStatement) visit(ctx.createOrAlterViewClause().select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new AlterDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(null, false);
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new DropFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        DropViewStatement result = new DropViewStatement();
        result.setIfExists(null != ctx.ifExists());
        for (ViewNameContext each : ctx.viewName()) {
            result.getViews().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new DropTriggerStatement();
    }
    
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        return new DropSequenceStatement(Collections.emptyList());
    }
}
