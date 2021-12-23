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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTableActionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DeallocateContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexElemContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexParamsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ModifyConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RenameTableSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableConstraintUsingIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableNameClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableNamesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ValidateConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.ValidateConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDeallocateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussTruncateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

/**
 * DDL Statement SQL visitor for openGauss.
 */
@NoArgsConstructor
public final class OpenGaussDDLStatementSQLVisitor extends OpenGaussStatementSQLVisitor implements DDLSQLVisitor, SQLStatementVisitor {
    
    public OpenGaussDDLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        OpenGaussCreateTableStatement result = new OpenGaussCreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setContainsNotExistClause(null != ctx.notExistClause());
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
            if (null != each.tableConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.tableConstraint()));
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        OpenGaussAlterTableStatement result = new OpenGaussAlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableNameClause().tableName()));
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
                } else if (each instanceof ValidateConstraintDefinitionSegment) {
                    result.getValidateConstraintDefinitions().add((ValidateConstraintDefinitionSegment) each);
                } else if (each instanceof ModifyConstraintDefinitionSegment) {
                    result.getModifyConstraintDefinitions().add((ModifyConstraintDefinitionSegment) each);
                } else if (each instanceof DropConstraintDefinitionSegment) {
                    result.getDropConstraintDefinitions().add((DropConstraintDefinitionSegment) each);
                } else if (each instanceof RenameTableDefinitionSegment) {
                    result.setRenameTable(((RenameTableDefinitionSegment) each).getRenameTable());
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.alterTableActions()) {
            for (AlterTableActionContext each : ctx.alterTableActions().alterTableAction()) {
                AddColumnSpecificationContext addColumnSpecification = each.addColumnSpecification();
                if (null != addColumnSpecification) {
                    result.getValue().addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification)).getValue());
                }
                if (null != each.addConstraintSpecification() && null != each.addConstraintSpecification().tableConstraint()) {
                    result.getValue().add((AddConstraintDefinitionSegment) visit(each.addConstraintSpecification()));
                }
                if (null != each.validateConstraintSpecification()) {
                    result.getValue().add((ValidateConstraintDefinitionSegment) visit(each.validateConstraintSpecification()));
                }
                if (null != each.modifyColumnSpecification()) {
                    result.getValue().add((ModifyColumnDefinitionSegment) visit(each.modifyColumnSpecification()));
                }
                if (null != each.modifyConstraintSpecification()) {
                    result.getValue().add((ModifyConstraintDefinitionSegment) visit(each.modifyConstraintSpecification()));
                }
                if (null != each.dropColumnSpecification()) {
                    result.getValue().add((DropColumnDefinitionSegment) visit(each.dropColumnSpecification()));
                }
                if (null != each.dropConstraintSpecification()) {
                    result.getValue().add((DropConstraintDefinitionSegment) visit(each.dropConstraintSpecification()));
                }
            }
        }
        if (null != ctx.renameTableSpecification()) {
            result.getValue().add((RenameTableDefinitionSegment) visit(ctx.renameTableSpecification()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddConstraintSpecification(final AddConstraintSpecificationContext ctx) {
        return new AddConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintDefinitionSegment) visit(ctx.tableConstraint()));
    }
    
    @Override
    public ASTNode visitValidateConstraintSpecification(final ValidateConstraintSpecificationContext ctx) {
        return new ValidateConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitModifyConstraintSpecification(final ModifyConstraintSpecificationContext ctx) {
        return new ModifyConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitDropConstraintSpecification(final DropConstraintSpecificationContext ctx) {
        return new DropConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitRenameTableSpecification(final RenameTableSpecificationContext ctx) {
        RenameTableDefinitionSegment result = new RenameTableDefinitionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        TableNameSegment tableName = new TableNameSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        result.setRenameTable(new SimpleTableSegment(tableName));
        return result;
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
        ColumnDefinitionContext columnDefinition = ctx.columnDefinition();
        if (null != columnDefinition) {
            AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                    ctx.columnDefinition().getStart().getStartIndex(), columnDefinition.getStop().getStopIndex(), Collections.singletonList((ColumnDefinitionSegment) visit(columnDefinition)));
            result.getValue().add(addColumnDefinition);
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataType = (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = isPrimaryKey(ctx);
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey);
        for (ColumnConstraintContext each : ctx.columnConstraint()) {
            if (null != each.columnConstraintOption().tableName()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.columnConstraintOption().tableName()));
            }
        }
        return result;
    }
    
    private boolean isPrimaryKey(final ColumnDefinitionContext ctx) {
        for (ColumnConstraintContext each : ctx.columnConstraint()) {
            if (null != each.columnConstraintOption() && null != each.columnConstraintOption().primaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ASTNode visitTableConstraintUsingIndex(final TableConstraintUsingIndexContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintName()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintName()));
        }
        if (null != ctx.indexName()) {
            result.setIndexName((IndexSegment) visit(ctx.indexName()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTableConstraint(final TableConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintClause()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintClause().constraintName()));
        }
        if (null != ctx.tableConstraintOption().primaryKey()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.tableConstraintOption().columnNames(0))).getValue());
        }
        if (null != ctx.tableConstraintOption().FOREIGN()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.tableConstraintOption().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO visit pk and table ref
        ColumnSegment column = (ColumnSegment) visit(ctx.modifyColumn().columnName());
        DataTypeSegment dataType = null == ctx.dataType() ? null : (DataTypeSegment) visit(ctx.dataType());
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false);
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList((ColumnSegment) visit(ctx.columnName())));
    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnSegment) visit(ctx.columnName(0)), (ColumnSegment) visit(ctx.columnName(1)));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        OpenGaussDropTableStatement result = new OpenGaussDropTableStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        result.setContainsExistClause(null != ctx.existClause());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        OpenGaussTruncateStatement result = new OpenGaussTruncateStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNamesClause())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        OpenGaussCreateIndexStatement result = new OpenGaussCreateIndexStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setColumns(((CollectionValue<ColumnSegment>) visit(ctx.indexParams())).getValue());
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        } else {
            result.setGeneratedIndexStartIndex(ctx.ON().getSymbol().getStartIndex() - 1);
        }
        return result;
    }
    
    @Override
    public ASTNode visitIndexParams(final IndexParamsContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (IndexElemContext each : ctx.indexElem()) {
            if (null != each.colId()) {
                result.getValue().add(new ColumnSegment(each.colId().start.getStartIndex(), each.colId().stop.getStopIndex(), new IdentifierValue(each.colId().getText())));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        OpenGaussAlterIndexStatement result = new OpenGaussAlterIndexStatement();
        result.setIndex((IndexSegment) visit(ctx.indexName()));
        if (null != ctx.alterIndexDefinitionClause().renameIndexSpecification()) {
            result.setRenameIndex((IndexSegment) visit(ctx.alterIndexDefinitionClause().renameIndexSpecification().indexName()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        OpenGaussDropIndexStatement result = new OpenGaussDropIndexStatement();
        result.getIndexes().addAll(((CollectionValue<IndexSegment>) visit(ctx.indexNames())).getValue());
        result.setContainsExistClause(null != ctx.existClause());
        return result;
    }
    
    @Override
    public ASTNode visitIndexNames(final IndexNamesContext ctx) {
        CollectionValue<IndexSegment> result = new CollectionValue<>();
        for (IndexNameContext each : ctx.indexName()) {
            result.getValue().add((IndexSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableNameClause(final TableNameClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitTableNamesClause(final TableNamesClauseContext ctx) {
        Collection<SimpleTableSegment> tableSegments = new LinkedList<>();
        for (int i = 0; i < ctx.tableNameClause().size(); i++) {
            tableSegments.add((SimpleTableSegment) visit(ctx.tableNameClause(i)));
        }
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        result.getValue().addAll(tableSegments);
        return result;
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new OpenGaussAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new OpenGaussAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new OpenGaussCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new OpenGaussCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new OpenGaussDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        return new OpenGaussDropViewStatement();
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        return new OpenGaussCreateViewStatement();
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        return new OpenGaussAlterViewStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        OpenGaussDropDatabaseStatement result = new OpenGaussDropDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new OpenGaussDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        OpenGaussCreateDatabaseStatement result = new OpenGaussCreateDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        return result;
    }

    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        OpenGaussCreateSequenceStatement result = new OpenGaussCreateSequenceStatement();
        result.setSequenceName(((IdentifierValue) visit(ctx.qualifiedName())).getValue());
        return result;
    }

    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        OpenGaussAlterSequenceStatement result = new OpenGaussAlterSequenceStatement();
        result.setSequenceName(((IdentifierValue) visit(ctx.qualifiedName())).getValue());
        return result;
    }

    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        OpenGaussDropSequenceStatement result = new OpenGaussDropSequenceStatement();
        result.setSequenceName(((IdentifierValue) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        OpenGaussPrepareStatement result = new OpenGaussPrepareStatement();
        if (null != ctx.preparableStmt().select()) {
            result.setSelect((SelectStatement) visit(ctx.preparableStmt().select()));    
        }
        if (null != ctx.preparableStmt().insert()) {
            result.setInsert((InsertStatement) visit(ctx.preparableStmt().insert()));
        }
        if (null != ctx.preparableStmt().update()) {
            result.setUpdate((UpdateStatement) visit(ctx.preparableStmt().update()));
        }
        if (null != ctx.preparableStmt().delete()) {
            result.setDelete((DeleteStatement) visit(ctx.preparableStmt().delete()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDeallocate(final DeallocateContext ctx) {
        return new OpenGaussDeallocateStatement();
    }
        
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new OpenGaussCreateTablespaceStatement();
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        return new OpenGaussAlterTablespaceStatement();
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new OpenGaussDropTablespaceStatement();
    }
    
    @Override
    public ASTNode visitDropDomain(final DropDomainContext ctx) {
        return new OpenGaussDropDomainStatement();
    }
    
    @Override
    public ASTNode visitCreateDomain(final CreateDomainContext ctx) {
        return new OpenGaussCreateDomainStatement();
    }
    
    @Override
    public ASTNode visitCreateRule(final CreateRuleContext ctx) {
        return new OpenGaussCreateRuleStatement();
    }
    
    @Override
    public ASTNode visitCreateLanguage(final CreateLanguageContext ctx) {
        return new OpenGaussCreateLanguageStatement();
    }
}
