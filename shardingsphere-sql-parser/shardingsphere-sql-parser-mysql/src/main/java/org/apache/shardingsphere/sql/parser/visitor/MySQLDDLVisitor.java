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

package org.apache.shardingsphere.sql.parser.visitor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.sql.parser.MySQLVisitor;
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
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL DDL visitor.
 *
 * @author panjuan
 */
public final class MySQLDDLVisitor extends MySQLVisitor {
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        CreateDefinitionClauseContext createDefinitionClause = ctx.createDefinitionClause();
        if (null != createDefinitionClause) {
            CreateTableStatement createDefinition = (CreateTableStatement) visit(createDefinitionClause);
            result.getColumnDefinitions().addAll(createDefinition.getColumnDefinitions());
            result.getAllSQLSegments().addAll(createDefinition.getAllSQLSegments());
        }
        CreateLikeClauseContext createLikeClause = ctx.createLikeClause();
        if (null != createLikeClause) {
            result.getAllSQLSegments().add((TableSegment) visit(createLikeClause));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        AlterDefinitionClauseContext alterDefinitionClause = ctx.alterDefinitionClause();
        if (null != alterDefinitionClause) {
            AlterTableStatement alterDefinition = (AlterTableStatement) visit(alterDefinitionClause);
            result.getAddedColumnDefinitions().addAll(alterDefinition.getAddedColumnDefinitions());
            result.getChangedPositionColumns().addAll(alterDefinition.getChangedPositionColumns());
            result.getDroppedColumnNames().addAll(alterDefinition.getDroppedColumnNames());
            result.getAllSQLSegments().addAll(alterDefinition.getAllSQLSegments());
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.tableNames());
        result.getTables().addAll(tables.getValue());
        result.getAllSQLSegments().addAll(tables.getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getAllSQLSegments().add(table);
        result.getTables().add(table);
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
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        IdentifierValue dataType = (IdentifierValue) visit(ctx.dataType().dataTypeName());
        Collection<InlineDataTypeContext> inlineDataTypes = Collections2.filter(ctx.inlineDataType(), new Predicate<InlineDataTypeContext>() {
            
            @Override
            public boolean apply(final InlineDataTypeContext inlineDataType) {
                return null != inlineDataType.commonDataTypeOption() && null != inlineDataType.commonDataTypeOption().primaryKey();
            }
        });
        Collection<GeneratedDataTypeContext> generatedDataTypes = Collections2.filter(ctx.generatedDataType(), new Predicate<GeneratedDataTypeContext>() {
            @Override
            public boolean apply(final GeneratedDataTypeContext generatedDataType) {
                return null != generatedDataType.commonDataTypeOption()
                        && null != generatedDataType.commonDataTypeOption().primaryKey();
            }
        });
        boolean isPrimaryKey = inlineDataTypes.size() > 0 || generatedDataTypes.size() > 0;
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                column.getName(), dataType.getValue(), isPrimaryKey);
    }
    
    @Override
    public ASTNode visitFirstOrAfterColumn(final FirstOrAfterColumnContext ctx) {
        return null == ctx.columnName() ? new ColumnFirstPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null)
                : new ColumnAfterPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null,
                ((ColumnSegment) visit(ctx.columnName())).getName());
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        for (CreateDefinitionContext createDefinition : ctx.createDefinitions().createDefinition()) {
            ColumnDefinitionContext columnDefinition = createDefinition.columnDefinition();
            if (null != columnDefinition) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(columnDefinition));
                result.getAllSQLSegments().addAll(extractColumnDefinition(columnDefinition));
            }
            ConstraintDefinitionContext constraintDefinition = createDefinition.constraintDefinition();
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
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        final AlterTableStatement result = new AlterTableStatement();
        for (AlterSpecificationContext alterSpecification : ctx.alterSpecification()) {
            AddColumnSpecificationContext addColumnSpecification = alterSpecification.addColumnSpecification();
            if (null != addColumnSpecification) {
                CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
                for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValue()) {
                    result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
                    Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
                    if (columnPositionSegment.isPresent()) {
                        result.getChangedPositionColumns().add(columnPositionSegment.get());
                    }
                }
                result.getAllSQLSegments().addAll(extractColumnDefinitions(addColumnSpecification.columnDefinition()));
            }
            AddConstraintSpecificationContext addConstraintSpecification = alterSpecification.addConstraintSpecification();
            ForeignKeyOptionContext foreignKeyOption = null == addConstraintSpecification
                    ? null : addConstraintSpecification.constraintDefinition().foreignKeyOption();
            if (null != foreignKeyOption) {
                result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption));
            }
            ChangeColumnSpecificationContext changeColumnSpecification = alterSpecification.changeColumnSpecification();
            if (null != changeColumnSpecification) {
                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(changeColumnSpecification)).getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
                result.getAllSQLSegments().addAll(extractColumnDefinition(changeColumnSpecification.columnDefinition()));
            }
            DropColumnSpecificationContext dropColumnSpecification = alterSpecification.dropColumnSpecification();
            if (null != dropColumnSpecification) {
                result.getDroppedColumnNames().add(((DropColumnDefinitionSegment) visit(dropColumnSpecification)).getColumnName());
            }
            ModifyColumnSpecificationContext modifyColumnSpecification = alterSpecification.modifyColumnSpecification();
            if (null != modifyColumnSpecification) {
                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
                result.getAllSQLSegments().addAll(extractColumnDefinition(modifyColumnSpecification.columnDefinition()));
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
                return new AddColumnDefinitionSegment(columnDefinition.getStart().getStartIndex(),
                        columnDefinition.getStop().getStopIndex(), (ColumnDefinitionSegment) visit(columnDefinition));
            }
        });
        if (null == ctx.firstOrAfterColumn()) {
            result.getValue().addAll(addColumnDefinitions);
        } else {
            AddColumnDefinitionSegment addColumnDefinition = addColumnDefinitions.get(0);
            addColumnDefinition.setColumnPosition(extractColumnDefinition(addColumnDefinition.getColumnDefinition(),
                    (ColumnPositionSegment) visit(ctx.firstOrAfterColumn())));
            result.getValue().add(addColumnDefinition);
        }
        return result;
    }
    
    @Override
    public ASTNode visitChangeColumnSpecification(final ChangeColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ColumnSegment) visit(ctx.columnName())).getName());
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
                ((ColumnSegment) visit(ctx.columnName(0))).getName(), ((ColumnSegment) visit(ctx.columnName(1))).getName());
    }
    
    @Override
    public ASTNode visitReferenceDefinition(final ReferenceDefinitionContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitForeignKeyOption(final ForeignKeyOptionContext ctx) {
        return visit(ctx.referenceDefinition());
    }
    
    private ModifyColumnDefinitionSegment extractModifyColumnDefinition(final Token start, final Token stop, final ColumnDefinitionContext columnDefinition,
                                                                  final FirstOrAfterColumnContext firstOrAfterColumn) {
        ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(start.getStartIndex(), stop.getStopIndex(),
                (ColumnDefinitionSegment) visit(columnDefinition));
        if (null != firstOrAfterColumn) {
            result.setColumnPosition(extractColumnDefinition(result.getColumnDefinition(), (ColumnPositionSegment) visit(firstOrAfterColumn)));
        }
        return result;
    }
    
    private ColumnPositionSegment extractColumnDefinition(final ColumnDefinitionSegment columnDefinition, final ColumnPositionSegment columnPosition) {
        return columnPosition instanceof ColumnFirstPositionSegment
                ? new ColumnFirstPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName())
                : new ColumnAfterPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName(),
                ((ColumnAfterPositionSegment) columnPosition).getAfterColumnName());
    }
    
    private Collection<TableSegment> extractColumnDefinition(final ColumnDefinitionContext columnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (InlineDataTypeContext inlineDataType : columnDefinition.inlineDataType()) {
            if (null != inlineDataType.commonDataTypeOption() && null != inlineDataType.commonDataTypeOption().referenceDefinition()) {
                result.add((TableSegment) visit(inlineDataType.commonDataTypeOption().referenceDefinition()));
            }
        }
        for (GeneratedDataTypeContext generatedDataType : columnDefinition.generatedDataType()) {
            if (null != generatedDataType.commonDataTypeOption() && null != generatedDataType.commonDataTypeOption().referenceDefinition()) {
                result.add((TableSegment) visit(generatedDataType.commonDataTypeOption().referenceDefinition()));
            }
        }
        return result;
    }
    
    private Collection<TableSegment> extractColumnDefinitions(final List<ColumnDefinitionContext> columnDefinitions) {
        Collection<TableSegment> result = new LinkedList<>();
        for (ColumnDefinitionContext columnDefinition : columnDefinitions) {
            result.addAll(extractColumnDefinition(columnDefinition));
        }
        return result;
    }
}
