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

package org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
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
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.SQLServerStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.AlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.DropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.service.SQLServerAlterServiceStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.service.SQLServerCreateServiceStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.service.SQLServerDropServiceStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * DDL statement visitor for SQLServer.
 */
public final class SQLServerDDLStatementVisitor extends SQLServerStatementVisitor implements DDLStatementVisitor {
    
    public SQLServerDDLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        return null == ctx.createTableClause() ? visit(ctx.createTableAsSelectClause()) : visit(ctx.createTableClause());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTableClause(final CreateTableClauseContext ctx) {
        Collection<ColumnDefinitionSegment> columnDefinitions = new LinkedList<>();
        Collection<ConstraintDefinitionSegment> constraintDefinitions = new LinkedList<>();
        CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) generateCreateDefinitionSegment(ctx.createDefinitionClause().createTableDefinitions());
        for (CreateDefinitionSegment each : createDefinitions.getValue()) {
            if (each instanceof ColumnDefinitionSegment) {
                columnDefinitions.add((ColumnDefinitionSegment) each);
            } else if (each instanceof ConstraintDefinitionSegment) {
                constraintDefinitions.add((ConstraintDefinitionSegment) each);
            }
        }
        return CreateTableStatement.builder()
                .databaseType(getDatabaseType())
                .table((SimpleTableSegment) visit(ctx.tableName()))
                .columnDefinitions(columnDefinitions)
                .constraintDefinitions(constraintDefinitions)
                .build();
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
        AlterTableStatement.AlterTableStatementBuilder result = AlterTableStatement.builder().databaseType(getDatabaseType()).table((SimpleTableSegment) visit(ctx.tableName()));
        for (AlterDefinitionClauseContext alterDefinitionClauseContext : ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(alterDefinitionClauseContext)).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.addColumnDefinition((AddColumnDefinitionSegment) each);
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.modifyColumnDefinition((ModifyColumnDefinitionSegment) each);
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.dropColumnDefinition((DropColumnDefinitionSegment) each);
                } else if (each instanceof AddConstraintDefinitionSegment) {
                    result.addConstraintDefinition((AddConstraintDefinitionSegment) each);
                } else if (each instanceof ModifyConstraintDefinitionSegment) {
                    result.modifyConstraintDefinition((ModifyConstraintDefinitionSegment) each);
                } else if (each instanceof DropConstraintDefinitionSegment) {
                    result.dropConstraintDefinition((DropConstraintDefinitionSegment) each);
                }
            }
        }
        return result.build();
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
        return new DropTableStatement(getDatabaseType(), ((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue(), null != ctx.ifExists(), false);
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        return new TruncateStatement(getDatabaseType(), Collections.singleton((SimpleTableSegment) visit(ctx.tableName())), Collections.emptyList());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        return CreateIndexStatement.builder()
                .databaseType(getDatabaseType())
                .table((SimpleTableSegment) visit(ctx.tableName()))
                .index((IndexSegment) visit(ctx.indexName()))
                .columns(((CollectionValue) visit(ctx.columnNamesWithSort())).getValue())
                .build();
    }
    
    @Override
    public ASTNode visitColumnNameWithSort(final ColumnNameWithSortContext ctx) {
        return visit(ctx.columnName());
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        return new AlterIndexStatement(getDatabaseType(), null == ctx.indexName() ? null : (IndexSegment) visit(ctx.indexName()), null, (SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        return DropIndexStatement.builder()
                .databaseType(getDatabaseType())
                .ifExists(null != ctx.ifExists())
                .indexes(Collections.singleton((IndexSegment) visit(ctx.indexName())))
                .simpleTable((SimpleTableSegment) visit(ctx.tableName()))
                .build();
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
        return new CreateDatabaseStatement(getDatabaseType(), ctx.databaseName().getText(), false);
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new CreateFunctionStatement(getDatabaseType(), null, null, Collections.emptyList());
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new CreateProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        CreateViewStatement result = new CreateViewStatement(getDatabaseType());
        result.setReplaceView(null != ctx.ALTER());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.createOrAlterViewClause().select()));
        result.setSelect((SelectStatement) visit(ctx.createOrAlterViewClause().select()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new CreateTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        return new CreateSequenceStatement(getDatabaseType(), ctx.sequenceName().name().getText());
    }
    
    @Override
    public ASTNode visitCreateSchema(final CreateSchemaContext ctx) {
        return new CreateSchemaStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateService(final CreateServiceContext ctx) {
        return new SQLServerCreateServiceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterSchema(final AlterSchemaContext ctx) {
        return new AlterSchemaStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterService(final AlterServiceContext ctx) {
        return new SQLServerAlterServiceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        return new DropSchemaStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropService(final DropServiceContext ctx) {
        return new SQLServerDropServiceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new AlterTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        return new AlterSequenceStatement(getDatabaseType(), null);
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new AlterProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new AlterFunctionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        AlterViewStatement result = new AlterViewStatement(getDatabaseType());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.createOrAlterViewClause().select()));
        result.setSelect((SelectStatement) visit(ctx.createOrAlterViewClause().select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new AlterDatabaseStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(getDatabaseType(), null, false);
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new DropFunctionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        return new DropViewStatement(getDatabaseType(), ctx.viewName().stream().map(each -> (SimpleTableSegment) visit(each)).collect(Collectors.toList()), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new DropTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        return new DropSequenceStatement(getDatabaseType(), Collections.emptyList());
    }
}
