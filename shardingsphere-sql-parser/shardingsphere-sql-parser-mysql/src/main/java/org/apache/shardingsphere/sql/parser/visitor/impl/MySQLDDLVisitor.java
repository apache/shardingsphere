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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.sql.parser.api.visitor.DDLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConstraintDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLikeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropPrimaryKeySpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FirstOrAfterColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ForeignKeyOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GeneratedDataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InlineDataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReferenceDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.DropPrimaryKeySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.visitor.MySQLVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DDL visitor for MySQL.
 */
public final class MySQLDDLVisitor extends MySQLVisitor implements DDLVisitor {
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getTables().add(table);
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
        if (null != ctx.createLikeClause()) {
            result.getTables().add((TableSegment) visit(ctx.createLikeClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        for (CreateDefinitionContext each : ctx.createDefinitions().createDefinition()) {
            ColumnDefinitionContext columnDefinition = each.columnDefinition();
            if (null != columnDefinition) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(columnDefinition));
                result.getAllSQLSegments().addAll(getTableSegments(columnDefinition));
            }
            ConstraintDefinitionContext constraintDefinition = each.constraintDefinition();
            ForeignKeyOptionContext foreignKeyOption = null == constraintDefinition ? null : constraintDefinition.foreignKeyOption();
            if (null != foreignKeyOption) {
                result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption));
            }
        }
        if (result.getColumnDefinitions().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getColumnDefinitions());
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateLikeClause(final CreateLikeClauseContext ctx) {
        return visit(ctx.tableName());
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
        for (AlterSpecificationContext each : ctx.alterSpecification()) {
            AddColumnSpecificationContext addColumnSpecification = each.addColumnSpecification();
            if (null != addColumnSpecification) {
                CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
                for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValue()) {
                    result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
                    Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
                    if (columnPositionSegment.isPresent()) {
                        result.getChangedPositionColumns().add(columnPositionSegment.get());
                    }
                }
                result.getAllSQLSegments().addAll(getTableSegments(addColumnSpecification.columnDefinition()));
            }
            AddConstraintSpecificationContext addConstraintSpecification = each.addConstraintSpecification();
            ForeignKeyOptionContext foreignKeyOption = null == addConstraintSpecification ? null : addConstraintSpecification.constraintDefinition().foreignKeyOption();
            if (null != foreignKeyOption) {
                result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption));
            }
            ChangeColumnSpecificationContext changeColumnSpecification = each.changeColumnSpecification();
            if (null != changeColumnSpecification) {
                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(changeColumnSpecification)).getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
                result.getAllSQLSegments().addAll(getTableSegments(changeColumnSpecification.columnDefinition()));
            }
            DropColumnSpecificationContext dropColumnSpecification = each.dropColumnSpecification();
            if (null != dropColumnSpecification) {
                result.getDroppedColumnNames().addAll(((DropColumnDefinitionSegment) visit(dropColumnSpecification)).getColumnNames());
            }
            ModifyColumnSpecificationContext modifyColumnSpecification = each.modifyColumnSpecification();
            if (null != modifyColumnSpecification) {
                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
                result.getAllSQLSegments().addAll(getTableSegments(modifyColumnSpecification.columnDefinition()));
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
        List<AddColumnDefinitionSegment> addColumnDefinitions = Lists.transform(ctx.columnDefinition(), new Function<ColumnDefinitionContext, AddColumnDefinitionSegment>() {
            
            @Override
            public AddColumnDefinitionSegment apply(final ColumnDefinitionContext columnDefinition) {
                return new AddColumnDefinitionSegment(columnDefinition.getStart().getStartIndex(), columnDefinition.getStop().getStopIndex(), (ColumnDefinitionSegment) visit(columnDefinition));
            }
        });
        if (null == ctx.firstOrAfterColumn()) {
            result.getValue().addAll(addColumnDefinitions);
        } else {
            AddColumnDefinitionSegment addColumnDefinition = addColumnDefinitions.get(0);
            addColumnDefinition.setColumnPosition(getColumnPositionSegment(addColumnDefinition.getColumnDefinition(), (ColumnPositionSegment) visit(ctx.firstOrAfterColumn())));
            result.getValue().add(addColumnDefinition);
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
        for (GeneratedDataTypeContext each : ctx.generatedDataType()) {
            if (null != each.commonDataTypeOption() && null != each.commonDataTypeOption().primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ASTNode visitChangeColumnSpecification(final ChangeColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList(((ColumnSegment) visit(ctx.columnName())).getIdentifier().getValue()));
    }
    
    @Override
    public ASTNode visitDropPrimaryKeySpecification(final DropPrimaryKeySpecificationContext ctx) {
        return new DropPrimaryKeySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ColumnSegment) visit(ctx.columnName(0))).getIdentifier().getValue(), ((ColumnSegment) visit(ctx.columnName(1))).getIdentifier().getValue());
    }
    
    @Override
    public ASTNode visitReferenceDefinition(final ReferenceDefinitionContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitForeignKeyOption(final ForeignKeyOptionContext ctx) {
        return visit(ctx.referenceDefinition());
    }
    
    private ModifyColumnDefinitionSegment extractModifyColumnDefinition(final Token start, final Token stop, 
                                                                        final ColumnDefinitionContext columnDefinition, final FirstOrAfterColumnContext firstOrAfterColumn) {
        ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(start.getStartIndex(), stop.getStopIndex(),
                (ColumnDefinitionSegment) visit(columnDefinition));
        if (null != firstOrAfterColumn) {
            result.setColumnPosition(getColumnPositionSegment(result.getColumnDefinition(), (ColumnPositionSegment) visit(firstOrAfterColumn)));
        }
        return result;
    }
    
    @Override
    public ASTNode visitFirstOrAfterColumn(final FirstOrAfterColumnContext ctx) {
        return null == ctx.columnName() ? new ColumnFirstPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null)
                : new ColumnAfterPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, ((ColumnSegment) visit(ctx.columnName())).getIdentifier().getValue());
    }
    
    private Collection<TableSegment> getTableSegments(final List<ColumnDefinitionContext> columnDefinitions) {
        Collection<TableSegment> result = new LinkedList<>();
        for (ColumnDefinitionContext each : columnDefinitions) {
            result.addAll(getTableSegments(each));
        }
        return result;
    }
    
    private Collection<TableSegment> getTableSegments(final ColumnDefinitionContext columnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (InlineDataTypeContext each : columnDefinition.inlineDataType()) {
            if (null != each.commonDataTypeOption() && null != each.commonDataTypeOption().referenceDefinition()) {
                result.add((TableSegment) visit(each.commonDataTypeOption().referenceDefinition()));
            }
        }
        for (GeneratedDataTypeContext each : columnDefinition.generatedDataType()) {
            if (null != each.commonDataTypeOption() && null != each.commonDataTypeOption().referenceDefinition()) {
                result.add((TableSegment) visit(each.commonDataTypeOption().referenceDefinition()));
            }
        }
        return result;
    }
    
    private ColumnPositionSegment getColumnPositionSegment(final ColumnDefinitionSegment columnDefinition, final ColumnPositionSegment columnPosition) {
        return columnPosition instanceof ColumnFirstPositionSegment
                ? new ColumnFirstPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName())
                : new ColumnAfterPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName(),
                ((ColumnAfterPositionSegment) columnPosition).getAfterColumnName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        result.getTables().addAll(((CollectionValue<TableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        result.getTables().add((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement();
        result.setTable((TableSegment) visit(ctx.tableName()));
        return result;
    }
}
