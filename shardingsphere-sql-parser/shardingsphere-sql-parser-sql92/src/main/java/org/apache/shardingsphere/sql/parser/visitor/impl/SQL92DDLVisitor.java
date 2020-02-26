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
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.GeneratedDataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.InlineDataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.visitor.SQL92Visitor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * DDL visitor for SQL92.
 */
public final class SQL92DDLVisitor extends SQL92Visitor implements DDLVisitor {
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        result.getTables().add((TableSegment) visit(ctx.tableName()));
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
        for (CreateDefinitionContext each : ctx.createDefinition()) {
            ColumnDefinitionContext columnDefinition = each.columnDefinition();
            if (null != columnDefinition) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(columnDefinition));
                Collection<TableSegment> tableSegments = getTableSegments(columnDefinition);
                result.getTables().addAll(tableSegments);
                result.getAllSQLSegments().addAll(tableSegments);
            }
            if (null != each.constraintDefinition() && null != each.constraintDefinition().foreignKeyOption()) {
                TableSegment tableSegment = (TableSegment) visit(each.constraintDefinition().foreignKeyOption().referenceDefinition().tableName());
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
        KeywordValue dataType = (KeywordValue) visit(ctx.dataType().dataTypeName());
        boolean isPrimaryKey = containsPrimaryKey(ctx);
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column.getIdentifier().getValue(), dataType.getValue(), isPrimaryKey);
    }
    
    private boolean containsPrimaryKey(final ColumnDefinitionContext ctx) {
        for (InlineDataTypeContext each : ctx.inlineDataType()) {
            if (null != each.commonDataTypeOption() && null != each.commonDataTypeOption().primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<TableSegment> getTableSegments(final ColumnDefinitionContext columnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (InlineDataTypeContext each : columnDefinition.inlineDataType()) {
            if (null != each.commonDataTypeOption() && null != each.commonDataTypeOption().referenceDefinition()) {
                result.add((TableSegment) visit(each.commonDataTypeOption().referenceDefinition().tableName()));
            }
        }
        for (GeneratedDataTypeContext each : columnDefinition.generatedDataType()) {
            if (null != each.commonDataTypeOption().referenceDefinition()) {
                result.add((TableSegment) visit(each.commonDataTypeOption().referenceDefinition().tableName()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        result.getTables().add((TableSegment) visit(ctx.tableName()));
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
        if (null != ctx.dropColumnSpecification()) {
            result.getDroppedColumnNames().addAll(((DropColumnDefinitionSegment) visit(ctx.dropColumnSpecification())).getColumnNames());
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
        AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                ctx.columnDefinition().getStart().getStartIndex(), ctx.columnDefinition().getStop().getStopIndex(), (ColumnDefinitionSegment) visit(ctx.columnDefinition()));
        result.getValue().add(addColumnDefinition);
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO visit column definition, need to change g4 for modifyColumn
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList(((ColumnSegment) visit(ctx.columnName())).getIdentifier().getValue()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        result.getTables().addAll(((CollectionValue<TableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
}
