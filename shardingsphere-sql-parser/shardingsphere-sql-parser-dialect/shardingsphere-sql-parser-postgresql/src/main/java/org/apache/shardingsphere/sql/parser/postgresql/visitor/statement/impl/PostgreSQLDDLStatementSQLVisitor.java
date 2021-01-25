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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTableActionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNameClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNamesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLTruncateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

/**
 * DDL Statement SQL visitor for PostgreSQL.
 */
@NoArgsConstructor
public final class PostgreSQLDDLStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DDLSQLVisitor, SQLStatementVisitor {
    
    public PostgreSQLDDLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        PostgreSQLCreateTableStatement result = new PostgreSQLCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setNotExisted(null != ctx.tableNotExistClause());
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
        for (CreateDefinitionContext each : ctx.createDefinition()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.tableConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.tableConstraint()));
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        PostgreSQLAlterTableStatement result = new PostgreSQLAlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableNameClause().tableName()));
        if (null != ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterDefinitionClause())).getValue()) {
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
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.alterTableActions()) {
            for (AlterTableActionContext each : ctx.alterTableActions().alterTableAction()) {
                AddColumnSpecificationContext addColumnSpecification = each.addColumnSpecification();
                if (null != addColumnSpecification) {
                    result.getValue().addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification)).getValue());
                }
                if (null != each.addConstraintSpecification() && null != each.addConstraintSpecification().tableConstraint()) {
                    result.getValue().add((ConstraintDefinitionSegment) visit(each.addConstraintSpecification().tableConstraint()));
                }
                if (null != each.modifyColumnSpecification()) {
                    result.getValue().add((ModifyColumnDefinitionSegment) visit(each.modifyColumnSpecification()));
                }
                if (null != each.dropColumnSpecification()) {
                    result.getValue().add((DropColumnDefinitionSegment) visit(each.dropColumnSpecification()));
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
        ColumnDefinitionContext columnDefinition = ctx.columnDefinition();
        if (null != columnDefinition) {
            AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                    ctx.columnDefinition().getStart().getStartIndex(), columnDefinition.getStop().getStopIndex(), Collections.singletonList((ColumnDefinitionSegment) visit(columnDefinition)));
            result.getValue().add(addColumnDefinition);
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey);
        for (ColumnConstraintContext each : ctx.columnConstraint()) {
            if (null != each.columnConstraintOption().tableName()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.columnConstraintOption().tableName()));
            }
        }
        return result;
    }
    
    private boolean isPrimaryKey(final ColumnDefinitionContext ctx) {
        for (ColumnConstraintContext each : ctx.columnConstraint()) {
            if (null != each.columnConstraintOption() && null != each.columnConstraintOption().primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTableConstraint(final TableConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.tableConstraintOption().primaryKey()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.tableConstraintOption().columnNames(0))).getValue());
        }
        if (null != ctx.tableConstraintOption().FOREIGN()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.tableConstraintOption().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO visit pk and table ref
        ColumnSegment column = (ColumnSegment) visit(ctx.modifyColumn().columnName());
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType());
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false);
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList((ColumnSegment) visit(ctx.columnName())));
    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnSegment) visit(ctx.columnName(0)), (ColumnSegment) visit(ctx.columnName(1)));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        PostgreSQLDropTableStatement result = new PostgreSQLDropTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        PostgreSQLTruncateStatement result = new PostgreSQLTruncateStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNamesClause())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        PostgreSQLCreateIndexStatement result = new PostgreSQLCreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        PostgreSQLAlterIndexStatement result = new PostgreSQLAlterIndexStatement();
        result.setIndex((IndexSegment) visit(ctx.indexName()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        PostgreSQLDropIndexStatement result = new PostgreSQLDropIndexStatement();
        result.getIndexes().addAll(((CollectionValue<IndexSegment>) visit(ctx.indexNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitIndexNames(final IndexNamesContext ctx) {
        CollectionValue<IndexSegment> result = new CollectionValue<>();
        for (IndexNameContext each : ctx.indexName()) {
            result.getValue().add((IndexSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableNameClause(final TableNameClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitTableNamesClause(final TableNamesClauseContext ctx) {
        Collection<SimpleTableSegment> tableSegments = new LinkedList<>();
        for (int i = 0; i < ctx.tableNameClause().size(); i++) {
            tableSegments.add((SimpleTableSegment) visit(ctx.tableNameClause(i)));
        }
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        result.getValue().addAll(tableSegments);
        return result;
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new PostgreSQLAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new PostgreSQLAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new PostgreSQLCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new PostgreSQLCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new PostgreSQLDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        return new PostgreSQLDropViewStatement();
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        return new PostgreSQLCreateViewStatement();
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        return new PostgreSQLAlterViewStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        PostgreSQLDropDatabaseStatement result = new PostgreSQLDropDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new PostgreSQLDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        PostgreSQLCreateDatabaseStatement result = new PostgreSQLCreateDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        return result;
    }
}
