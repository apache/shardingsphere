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

import org.apache.shardingsphere.sql.parser.api.visitor.DDLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DataTypeOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.visitor.SQL92Visitor;

import java.util.Collections;

/**
 * DDL visitor for SQL92.
 */
public final class SQL92DDLVisitor extends SQL92Visitor implements DDLVisitor {
    
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
        CreateTableStatement result = new CreateTableStatement();
        for (CreateDefinitionContext each : ctx.createDefinition()) {
            if (null != each.columnDefinition()) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.constraintDefinition()) {
                result.getConstraintDefinitions().add((ConstraintDefinitionSegment) visit(each.constraintDefinition()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        KeywordValue dataType = (KeywordValue) visit(ctx.dataType().dataTypeName());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column.getIdentifier().getValue(), dataType.getValue(), isPrimaryKey);
        for (DataTypeOptionContext each : ctx.dataTypeOption()) {
            if (null != each.referenceDefinition()) {
                result.getReferencedTables().add((TableSegment) visit(each.referenceDefinition().tableName()));
            }
        }
        return result;
    }
    
    private boolean isPrimaryKey(final ColumnDefinitionContext ctx) {
        for (DataTypeOptionContext each : ctx.dataTypeOption()) {
            if (null != each.primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitConstraintDefinition(final ConstraintDefinitionContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.primaryKeyOption()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.primaryKeyOption().columnNames())).getValue());
        }
        if (null != ctx.foreignKeyOption()) {
            result.setReferencedTable((TableSegment) visit(ctx.foreignKeyOption().referenceDefinition().tableName()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getTables().add(table);
        if (null != ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterDefinitionClause())).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.getAddColumnDefinitions().add((AddColumnDefinitionSegment) each);
                    for (ColumnDefinitionSegment columnDefinition : ((AddColumnDefinitionSegment) each).getColumnDefinitions()) {
                        result.getTables().addAll(columnDefinition.getReferencedTables());
                    }
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.getModifyColumnDefinitions().add((ModifyColumnDefinitionSegment) each);
                    result.getTables().addAll(((ModifyColumnDefinitionSegment) each).getColumnDefinition().getReferencedTables());
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.getDropColumnDefinitions().add((DropColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getAddConstraintDefinitions().add((ConstraintDefinitionSegment) each);
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
        if (null != ctx.dropColumnSpecification()) {
            result.getValue().add((DropColumnDefinitionSegment) visit(ctx.dropColumnSpecification()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
        AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                ctx.columnDefinition().getStart().getStartIndex(), ctx.columnDefinition().getStop().getStopIndex(), 
                Collections.singletonList((ColumnDefinitionSegment) visit(ctx.columnDefinition())));
        result.getValue().add(addColumnDefinition);
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO visit pk and table ref
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnDefinitionSegment) visit(ctx.columnDefinition()));
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList((ColumnSegment) visit(ctx.columnName())));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        result.getTables().addAll(((CollectionValue<TableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
}
