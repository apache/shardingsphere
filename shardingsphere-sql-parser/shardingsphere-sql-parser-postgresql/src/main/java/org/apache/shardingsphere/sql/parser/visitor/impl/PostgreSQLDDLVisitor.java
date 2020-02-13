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

import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNameClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNamesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.visitor.PostgreSQLVisitor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * PostgreSQL DDL visitor.
 *
 * @author zhangliang
 */
public final class PostgreSQLDDLVisitor extends PostgreSQLVisitor {
    
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
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableNameClause());
        result.getTables().add(table);
        result.getAllSQLSegments().add(table);
        if (null != ctx.alterDefinitionClause_()) {
            AlterTableStatement alterDefinition = (AlterTableStatement) visit(ctx.alterDefinitionClause_());
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
    
//    @Override
//    public ASTNode visitAlterDefinitionClause_(final AlterDefinitionClause_Context ctx) {
//        final AlterTableStatement result = new AlterTableStatement();
//        for (AlterTableActionContext each : ctx.alterTableActions().alterTableAction()) {
//            AddColumnSpecificationContext addColumnSpecification = each.addColumnSpecification();
//            if (null != addColumnSpecification) {
//                CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
//                for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValue()) {
//                    result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
//                    Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
//                    if (columnPositionSegment.isPresent()) {
//                        result.getChangedPositionColumns().add(columnPositionSegment.get());
//                    }
//                }
//                result.getAllSQLSegments().addAll(extractColumnDefinitions(addColumnSpecification.columnDefinition()));
//            }
//            AddConstraintSpecificationContext addConstraintSpecification = each.addConstraintSpecification();
//            ForeignKeyOptionContext foreignKeyOption = null == addConstraintSpecification
//                    ? null : addConstraintSpecification.constraintDefinition().foreignKeyOption();
//            if (null != foreignKeyOption) {
//                result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption));
//            }
//            ChangeColumnSpecificationContext changeColumnSpecification = each.changeColumnSpecification();
//            if (null != changeColumnSpecification) {
//                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(changeColumnSpecification)).getColumnPosition();
//                if (columnPositionSegment.isPresent()) {
//                    result.getChangedPositionColumns().add(columnPositionSegment.get());
//                }
//                result.getAllSQLSegments().addAll(extractColumnDefinition(changeColumnSpecification.columnDefinition()));
//            }
//            DropColumnSpecificationContext dropColumnSpecification = each.dropColumnSpecification();
//            if (null != dropColumnSpecification) {
//                result.getDroppedColumnNames().add(((DropColumnDefinitionSegment) visit(dropColumnSpecification)).getColumnName());
//            }
//            ModifyColumnSpecificationContext modifyColumnSpecification = each.modifyColumnSpecification();
//            if (null != modifyColumnSpecification) {
//                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
//                if (columnPositionSegment.isPresent()) {
//                    result.getChangedPositionColumns().add(columnPositionSegment.get());
//                }
//                result.getAllSQLSegments().addAll(extractColumnDefinition(modifyColumnSpecification.columnDefinition()));
//            }
//        }
//        if (result.getAddedColumnDefinitions().isEmpty()) {
//            result.getAllSQLSegments().addAll(result.getAddedColumnDefinitions());
//        }
//        if (result.getChangedPositionColumns().isEmpty()) {
//            result.getAllSQLSegments().addAll(result.getChangedPositionColumns());
//        }
//        return result;
//    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.tableNames());
        result.getTables().addAll(tables.getValue());
        result.getAllSQLSegments().addAll(tables.getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.tableNamesClause());
        result.getTables().addAll(tables.getValue());
        result.getAllSQLSegments().addAll(tables.getValue());
        return result;
    }
    
    @Override
    public ASTNode visitTableNameClause(final TableNameClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitTableNamesClause(final TableNamesClauseContext ctx) {
        Collection<TableSegment> tableSegments = new LinkedList<>();
        for (int i = 0; i < ctx.tableNameClause().size(); i++) {
            tableSegments.add((TableSegment) visit(ctx.tableNameClause(i)));
        }
        CollectionValue<TableSegment> result = new CollectionValue<>();
        result.getValue().addAll(tableSegments);
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
        return new DropIndexStatement();
    }
    
//    @Override
//    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
//        Collection<InlineDataTypeContext> inlineDataTypes = Collections2.filter(ctx.inlineDataType(), new Predicate<InlineDataTypeContext>() {
//            
//            @Override
//            public boolean apply(final InlineDataTypeContext inlineDataType) {
//                return null != inlineDataType.commonDataTypeOption() && null != inlineDataType.commonDataTypeOption().primaryKey();
//            }
//        });
//        Collection<GeneratedDataTypeContext> generatedDataTypes = Collections2.filter(ctx.generatedDataType(), new Predicate<GeneratedDataTypeContext>() {
//            @Override
//            public boolean apply(final GeneratedDataTypeContext generatedDataType) {
//                return null != generatedDataType.commonDataTypeOption() && null != generatedDataType.commonDataTypeOption().primaryKey();
//            }
//        });
//        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
//        boolean isPrimaryKey = inlineDataTypes.size() > 0 || generatedDataTypes.size() > 0;
//        IdentifierValue dataType = (IdentifierValue) visit(ctx.dataType().dataTypeName());
//        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column.getIdentifier().getValue(), dataType.getValue(), isPrimaryKey);
//    }
    
//    @Override
//    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
//        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
//        List<AddColumnDefinitionSegment> addColumnDefinitions = Lists.transform(ctx.columnDefinition(), new Function<ColumnDefinitionContext, AddColumnDefinitionSegment>() {
//            @Override
//            public AddColumnDefinitionSegment apply(final ColumnDefinitionContext columnDefinition) {
//                return new AddColumnDefinitionSegment(columnDefinition.getStart().getStartIndex(),
//                        columnDefinition.getStop().getStopIndex(), (ColumnDefinitionSegment) visit(columnDefinition));
//            }
//        });
//        if (null == ctx.firstOrAfterColumn()) {
//            result.getValue().addAll(addColumnDefinitions);
//        } else {
//            AddColumnDefinitionSegment addColumnDefinition = addColumnDefinitions.get(0);
//            addColumnDefinition.setColumnPosition(extractColumnDefinition(addColumnDefinition.getColumnDefinition(),
//                    (ColumnPositionSegment) visit(ctx.firstOrAfterColumn())));
//            result.getValue().add(addColumnDefinition);
//        }
//        return result;
//    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ColumnSegment) visit(ctx.columnName())).getIdentifier().getValue());
    }
    
//    @Override
//    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
//        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
//    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ColumnSegment) visit(ctx.columnName(0))).getIdentifier().getValue(), ((ColumnSegment) visit(ctx.columnName(1))).getIdentifier().getValue());
    }
    
//    private ModifyColumnDefinitionSegment extractModifyColumnDefinition(final Token start, final Token stop, final ColumnDefinitionContext columnDefinition,
//                                                                  final FirstOrAfterColumnContext firstOrAfterColumn) {
//        ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(start.getStartIndex(), stop.getStopIndex(),
//                (ColumnDefinitionSegment) visit(columnDefinition));
//        if (null != firstOrAfterColumn) {
//            result.setColumnPosition(extractColumnDefinition(result.getColumnDefinition(), (ColumnPositionSegment) visit(firstOrAfterColumn)));
//        }
//        return result;
//    }
    
//    private ColumnPositionSegment extractColumnDefinition(final ColumnDefinitionSegment columnDefinition, final ColumnPositionSegment columnPosition) {
//        return columnPosition instanceof ColumnFirstPositionSegment
//                ? new ColumnFirstPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName())
//                : new ColumnAfterPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName(),
//                ((ColumnAfterPositionSegment) columnPosition).getAfterColumnName());
//    }
//    
//    private Collection<TableSegment> extractColumnDefinition(final ColumnDefinitionContext columnDefinition) {
//        Collection<TableSegment> result = new LinkedList<>();
//        for (InlineDataTypeContext inlineDataType : columnDefinition.inlineDataType()) {
//            if (null != inlineDataType.commonDataTypeOption() && null != inlineDataType.commonDataTypeOption().referenceDefinition()) {
//                result.add((TableSegment) visit(inlineDataType.commonDataTypeOption().referenceDefinition()));
//            }
//        }
//        for (GeneratedDataTypeContext generatedDataType : columnDefinition.generatedDataType()) {
//            if (null != generatedDataType.commonDataTypeOption() && null != generatedDataType.commonDataTypeOption().referenceDefinition()) {
//                result.add((TableSegment) visit(generatedDataType.commonDataTypeOption().referenceDefinition()));
//            }
//        }
//        return result;
//    }
}
