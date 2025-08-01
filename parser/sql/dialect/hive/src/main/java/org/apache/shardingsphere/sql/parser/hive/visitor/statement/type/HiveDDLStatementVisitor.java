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

package org.apache.shardingsphere.sql.parser.hive.visitor.statement.type;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.AlterDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.TableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DataTypeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.TableNameWithDbContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.MsckStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ChangeColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.CreateMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.DropMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.AlterMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.hive.visitor.statement.HiveStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.AddColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ReplaceColumnsContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ReplaceColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.HiveStatementParser.ViewNameWithDbContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import java.util.Collections;

/**
 * DDL statement visitor for Hive.
 */
public final class HiveDDLStatementVisitor extends HiveStatementVisitor implements DDLStatementVisitor {
    
    public HiveDDLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new CreateDatabaseStatement(getDatabaseType(), new IdentifierValue(ctx.identifier().getText()).getValue(), null != ctx.ifNotExists());
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(getDatabaseType(), new IdentifierValue(ctx.identifier().getText()).getValue(), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new AlterDatabaseStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.alterTableCommonClause().tableName()));
        if (null != ctx.changeColumn()) {
            ChangeColumnDefinitionSegment changeColumnSegment = (ChangeColumnDefinitionSegment) visit(ctx.changeColumn());
            result.getChangeColumnDefinitions().add(changeColumnSegment);
        }
        if (null != ctx.addColumns()) {
            AddColumnDefinitionSegment addSeg = (AddColumnDefinitionSegment) visit(ctx.addColumns());
            result.getAddColumnDefinitions().add(addSeg);
        }
        if (null != ctx.replaceColumns()) {
            ReplaceColumnDefinitionSegment repSeg = (ReplaceColumnDefinitionSegment) visit(ctx.replaceColumns());
            result.getReplaceColumnDefinitions().add(repSeg);
        }
        if (null != ctx.COMPACT()) {
            String compactionType = ctx.string_().getText().replace("'", "");
            if (!isValidCompactionType(compactionType)) {
                throw new IllegalArgumentException("Invalid compaction type. Must be 'MAJOR', 'MINOR' or 'REBALANCE'");
            }
            if ((null != ctx.clusteredIntoClause() || null != ctx.orderByClause())
                    && !"REBALANCE".equalsIgnoreCase(compactionType)) {
                throw new IllegalArgumentException("[CLUSTERED INTO n BUCKETS] and [ORDER BY col_list] clauses can only be used with REBALANCE compaction");
            }
        }
        return result;
    }
    
    private boolean isValidCompactionType(final String compactionType) {
        return "MAJOR".equalsIgnoreCase(compactionType)
                || "MINOR".equalsIgnoreCase(compactionType)
                || "REBALANCE".equalsIgnoreCase(compactionType);
    }
    
    @Override
    public ASTNode visitMsckStatement(final MsckStatementContext ctx) {
        AlterTableStatement result = new AlterTableStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.getTables().add((SimpleTableSegment) visit(ctx.tableNameWithDb()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        return new TruncateStatement(getDatabaseType(), Collections.singleton((SimpleTableSegment) visit(ctx.tableNameWithDb())));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.createTableCommonClause().tableNameWithDb()));
        result.setIfNotExists(null != ctx.createTableCommonClause().ifNotExists());
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
        for (ColumnDefinitionContext each : ctx.columnDefinition()) {
            result.getValue().add((ColumnDefinitionSegment) visit(each));
        }
        for (TableConstraintContext each : ctx.tableConstraint()) {
            result.getValue().add((ConstraintDefinitionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.columnName().getStart().getStartIndex(), ctx.columnName().getStop().getStopIndex(),
                new IdentifierValue(ctx.columnName().getText()));
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataTypeClause());
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false, getText(ctx));
    }
    
    @Override
    public ASTNode visitChangeColumn(final ChangeColumnContext ctx) {
        ColumnSegment oldColumn = new ColumnSegment(ctx.columnName(0).getStart().getStartIndex(), ctx.columnName(0).getStop().getStopIndex(),
                new IdentifierValue(ctx.columnName(0).getText()));
        ColumnSegment newColumn = new ColumnSegment(ctx.columnName(1).getStart().getStartIndex(), ctx.columnName(1).getStop().getStopIndex(),
                new IdentifierValue(ctx.columnName(1).getText()));
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataTypeClause());
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.columnName(1).getStart().getStartIndex(),
                ctx.dataTypeClause().getStop().getStopIndex(), newColumn, dataType, false, false, getText(ctx));
        ChangeColumnDefinitionSegment result = new ChangeColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
        result.setPreviousColumn(oldColumn);
        if (null != ctx.FIRST()) {
            ColumnFirstPositionSegment firstPos = new ColumnFirstPositionSegment(ctx.FIRST().getSymbol().getStartIndex(), ctx.FIRST().getSymbol().getStopIndex(), null);
            result.setColumnPosition(firstPos);
        } else if (null != ctx.AFTER()) {
            ColumnNameContext afterCtx = ctx.columnName(ctx.columnName().size() - 1);
            ColumnSegment afterColumn = new ColumnSegment(afterCtx.getStart().getStartIndex(), afterCtx.getStop().getStopIndex(),
                    new IdentifierValue(afterCtx.getText()));
            ColumnAfterPositionSegment afterPos = new ColumnAfterPositionSegment(afterCtx.getStart().getStartIndex(), afterCtx.getStop().getStopIndex(), afterColumn);
            result.setColumnPosition(afterPos);
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumns(final AddColumnsContext ctx) {
        java.util.Collection<ColumnDefinitionSegment> cols = new java.util.LinkedList<>();
        for (ColumnDefinitionContext each : ctx.columnDefinition()) {
            cols.add((ColumnDefinitionSegment) visit(each));
        }
        return new AddColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), cols);
    }
    
    @Override
    public ASTNode visitReplaceColumns(final ReplaceColumnsContext ctx) {
        java.util.Collection<ColumnDefinitionSegment> cols = new java.util.LinkedList<>();
        for (ColumnDefinitionContext each : ctx.columnDefinition()) {
            cols.add((ColumnDefinitionSegment) visit(each));
        }
        return new ReplaceColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), cols);
    }
    
    private String getText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
    
    @Override
    public ASTNode visitDataTypeClause(final DataTypeClauseContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setStartIndex(ctx.getStart().getStartIndex());
        result.setStopIndex(ctx.getStop().getStopIndex());
        if (null != ctx.primitiveType()) {
            result.setDataTypeName(ctx.primitiveType().getText());
        } else if (null != ctx.arrayType()) {
            result.setDataTypeName(ctx.arrayType().getText());
        } else if (null != ctx.mapType()) {
            result.setDataTypeName(ctx.mapType().getText());
        } else if (null != ctx.structType()) {
            result.setDataTypeName(ctx.structType().getText());
        } else if (null != ctx.unionType()) {
            result.setDataTypeName(ctx.unionType().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableNameWithDb(final TableNameWithDbContext ctx) {
        if (1 == ctx.identifier().size()) {
            return new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    new IdentifierValue(ctx.identifier(0).getText())));
        } else {
            SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.identifier(1).getStart().getStartIndex(),
                    ctx.identifier(1).getStop().getStopIndex(), new IdentifierValue(ctx.identifier(1).getText())));
            result.setOwner(new org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment(
                    ctx.identifier(0).getStart().getStartIndex(), ctx.identifier(0).getStop().getStopIndex(),
                    new IdentifierValue(ctx.identifier(0).getText())));
            return result;
        }
    }
    
    @Override
    public ASTNode visitTableConstraint(final TableConstraintContext ctx) {
        return new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        CreateViewStatement result = new CreateViewStatement(getDatabaseType());
        result.setView((SimpleTableSegment) visit(ctx.viewNameWithDb()));
        if (null != ctx.COMMENT() && !ctx.string_().isEmpty()) {
            result.setViewDefinition(ctx.string_(ctx.string_().size() - 1).getText().replace("'", ""));
        }
        if (null != ctx.tblProperties()) {
            result.setViewDefinition(getText(ctx.tblProperties()));
        }
        HiveDMLStatementVisitor dmlVisitor = new HiveDMLStatementVisitor(getDatabaseType());
        ASTNode selectNode = dmlVisitor.visit(ctx.select());
        if (selectNode instanceof SelectStatement) {
            result.setSelect((SelectStatement) selectNode);
        }
        result.setViewDefinition(getText(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitViewNameWithDb(final ViewNameWithDbContext ctx) {
        if (1 == ctx.identifier().size()) {
            return new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    new IdentifierValue(ctx.identifier(0).getText())));
        } else {
            SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.identifier(1).getStart().getStartIndex(),
                    ctx.identifier(1).getStop().getStopIndex(), new IdentifierValue(ctx.identifier(1).getText())));
            result.setOwner(new org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment(
                    ctx.identifier(0).getStart().getStartIndex(), ctx.identifier(0).getStop().getStopIndex(),
                    new IdentifierValue(ctx.identifier(0).getText())));
            return result;
        }
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        DropViewStatement result = new DropViewStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.getViews().add((SimpleTableSegment) visit(ctx.viewNameWithDb()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        AlterViewStatement result = new AlterViewStatement(getDatabaseType());
        result.setView((SimpleTableSegment) visit(ctx.alterViewCommonClause().viewNameWithDb()));
        if (null != ctx.select()) {
            HiveDMLStatementVisitor dmlVisitor = new HiveDMLStatementVisitor(getDatabaseType());
            ASTNode selectNode = dmlVisitor.visit(ctx.select());
            if (selectNode instanceof SelectStatement) {
                result.setSelect((SelectStatement) selectNode);
            }
            result.setViewDefinition(getText(ctx.select()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateMaterializedView(final CreateMaterializedViewContext ctx) {
        return new CreateMaterializedViewStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropMaterializedView(final DropMaterializedViewContext ctx) {
        return new DropMaterializedViewStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new AlterMaterializedViewStatement(getDatabaseType());
    }
}
