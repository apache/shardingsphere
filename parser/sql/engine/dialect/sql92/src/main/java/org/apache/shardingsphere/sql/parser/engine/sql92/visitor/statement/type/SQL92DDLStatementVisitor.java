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

package org.apache.shardingsphere.sql.parser.engine.sql92.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CheckConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DataTypeOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.engine.sql92.visitor.statement.SQL92StatementVisitor;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * DDL statement visitor for SQL92.
 */
public final class SQL92DDLStatementVisitor extends SQL92StatementVisitor implements DDLStatementVisitor {
    
    public SQL92DDLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        Collection<ColumnDefinitionSegment> columnDefinitions = new LinkedList<>();
        Collection<ConstraintDefinitionSegment> constraintDefinitions = new LinkedList<>();
        if (null != ctx.createDefinitionClause()) {
            CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) visit(ctx.createDefinitionClause());
            for (CreateDefinitionSegment each : createDefinitions.getValue()) {
                if (each instanceof ColumnDefinitionSegment) {
                    columnDefinitions.add((ColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    constraintDefinitions.add((ConstraintDefinitionSegment) each);
                }
            }
        }
        return CreateTableStatement.builder()
                .databaseType(getDatabaseType())
                .table((SimpleTableSegment) visit(ctx.tableName()))
                .columnDefinitions(columnDefinitions)
                .constraintDefinitions(constraintDefinitions)
                .build();
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        for (CreateDefinitionContext each : ctx.createDefinition()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.constraintDefinition()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.constraintDefinition()));
            }
            if (null != each.checkConstraintDefinition()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.checkConstraintDefinition()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = ctx.dataTypeOption().stream().anyMatch(each -> null != each.primaryKey());
        // TODO parse not null
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey, false, getText(ctx));
        for (DataTypeOptionContext each : ctx.dataTypeOption()) {
            if (null != each.referenceDefinition()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.referenceDefinition().tableName()));
            }
        }
        return result;
    }
    
    private String getText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
    
    @Override
    public ASTNode visitCheckConstraintDefinition(final CheckConstraintDefinitionContext ctx) {
        return new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitAddConstraintSpecification(final AddConstraintSpecificationContext ctx) {
        return new AddConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintDefinitionSegment) visit(ctx.constraintDefinition()));
    }
    
    @Override
    public ASTNode visitDropConstraintSpecification(final DropConstraintSpecificationContext ctx) {
        return new DropConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintDefinition().constraintName()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitConstraintDefinition(final ConstraintDefinitionContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintName()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintName()));
        }
        if (null != ctx.primaryKeyOption()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.primaryKeyOption().columnNames())).getValue());
        }
        if (null != ctx.foreignKeyOption()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.foreignKeyOption().referenceDefinition().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitConstraintName(final ConstraintNameContext ctx) {
        return new ConstraintSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement.AlterTableStatementBuilder result = AlterTableStatement.builder().databaseType(getDatabaseType()).table((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterDefinitionClause())).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.addColumnDefinition((AddColumnDefinitionSegment) each);
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.modifyColumnDefinition((ModifyColumnDefinitionSegment) each);
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.dropColumnDefinition((DropColumnDefinitionSegment) each);
                } else if (each instanceof AddConstraintDefinitionSegment) {
                    result.addConstraintDefinition((AddConstraintDefinitionSegment) each);
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
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singleton((ColumnSegment) visit(ctx.columnName())));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        return new DropTableStatement(getDatabaseType(), ((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue(), false, false);
    }
}
