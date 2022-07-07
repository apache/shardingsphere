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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DDLSQLVisitor;
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
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNamesWithSortContext;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterServiceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateServiceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropServiceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

/**
 * DDL Statement SQL visitor for SQLServer.
 */
@NoArgsConstructor
public final class SQLServerDDLStatementSQLVisitor extends SQLServerStatementSQLVisitor implements DDLSQLVisitor, SQLStatementVisitor {
    
    public SQLServerDDLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        return null == ctx.createTableClause() ? visit(ctx.createTableAsSelectClause()) : visit(ctx.createTableClause());
    }
    
    @Override
    public ASTNode visitCreateTableClause(final CreateTableClauseContext ctx) {
        SQLServerCreateTableStatement result = new SQLServerCreateTableStatement();
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
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey, false);
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
        SQLServerAlterTableStatement result = new SQLServerAlterTableStatement();
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
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false);
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
        SQLServerDropTableStatement result = new SQLServerDropTableStatement(null != ctx.ifExists());
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        SQLServerTruncateStatement result = new SQLServerTruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        SQLServerCreateIndexStatement result = new SQLServerCreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setIndex((IndexSegment) visit(ctx.indexName()));
        result.getColumns().addAll(((CollectionValue) visit(ctx.columnNamesWithSort())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitColumnNamesWithSort(final ColumnNamesWithSortContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (ColumnNameWithSortContext each : ctx.columnNameWithSort()) {
            result.getValue().add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnNameWithSort(final ColumnNameWithSortContext ctx) {
        return visit(ctx.columnName());
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        SQLServerAlterIndexStatement result = new SQLServerAlterIndexStatement();
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        }
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        SQLServerDropIndexStatement result = new SQLServerDropIndexStatement(null != ctx.ifExists());
        result.getIndexes().add((IndexSegment) visit(ctx.indexName()));
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
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
        SQLServerCreateDatabaseStatement result = new SQLServerCreateDatabaseStatement();
        result.setDatabaseName(ctx.databaseName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new SQLServerCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new SQLServerCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        SQLServerCreateViewStatement result = new SQLServerCreateViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setSelect((SQLServerSelectStatement) visit(ctx.createOrAlterViewClause().select()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new SQLServerCreateTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        return new SQLServerCreateSequenceStatement();
    }
    
    @Override
    public ASTNode visitCreateSchema(final CreateSchemaContext ctx) {
        return new SQLServerCreateSchemaStatement();
    }
    
    @Override
    public ASTNode visitCreateService(final CreateServiceContext ctx) {
        return new SQLServerCreateServiceStatement();
    }
    
    @Override
    public ASTNode visitAlterSchema(final AlterSchemaContext ctx) {
        return new SQLServerAlterSchemaStatement();
    }
    
    @Override
    public ASTNode visitAlterService(final AlterServiceContext ctx) {
        return new SQLServerAlterServiceStatement();
    }
    
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        return new SQLServerDropSchemaStatement();
    }
    
    @Override
    public ASTNode visitDropService(final DropServiceContext ctx) {
        return new SQLServerDropServiceStatement();
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new SQLServerAlterTriggerStatement();
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        return new SQLServerAlterSequenceStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new SQLServerAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new SQLServerAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        return new SQLServerAlterViewStatement();
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new SQLServerAlterDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new SQLServerDropDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new SQLServerDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new SQLServerDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        return new SQLServerDropViewStatement();
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new SQLServerDropTriggerStatement();
    }
    
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        return new SQLServerDropSequenceStatement();
    }
}
