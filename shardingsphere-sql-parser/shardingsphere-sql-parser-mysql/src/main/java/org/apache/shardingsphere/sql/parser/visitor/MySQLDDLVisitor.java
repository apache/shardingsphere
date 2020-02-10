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

import org.apache.shardingsphere.sql.parser.MySQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterSpecification_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommonDataTypeOption_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConstraintDefinition_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinition_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLikeClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FirstOrAfterColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ForeignKeyOption_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GeneratedDataType_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IndexDefinition_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InlineDataType_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.LiteralValue;

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
        CreateDefinitionClause_Context createDefinitionClause = ctx.createDefinitionClause_();
        if (null != createDefinitionClause) {
            for (CreateDefinition_Context createDefinition : createDefinitionClause.createDefinitions_().createDefinition_()) {
                ColumnDefinitionContext columnDefinition = createDefinition.columnDefinition();
                if (null != columnDefinition) {
                    ColumnDefinitionSegment columnDefinitionSegment = createColumnDefinitionSegment(columnDefinition, result);
                    result.getColumnDefinitions().add(columnDefinitionSegment);
                    result.getAllSQLSegments().add(columnDefinitionSegment);
                }
                ConstraintDefinition_Context constraintDefinition = createDefinition.constraintDefinition_();
                ForeignKeyOption_Context foreignKeyOption = null == constraintDefinition ? null : constraintDefinition.foreignKeyOption_();
                if (null != foreignKeyOption) {
                    result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption.referenceDefinition_().tableName()));
                }
            }
        }
        CreateLikeClause_Context createLikeClause = ctx.createLikeClause_();
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
        if (null != ctx.alterDefinitionClause_()) {
            for (AlterSpecification_Context alterSpecification : ctx.alterDefinitionClause_().alterSpecification_()) {
                AddColumnSpecificationContext addColumnSpecification = alterSpecification.addColumnSpecification();
                if (null != addColumnSpecification) {
                    List<ColumnDefinitionContext> columnDefinitions = addColumnSpecification.columnDefinition();
                    ColumnDefinitionSegment columnDefinitionSegment = null;
                    for (ColumnDefinitionContext columnDefinition : columnDefinitions) {
                        columnDefinitionSegment = createColumnDefinitionSegment(columnDefinition, result);
                        result.getAddedColumnDefinitions().add(columnDefinitionSegment);
                        result.getAllSQLSegments().add(columnDefinitionSegment);
                    }
                    createColumnPositionSegment(addColumnSpecification.firstOrAfterColumn(), columnDefinitionSegment, result);
                }
                AddConstraintSpecificationContext addConstraintSpecification = alterSpecification.addConstraintSpecification();
                ForeignKeyOption_Context foreignKeyOption = null == addConstraintSpecification
                        ? null : addConstraintSpecification.constraintDefinition_().foreignKeyOption_();
                if (null != foreignKeyOption) {
                    result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption.referenceDefinition_().tableName()));
                }
                ChangeColumnSpecificationContext changeColumnSpecification = alterSpecification.changeColumnSpecification();
                if (null != changeColumnSpecification) {
                    createColumnPositionSegment(changeColumnSpecification.firstOrAfterColumn(),
                            createColumnDefinitionSegment(changeColumnSpecification.columnDefinition(), result), result);
                }
                DropColumnSpecificationContext dropColumnSpecification = alterSpecification.dropColumnSpecification();
                if (null != dropColumnSpecification) {
                    result.getDroppedColumnNames().add(((ColumnSegment) visit(dropColumnSpecification)).getName());
                }
                ModifyColumnSpecificationContext modifyColumnSpecification = alterSpecification.modifyColumnSpecification();
                if (null != modifyColumnSpecification) {
                    createColumnPositionSegment(modifyColumnSpecification.firstOrAfterColumn(),
                            createColumnDefinitionSegment(modifyColumnSpecification.columnDefinition(), result), result);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.tableNames());
        result.getTables().addAll(tables.getValues());
        result.getAllSQLSegments().addAll(tables.getValues());
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
    public ASTNode visitIndexDefinition_(final IndexDefinition_Context ctx) {
        return visit(ctx.indexName());
    }
    
    @Override
    public ASTNode visitCreateLikeClause_(final CreateLikeClause_Context ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return visit(ctx.columnName());
    }
    
    private ColumnDefinitionSegment createColumnDefinitionSegment(final ColumnDefinitionContext columnDefinition, final DDLStatement statement) {
        ColumnSegment column = (ColumnSegment) visit(columnDefinition.columnName());
        LiteralValue dataType = (LiteralValue) visit(columnDefinition.dataType().dataTypeName_());
        boolean isPrimaryKey = false;
        for (InlineDataType_Context inlineDataType : columnDefinition.inlineDataType_()) {
            CommonDataTypeOption_Context commonDataTypeOption = inlineDataType.commonDataTypeOption_();
            if (null != commonDataTypeOption) {
                if (null != commonDataTypeOption.primaryKey()) {
                    isPrimaryKey = true;
                }
                if (null != commonDataTypeOption.referenceDefinition_()) {
                    statement.getAllSQLSegments().add((TableSegment) visit(commonDataTypeOption.referenceDefinition_().tableName()));
                }
            }
        }
        for (GeneratedDataType_Context generatedDataType: columnDefinition.generatedDataType_()) {
            CommonDataTypeOption_Context commonDataTypeOption = generatedDataType.commonDataTypeOption_();
            if (null != commonDataTypeOption) {
                if (null != commonDataTypeOption.primaryKey()) {
                    isPrimaryKey = true;
                }
                if (null != commonDataTypeOption.referenceDefinition_()) {
                    statement.getAllSQLSegments().add((TableSegment) visit(commonDataTypeOption.referenceDefinition_().tableName()));
                }
            }
        }
        return new ColumnDefinitionSegment(column.getStartIndex(), column.getStopIndex(),
                column.getName(), dataType.getLiteral(), isPrimaryKey);
    }
    
    private void createColumnPositionSegment(final FirstOrAfterColumnContext firstOrAfterColumn, final ColumnDefinitionSegment columnDefinition,
                                             final AlterTableStatement statement) {
        if (null != firstOrAfterColumn) {
            ColumnPositionSegment columnPositionSegment = null;
            if (null != firstOrAfterColumn.FIRST()) {
                columnPositionSegment = new ColumnFirstPositionSegment(columnDefinition.getStartIndex(), columnDefinition.getStopIndex(),
                        columnDefinition.getColumnName());
            } else if (null != firstOrAfterColumn.AFTER()) {
                ColumnSegment afterColumn = (ColumnSegment) visit(firstOrAfterColumn.columnName());
                columnPositionSegment = new ColumnAfterPositionSegment(columnDefinition.getStartIndex(), columnDefinition.getStopIndex(),
                        columnDefinition.getColumnName(), afterColumn.getName());
            }
            statement.getChangedPositionColumns().add(columnPositionSegment);
            statement.getAllSQLSegments().add(columnPositionSegment);
        }
    }
}
