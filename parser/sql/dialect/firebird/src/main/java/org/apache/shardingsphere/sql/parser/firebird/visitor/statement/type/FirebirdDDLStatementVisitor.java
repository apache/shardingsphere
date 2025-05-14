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

package org.apache.shardingsphere.sql.parser.firebird.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AlterDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AlterSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.AlterTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CheckConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CommentContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.ConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateCollationContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.DataTypeOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.DropConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.firebird.visitor.statement.FirebirdStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdAlterDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdAlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdAlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCommentStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdDropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.ddl.FirebirdExecuteStatement;

import java.util.Collections;

/**
 * DDL statement visitor for Firebird.
 */
public final class FirebirdDDLStatementVisitor extends FirebirdStatementVisitor implements DDLStatementVisitor {
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        FirebirdCreateTableStatement result = new FirebirdCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
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
        DataTypeSegment dataType = ctx.dataType() != null ? (DataTypeSegment) visit(ctx.dataType()) : null;
        boolean isPrimaryKey = ctx.dataTypeOption().stream().anyMatch(each -> null != each.primaryKey());
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
        FirebirdAlterTableStatement result = new FirebirdAlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.alterDefinitionClause()) {
            for (AlterDefinitionSegment each : ((CollectionValue<AlterDefinitionSegment>) visit(ctx.alterDefinitionClause())).getValue()) {
                if (each instanceof AddColumnDefinitionSegment) {
                    result.getAddColumnDefinitions().add((AddColumnDefinitionSegment) each);
                } else if (each instanceof ModifyColumnDefinitionSegment) {
                    result.getModifyColumnDefinitions().add((ModifyColumnDefinitionSegment) each);
                } else if (each instanceof DropColumnDefinitionSegment) {
                    result.getDropColumnDefinitions().add((DropColumnDefinitionSegment) each);
                } else if (each instanceof AddConstraintDefinitionSegment) {
                    result.getAddConstraintDefinitions().add((AddConstraintDefinitionSegment) each);
                } else if (each instanceof DropConstraintDefinitionSegment) {
                    result.getDropConstraintDefinitions().add((DropConstraintDefinitionSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterDomain(final AlterDomainContext ctx) {
        return new FirebirdAlterDomainStatement();
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
        ColumnSegment column = (ColumnSegment) visit(ctx.modifyColumn().columnName());
        DataTypeSegment dataType = null == ctx.dataType() ? null : (DataTypeSegment) visit(ctx.dataType());
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false, getText(ctx));
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singleton((ColumnSegment) visit(ctx.columnName())));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        FirebirdDropTableStatement result = new FirebirdDropTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new FirebirdCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new FirebirdCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new FirebirdAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        FirebirdAlterSequenceStatement result = new FirebirdAlterSequenceStatement();
        result.setSequenceName(((SimpleTableSegment) visit(ctx.tableName())).getTableName().getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateCollation(final CreateCollationContext ctx) {
        return new FirebirdCreateCollationStatement();
    }
    
    @Override
    public ASTNode visitCreateDomain(final CreateDomainContext ctx) {
        return new FirebirdCreateDomainStatement();
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new FirebirdAlterTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new FirebirdCreateTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        FirebirdCreateSequenceStatement result = new FirebirdCreateSequenceStatement();
        result.setSequenceName(((SimpleTableSegment) visit(ctx.tableName())).getTableName().getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitExecuteStmt(final ExecuteStmtContext ctx) {
        return new FirebirdExecuteStatement();
    }
    
    @Override
    public ASTNode visitComment(final CommentContext ctx) {
        FirebirdCommentStatement result = new FirebirdCommentStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.columnName()) {
            result.setColumn((ColumnSegment) visit(ctx.columnName()));
        }
        return result;
    }
}
