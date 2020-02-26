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
import org.apache.shardingsphere.sql.parser.api.visitor.DDLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnOrVirtualDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropColumnClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InlineConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OperateColumnClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OutOfLineConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OutOfLineRefConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RelationalPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.VirtualColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
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
import org.apache.shardingsphere.sql.parser.visitor.OracleVisitor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * DDL visitor for Oracle.
 */
public final class OracleDDLVisitor extends OracleVisitor implements DDLVisitor {
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        result.getTables().add((TableSegment) visit(ctx.tableName()));
        if (null != ctx.createDefinitionClause()) {
            CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) visit(ctx.createDefinitionClause());
            for (CreateDefinitionSegment each : createDefinitions.getValue()) {
                if (each instanceof ColumnDefinitionSegment) {
                    result.getColumnDefinitions().add((ColumnDefinitionSegment) each);
                    result.getTables().addAll(((ColumnDefinitionSegment) each).getReferencedTables());
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getConstraintDefinitions().add((ConstraintDefinitionSegment) each);
                    // CHECKSTYLE:OFF
                    if (((ConstraintDefinitionSegment) each).getReferencedTable().isPresent()) {
                        // CHECKSTYLE:ON
                        result.getTables().add(((ConstraintDefinitionSegment) each).getReferencedTable().get());
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        for (RelationalPropertyContext each : ctx.relationalProperties().relationalProperty()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.outOfLineConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.outOfLineConstraint()));
            }
            if (null != each.outOfLineRefConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.outOfLineRefConstraint()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        IdentifierValue dataType = (IdentifierValue) visit(ctx.dataType().dataTypeName());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column.getIdentifier().getValue(), dataType.getValue(), isPrimaryKey);
        for (InlineConstraintContext each : ctx.inlineConstraint()) {
            if (null != each.referencesClause()) {
                result.getReferencedTables().add((TableSegment) visit(each.referencesClause().tableName()));
            }
        }
        if (null != ctx.inlineRefConstraint()) {
            result.getReferencedTables().add((TableSegment) visit(ctx.inlineRefConstraint().tableName()));
        }
        return result;
    }
    
    private boolean isPrimaryKey(final ColumnDefinitionContext ctx) {
        for (InlineConstraintContext each : ctx.inlineConstraint()) {
            if (null != each.primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<TableSegment> getTableSegments(final ColumnDefinitionContext columnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (InlineConstraintContext each : columnDefinition.inlineConstraint()) {
            if (null != each.referencesClause()) {
                result.add((TableSegment) visit(each.referencesClause().tableName()));
            }
        }
        if (null != columnDefinition.inlineRefConstraint()) {
            result.add((TableSegment) visit(columnDefinition.inlineRefConstraint().tableName()));
        }
        return result;
    }
    
    private Collection<TableSegment> getTableSegments(final VirtualColumnDefinitionContext virtualColumnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (InlineConstraintContext each : virtualColumnDefinition.inlineConstraint()) {
            if (null != each.referencesClause()) {
                result.add((TableSegment) visit(each.referencesClause().tableName()));
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitOutOfLineConstraint(final OutOfLineConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.primaryKey()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue());
        }
        if (null != ctx.referencesClause()) {
            result.setReferencedTable((TableSegment) visit(ctx.referencesClause().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitOutOfLineRefConstraint(final OutOfLineRefConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.referencesClause()) {
            result.setReferencedTable((TableSegment) visit(ctx.referencesClause().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getTables().add(table);
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
        if (null != ctx.columnClauses()) {
            for (OperateColumnClauseContext each : ctx.columnClauses().operateColumnClause()) {
                AddColumnSpecificationContext addColumnSpecification = each.addColumnSpecification();
                if (null != addColumnSpecification) {
                    CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
                    for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValue()) {
                        result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
                        Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
                        // TODO refactor SQLStatement
                        // CHECKSTYLE:OFF
                        if (columnPositionSegment.isPresent()) {
                            result.getChangedPositionColumns().add(columnPositionSegment.get());
                        }
                        // CHECKSTYLE:ON
                    }
                    for (ColumnOrVirtualDefinitionContext columnOrVirtualDefinition : addColumnSpecification.columnOrVirtualDefinitions().columnOrVirtualDefinition()) {
                        // TODO refactor SQLStatement
                        // CHECKSTYLE:OFF
                        if (null != columnOrVirtualDefinition.columnDefinition()) {
                            result.getAllSQLSegments().addAll(getTableSegments(columnOrVirtualDefinition.columnDefinition()));
                        }
                        if (null != columnOrVirtualDefinition.virtualColumnDefinition()) {
                            result.getAllSQLSegments().addAll(getTableSegments(columnOrVirtualDefinition.virtualColumnDefinition()));
                        }
                        // CHECKSTYLE:ON
                    }
                }
                ModifyColumnSpecificationContext modifyColumnSpecification = each.modifyColumnSpecification();
                if (null != modifyColumnSpecification) {
                    Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
                    // TODO refactor SQLStatement
                    // CHECKSTYLE:OFF
                    if (columnPositionSegment.isPresent()) {
                        result.getChangedPositionColumns().add(columnPositionSegment.get());
                    }
                    // CHECKSTYLE:ON
                }
                DropColumnClauseContext dropColumnClause = each.dropColumnClause();
                if (null != dropColumnClause) {
                    result.getDroppedColumnNames().addAll(((DropColumnDefinitionSegment) visit(dropColumnClause)).getColumnNames());
                }
            }
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
        for (ColumnOrVirtualDefinitionContext each : ctx.columnOrVirtualDefinitions().columnOrVirtualDefinition()) {
            if (null != each.columnDefinition()) {
                AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                        each.columnDefinition().getStart().getStartIndex(), each.columnDefinition().getStop().getStopIndex(), (ColumnDefinitionSegment) visit(each.columnDefinition()));
                result.getValue().add(addColumnDefinition);
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
        for (ColumnNameContext each : ctx.columnOrColumnList().columnName()) {
            columnNames.add(((ColumnSegment) visit(each)).getIdentifier().getValue());
        }
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnNames);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        result.getTables().add((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        result.getTables().add((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        if (null != ctx.createIndexDefinitionClause().tableIndexClause()) {
            result.setTable((TableSegment) visit(ctx.createIndexDefinitionClause().tableIndexClause().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        return new AlterIndexStatement();
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        return new DropIndexStatement();
    }
}
