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

package org.apache.shardingsphere.sql.parser.visitor.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterColumnAddOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnDefinitionOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.visitor.SQLServerVisitor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQLServer DDL visitor.
 */
public final class SQLServerDDLVisitor extends SQLServerVisitor {
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getTables().add(table);
        result.getAllSQLSegments().add(table);
        if (null != ctx.createDefinitionClause()) {
            CreateTableStatement createDefinition = (CreateTableStatement) visit(ctx.createDefinitionClause());
            result.getColumnDefinitions().addAll(createDefinition.getColumnDefinitions());
            for (SQLSegment each : createDefinition.getAllSQLSegments()) {
                result.getAllSQLSegments().add(each);
                if (each instanceof TableSegment) {
                    result.getTables().add((TableSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        for (CreateTableDefinitionContext each : ctx.createTableDefinitions().createTableDefinition()) {
            ColumnDefinitionContext columnDefinition = each.columnDefinition();
            if (null != columnDefinition) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(columnDefinition));
                Collection<TableSegment> tableSegments = getTableSegments(columnDefinition);
                result.getTables().addAll(tableSegments);
                result.getAllSQLSegments().addAll(tableSegments);
            }
            if (null != each.tableConstraint() && null != each.tableConstraint().tableForeignKeyConstraint()) {
                TableSegment tableSegment = (TableSegment) visit(each.tableConstraint().tableForeignKeyConstraint().tableName());
                result.getTables().add(tableSegment);
                result.getAllSQLSegments().add(tableSegment);
            }
        }
        if (result.getColumnDefinitions().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getColumnDefinitions());
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        IdentifierValue dataType = (IdentifierValue) visit(ctx.dataType().dataTypeName());
        boolean isPrimaryKey = containsPrimaryKey(ctx);
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column.getIdentifier().getValue(), dataType.getValue(), isPrimaryKey);
    }
    
    private boolean containsPrimaryKey(final ColumnDefinitionContext ctx) {
        // CHECKSTYLE:OFF
        for (ColumnDefinitionOptionContext each : ctx.columnDefinitionOption()) {
            // CHECKSTYLE:ON
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
    
    private Collection<TableSegment> getTableSegments(final ColumnDefinitionContext columnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (ColumnDefinitionOptionContext each : columnDefinition.columnDefinitionOption()) {
            for (ColumnConstraintContext columnConstraint : each.columnConstraint()) {
                if (null != columnConstraint.columnForeignKeyConstraint()) {
                    result.add((TableSegment) visit(columnConstraint.columnForeignKeyConstraint().tableName()));
                }
            }
        }
        for (ColumnConstraintContext each : columnDefinition.columnConstraints().columnConstraint()) {
            if (null != each.columnForeignKeyConstraint()) {
                result.add((TableSegment) visit(each.columnForeignKeyConstraint().tableName()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getTables().add(table);
        result.getAllSQLSegments().add(table);
        if (null != ctx.alterDefinitionClause()) {
            AlterTableStatement alterDefinition = (AlterTableStatement) visit(ctx.alterDefinitionClause());
            result.getAddedColumnDefinitions().addAll(alterDefinition.getAddedColumnDefinitions());
            result.getChangedPositionColumns().addAll(alterDefinition.getChangedPositionColumns());
            result.getDroppedColumnNames().addAll(alterDefinition.getDroppedColumnNames());
            for (SQLSegment each : alterDefinition.getAllSQLSegments()) {
                result.getAllSQLSegments().add(each);
                if (each instanceof TableSegment) {
                    result.getTables().add((TableSegment) each);
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        final AlterTableStatement result = new AlterTableStatement();
        AddColumnSpecificationContext addColumnSpecification = ctx.addColumnSpecification();
        if (null != addColumnSpecification) {
            CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
            for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValue()) {
                result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
                Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
            }
        }
        ModifyColumnSpecificationContext modifyColumnSpecification = ctx.modifyColumnSpecification();
        if (null != modifyColumnSpecification) {
            Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
            if (columnPositionSegment.isPresent()) {
                result.getChangedPositionColumns().add(columnPositionSegment.get());
            }
        }
        if (null != ctx.alterDrop() && null != ctx.alterDrop().dropColumnSpecification()) {
            result.getDroppedColumnNames().addAll(((DropColumnDefinitionSegment) visit(ctx.alterDrop().dropColumnSpecification())).getColumnNames());
        }
        if (result.getAddedColumnDefinitions().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getAddedColumnDefinitions());
        }
        if (result.getChangedPositionColumns().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getChangedPositionColumns());
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
                            each.columnDefinition().getStart().getStartIndex(), each.columnDefinition().getStop().getStopIndex(), (ColumnDefinitionSegment) visit(each.columnDefinition()));
                    result.getValue().add(addColumnDefinition);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO visit column definition, need to change g4 for modifyColumn
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        Collection<String> columnNames = new LinkedList<>();
        for (ColumnNameContext each : ctx.columnName()) {
            columnNames.add(((ColumnSegment) visit(each)).getIdentifier().getValue());
        }
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnNames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        Collection<TableSegment> tables = ((CollectionValue<TableSegment>) visit(ctx.tableNames())).getValue();
        result.getTables().addAll(tables);
        result.getAllSQLSegments().addAll(tables);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getTables().add(table);
        result.getAllSQLSegments().add(table);
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        return result;
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        AlterIndexStatement result = new AlterIndexStatement();
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        }
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement();
        result.getIndexes().add((IndexSegment) visit(ctx.indexName()));
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
}
