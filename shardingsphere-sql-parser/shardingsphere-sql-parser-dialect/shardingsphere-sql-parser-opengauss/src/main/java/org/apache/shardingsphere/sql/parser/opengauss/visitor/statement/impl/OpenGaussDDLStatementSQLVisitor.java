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
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AbsoluteCountContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AllContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterAggregateContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterConversionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterDefaultPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterDirectoryContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterExtensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterForeignTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterRenameViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterSynonymContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTableActionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTextSearchDictionaryContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTextSearchParserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTextSearchTemplateContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.BackwardAllContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.BackwardContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.BackwardCountContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CloseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CommentContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CountContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateAggregateContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateCastContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateConversionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDirectoryContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateExtensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreatePublicationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateSynonymContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateTextSearchContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CursorContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CursorNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DeallocateContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DeclareContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropCastContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropConversionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropDirectoryContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropExtensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropPublicationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropServerContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropSynonymContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FetchContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FirstContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ForwardAllContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ForwardContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ForwardCountContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexElemContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexParamsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.LastContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ModifyConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.MoveContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NextContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PriorContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RelativeCountContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RenameTableSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableConstraintUsingIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableNameClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableNamesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ValidateConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.DirectionType;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.NameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterAggregateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterConversionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterDirectoryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterExtensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterTextSearchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCloseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCommentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateCastStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateConversionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateDirectoryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateExtensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTextSearchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDeallocateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDeclareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropCastStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropConversionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropDirectoryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropExtensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropPublicationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussFetchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussMoveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussTruncateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
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
        OpenGaussCreateTableStatement result = new OpenGaussCreateTableStatement(null != ctx.ifNotExists());
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
    
    @Override
    public ASTNode visitAlterAggregate(final AlterAggregateContext ctx) {
        return new OpenGaussAlterAggregateStatement();
    }
    
    @Override
    public ASTNode visitAlterDefaultPrivileges(final AlterDefaultPrivilegesContext ctx) {
        return new OpenGaussAlterDefaultPrivilegesStatement();
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
    public ASTNode visitAlterForeignTable(final AlterForeignTableContext ctx) {
        return new OpenGaussAlterForeignTableStatement();
    }
    
    @Override
    public ASTNode visitAlterGroup(final AlterGroupContext ctx) {
        return new OpenGaussAlterGroupStatement();
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new OpenGaussAlterMaterializedViewStatement();
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
    public ASTNode visitAlterDomain(final AlterDomainContext ctx) {
        return new OpenGaussAlterDomainStatement();
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
        boolean isPrimaryKey = ctx.columnConstraint().stream().anyMatch(each -> null != each.columnConstraintOption() && null != each.columnConstraintOption().primaryKey());
        // TODO parse not null
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey, false);
        for (ColumnConstraintContext each : ctx.columnConstraint()) {
            if (null != each.columnConstraintOption().tableName()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.columnConstraintOption().tableName()));
            }
        }
        return result;
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
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false);
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
        boolean containsCascade = null != ctx.dropTableOpt() && null != ctx.dropTableOpt().CASCADE();
        OpenGaussDropTableStatement result = new OpenGaussDropTableStatement(null != ctx.ifExists(), containsCascade);
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
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
        OpenGaussCreateIndexStatement result = new OpenGaussCreateIndexStatement(null != ctx.ifNotExists());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.indexParams())).getValue());
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
        result.setIndex(createIndexSegment((SimpleTableSegment) visit(ctx.qualifiedName())));
        if (null != ctx.alterIndexDefinitionClause().renameIndexSpecification()) {
            result.setRenameIndex((IndexSegment) visit(ctx.alterIndexDefinitionClause().renameIndexSpecification().indexName()));
        }
        return result;
    }
    
    private IndexSegment createIndexSegment(final SimpleTableSegment tableSegment) {
        IndexNameSegment indexName = new IndexNameSegment(tableSegment.getTableName().getStartIndex(), tableSegment.getTableName().getStopIndex(), tableSegment.getTableName().getIdentifier());
        IndexSegment result = new IndexSegment(tableSegment.getStartIndex(), tableSegment.getStopIndex(), indexName);
        tableSegment.getOwner().ifPresent(result::setOwner);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        OpenGaussDropIndexStatement result = new OpenGaussDropIndexStatement(null != ctx.ifExists());
        result.getIndexes().addAll(createIndexSegments(((CollectionValue<SimpleTableSegment>) visit(ctx.qualifiedNameList())).getValue()));
        return result;
    }
    
    private Collection<IndexSegment> createIndexSegments(final Collection<SimpleTableSegment> tableSegments) {
        Collection<IndexSegment> result = new LinkedList<>();
        for (SimpleTableSegment each : tableSegments) {
            result.add(createIndexSegment(each));
        }
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
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        OpenGaussDropViewStatement result = new OpenGaussDropViewStatement();
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        OpenGaussCreateViewStatement result = new OpenGaussCreateViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.qualifiedName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        OpenGaussAlterViewStatement result = new OpenGaussAlterViewStatement();
        result.setViewSQL(getOriginalText(ctx));
        result.setView((SimpleTableSegment) visit(ctx.qualifiedName()));
        if (ctx.alterViewClauses() instanceof AlterRenameViewContext) {
            NameContext nameContext = ((AlterRenameViewContext) ctx.alterViewClauses()).name();
            result.setRenameView(new SimpleTableSegment(new TableNameSegment(nameContext.getStart().getStartIndex(),
                    nameContext.getStop().getStopIndex(), (IdentifierValue) visit(nameContext.identifier()))));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        OpenGaussDropDatabaseStatement result = new OpenGaussDropDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        result.setIfExists(null != ctx.ifExists());
        return result;
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new OpenGaussDropServerStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new OpenGaussDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropPublication(final DropPublicationContext ctx) {
        return new OpenGaussDropPublicationStatement();
    }
    
    @Override
    public ASTNode visitDropCast(final DropCastContext ctx) {
        return new OpenGaussDropCastStatement();
    }
    
    @Override
    public ASTNode visitDropRule(final DropRuleContext ctx) {
        return new OpenGaussDropRuleStatement();
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
        result.setSequenceName(((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        OpenGaussAlterSequenceStatement result = new OpenGaussAlterSequenceStatement();
        result.setSequenceName(((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        OpenGaussDropSequenceStatement result = new OpenGaussDropSequenceStatement();
        result.setSequenceNames(((CollectionValue) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropSynonym(final DropSynonymContext ctx) {
        return new OpenGaussDropSynonymStatement();
    }
    
    @Override
    public ASTNode visitDropType(final DropTypeContext ctx) {
        return new OpenGaussDropTypeStatement();
    }
    
    @Override
    public ASTNode visitDropDirectory(final DropDirectoryContext ctx) {
        return new OpenGaussDropDirectoryStatement();
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
    public ASTNode visitCreateSynonym(final CreateSynonymContext ctx) {
        return new OpenGaussCreateSynonymStatement();
    }
    
    @Override
    public ASTNode visitCreateAggregate(final CreateAggregateContext ctx) {
        return new OpenGaussCreateAggregateStatement();
    }
    
    @Override
    public ASTNode visitCreatePublication(final CreatePublicationContext ctx) {
        return new OpenGaussCreatePublicationStatement();
    }
    
    @Override
    public ASTNode visitCreateDirectory(final CreateDirectoryContext ctx) {
        return new OpenGaussCreateDirectoryStatement();
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
    
    @Override
    public ASTNode visitCreateSchema(final CreateSchemaContext ctx) {
        OpenGaussCreateSchemaStatement result = new OpenGaussCreateSchemaStatement();
        if (null != ctx.createSchemaClauses().colId()) {
            result.setSchemaName(new IdentifierValue(ctx.createSchemaClauses().colId().getText()));
        }
        if (null != ctx.createSchemaClauses().roleSpec() && null != ctx.createSchemaClauses().roleSpec().identifier()) {
            result.setUsername((IdentifierValue) visit(ctx.createSchemaClauses().roleSpec().identifier()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterSchema(final AlterSchemaContext ctx) {
        OpenGaussAlterSchemaStatement result = new OpenGaussAlterSchemaStatement();
        result.setSchemaName((IdentifierValue) visit(ctx.name().get(0)));
        if (ctx.name().size() > 1) {
            result.setRenameSchema((IdentifierValue) visit(ctx.name().get(1)));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        OpenGaussDropSchemaStatement result = new OpenGaussDropSchemaStatement();
        result.getSchemaNames().addAll(((CollectionValue<IdentifierValue>) visit(ctx.nameList())).getValue());
        result.setContainsCascade(null != ctx.dropBehavior() && null != ctx.dropBehavior().CASCADE());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitNameList(final NameListContext ctx) {
        CollectionValue<IdentifierValue> result = new CollectionValue<>();
        if (null != ctx.nameList()) {
            result.combine((CollectionValue<IdentifierValue>) visit(ctx.nameList()));
        }
        if (null != ctx.name()) {
            result.getValue().add((IdentifierValue) visit(ctx.name()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterLanguage(final AlterLanguageContext ctx) {
        return new OpenGaussAlterLanguageStatement();
    }
    
    @Override
    public ASTNode visitAlterSynonym(final AlterSynonymContext ctx) {
        return new OpenGaussAlterSynonymStatement();
    }
    
    @Override
    public ASTNode visitAlterDirectory(final AlterDirectoryContext ctx) {
        return new OpenGaussAlterDirectoryStatement();
    }
    
    @Override
    public ASTNode visitAlterRule(final AlterRuleContext ctx) {
        return new OpenGaussAlterRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new OpenGaussAlterTypeStatement();
    }
    
    @Override
    public ASTNode visitDropLanguage(final DropLanguageContext ctx) {
        return new OpenGaussDropLanguageStatement();
    }
    
    @Override
    public ASTNode visitCreateConversion(final CreateConversionContext ctx) {
        return new OpenGaussCreateConversionStatement();
    }
    
    @Override
    public ASTNode visitCreateCast(final CreateCastContext ctx) {
        return new OpenGaussCreateCastStatement();
    }
    
    @Override
    public ASTNode visitCreateType(final CreateTypeContext ctx) {
        return new OpenGaussCreateTypeStatement();
    }
    
    @Override
    public ASTNode visitDropConversion(final DropConversionContext ctx) {
        return new OpenGaussDropConversionStatement();
    }
    
    @Override
    public ASTNode visitAlterConversion(final AlterConversionContext ctx) {
        return new OpenGaussAlterConversionStatement();
    }
    
    @Override
    public ASTNode visitCreateTextSearch(final CreateTextSearchContext ctx) {
        return new OpenGaussCreateTextSearchStatement();
    }
    
    @Override
    public ASTNode visitAlterTextSearchDictionary(final AlterTextSearchDictionaryContext ctx) {
        return new OpenGaussAlterTextSearchStatement();
    }
    
    @Override
    public ASTNode visitAlterTextSearchTemplate(final AlterTextSearchTemplateContext ctx) {
        return new OpenGaussAlterTextSearchStatement();
    }
    
    @Override
    public ASTNode visitAlterTextSearchParser(final AlterTextSearchParserContext ctx) {
        return new OpenGaussAlterTextSearchStatement();
    }
    
    @Override
    public ASTNode visitCreateExtension(final CreateExtensionContext ctx) {
        return new OpenGaussCreateExtensionStatement();
    }
    
    @Override
    public ASTNode visitAlterExtension(final AlterExtensionContext ctx) {
        return new OpenGaussAlterExtensionStatement();
    }
    
    @Override
    public ASTNode visitDropExtension(final DropExtensionContext ctx) {
        return new OpenGaussDropExtensionStatement();
    }
    
    @Override
    public ASTNode visitDeclare(final DeclareContext ctx) {
        OpenGaussDeclareStatement result = new OpenGaussDeclareStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitComment(final CommentContext ctx) {
        if (null != ctx.commentClauses().objectTypeAnyName() && null != ctx.commentClauses().objectTypeAnyName().TABLE()) {
            return commentOnTable(ctx);
        } else if (null != ctx.commentClauses().COLUMN()) {
            return commentOnColumn(ctx);
        }
        return new OpenGaussCommentStatement();
    }
    
    @SuppressWarnings("unchecked")
    private OpenGaussCommentStatement commentOnColumn(final CommentContext ctx) {
        OpenGaussCommentStatement result = new OpenGaussCommentStatement();
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        Optional<NameSegment> columnName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        columnName.ifPresent(optional -> result.setColumn(new ColumnSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier())));
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private OpenGaussCommentStatement commentOnTable(final CommentContext ctx) {
        OpenGaussCommentStatement result = new OpenGaussCommentStatement();
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    private void setTableSegment(final OpenGaussCommentStatement statement, final Iterator<NameSegment> nameSegmentIterator) {
        Optional<NameSegment> tableName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        tableName.ifPresent(optional -> statement.setTable(new SimpleTableSegment(new TableNameSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier()))));
        Optional<NameSegment> schemaName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        schemaName.ifPresent(optional -> statement.getTable().setOwner(new OwnerSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier())));
        Optional<NameSegment> databaseName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        databaseName.ifPresent(optional -> statement.getTable().getOwner()
                .ifPresent(owner -> owner.setOwner(new OwnerSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier()))));
    }
    
    @Override
    public ASTNode visitCursor(final CursorContext ctx) {
        OpenGaussCursorStatement result = new OpenGaussCursorStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitCursorName(final CursorNameContext ctx) {
        return new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public ASTNode visitClose(final CloseContext ctx) {
        OpenGaussCloseStatement result = new OpenGaussCloseStatement();
        if (null != ctx.cursorName()) {
            result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        }
        result.setCloseAll(null != ctx.ALL());
        return result;
    }
    
    @Override
    public ASTNode visitMove(final MoveContext ctx) {
        OpenGaussMoveStatement result = new OpenGaussMoveStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        if (null != ctx.direction()) {
            result.setDirection((DirectionSegment) visit(ctx.direction()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitFetch(final FetchContext ctx) {
        OpenGaussFetchStatement result = new OpenGaussFetchStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        if (null != ctx.direction()) {
            result.setDirection((DirectionSegment) visit(ctx.direction()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitNext(final NextContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.NEXT);
        return result;
    }
    
    @Override
    public ASTNode visitPrior(final PriorContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.PRIOR);
        return result;
    }
    
    @Override
    public ASTNode visitFirst(final FirstContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.FIRST);
        return result;
    }
    
    @Override
    public ASTNode visitLast(final LastContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.LAST);
        return result;
    }
    
    @Override
    public ASTNode visitAbsoluteCount(final AbsoluteCountContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.ABSOLUTE_COUNT);
        result.setCount(((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
        return result;
    }
    
    @Override
    public ASTNode visitRelativeCount(final RelativeCountContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.RELATIVE_COUNT);
        result.setCount(((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
        return result;
    }
    
    @Override
    public ASTNode visitCount(final CountContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.COUNT);
        result.setCount(((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
        return result;
    }
    
    @Override
    public ASTNode visitAll(final AllContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.ALL);
        return result;
    }
    
    @Override
    public ASTNode visitForward(final ForwardContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.FORWARD);
        return result;
    }
    
    @Override
    public ASTNode visitForwardCount(final ForwardCountContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.FORWARD_COUNT);
        result.setCount(((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
        return result;
    }
    
    @Override
    public ASTNode visitForwardAll(final ForwardAllContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.FORWARD_ALL);
        return result;
    }
    
    @Override
    public ASTNode visitBackward(final BackwardContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.BACKWARD);
        return result;
    }
    
    @Override
    public ASTNode visitBackwardCount(final BackwardCountContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.BACKWARD_COUNT);
        result.setCount(((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
        return result;
    }
    
    @Override
    public ASTNode visitBackwardAll(final BackwardAllContext ctx) {
        DirectionSegment result = new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setDirectionType(DirectionType.BACKWARD_ALL);
        return result;
    }
}
