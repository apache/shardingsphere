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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterPackageContext;
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
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncArgExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FunctionExprWindowlessContext;
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
import org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.DirectionType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ValidateConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.NameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterPackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDirectoryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeallocateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropDirectoryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.opengauss.ddl.OpenGaussAlterDirectoryStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropTypeStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DDL statement visitor for openGauss.
 */
public final class OpenGaussDDLStatementVisitor extends OpenGaussStatementVisitor implements DDLStatementVisitor {
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setIfNotExists(null != ctx.ifNotExists());
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
        AlterTableStatement result = new AlterTableStatement();
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
                } else if (each instanceof RenameColumnSegment) {
                    result.getRenameColumnDefinitions().add((RenameColumnSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterAggregate(final AlterAggregateContext ctx) {
        return new PostgreSQLAlterAggregateStatement();
    }
    
    @Override
    public ASTNode visitAlterDefaultPrivileges(final AlterDefaultPrivilegesContext ctx) {
        return new PostgreSQLAlterDefaultPrivilegesStatement();
    }
    
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.alterTableActions()) {
            result.getValue().addAll(ctx.alterTableActions().alterTableAction().stream().flatMap(each -> getAlterDefinitionSegments(each).stream()).collect(Collectors.toList()));
        }
        if (null != ctx.renameColumnSpecification()) {
            result.getValue().add((RenameColumnSegment) visit(ctx.renameColumnSpecification()));
        }
        if (null != ctx.renameTableSpecification()) {
            result.getValue().add((RenameTableDefinitionSegment) visit(ctx.renameTableSpecification()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<AlterDefinitionSegment> getAlterDefinitionSegments(final AlterTableActionContext ctx) {
        Collection<AlterDefinitionSegment> result = new LinkedList<>();
        if (null != ctx.addColumnSpecification()) {
            result.addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(ctx.addColumnSpecification())).getValue());
        }
        if (null != ctx.addConstraintSpecification() && null != ctx.addConstraintSpecification().tableConstraint()) {
            result.add((AddConstraintDefinitionSegment) visit(ctx.addConstraintSpecification()));
        }
        if (null != ctx.validateConstraintSpecification()) {
            result.add((ValidateConstraintDefinitionSegment) visit(ctx.validateConstraintSpecification()));
        }
        if (null != ctx.modifyColumnSpecification()) {
            result.add((ModifyColumnDefinitionSegment) visit(ctx.modifyColumnSpecification()));
        }
        if (null != ctx.modifyConstraintSpecification()) {
            result.add((ModifyConstraintDefinitionSegment) visit(ctx.modifyConstraintSpecification()));
        }
        if (null != ctx.dropColumnSpecification()) {
            result.add((DropColumnDefinitionSegment) visit(ctx.dropColumnSpecification()));
        }
        if (null != ctx.dropConstraintSpecification()) {
            result.add((DropConstraintDefinitionSegment) visit(ctx.dropConstraintSpecification()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterForeignTable(final AlterForeignTableContext ctx) {
        return new PostgreSQLAlterForeignTableStatement();
    }
    
    @Override
    public ASTNode visitAlterGroup(final AlterGroupContext ctx) {
        return new PostgreSQLAlterGroupStatement();
    }
    
    @Override
    public ASTNode visitAlterPackage(final AlterPackageContext ctx) {
        return new AlterPackageStatement();
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new AlterMaterializedViewStatement();
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
        return new AlterDomainStatement();
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
                    ctx.columnDefinition().getStart().getStartIndex(), columnDefinition.getStop().getStopIndex(), Collections.singleton((ColumnDefinitionSegment) visit(columnDefinition)));
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
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey, false, getText(ctx));
        for (ColumnConstraintContext each : ctx.columnConstraint()) {
            if (null != each.columnConstraintOption().tableName()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.columnConstraintOption().tableName()));
            }
        }
        return result;
    }
    
    private String getText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
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
        ColumnDefinitionSegment columnDefinition = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false, getText(ctx));
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singleton((ColumnSegment) visit(ctx.columnName())));
    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnSegment) visit(ctx.columnName(0)), (ColumnSegment) visit(ctx.columnName(1)));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        boolean containsCascade = null != ctx.dropTableOpt() && null != ctx.dropTableOpt().CASCADE();
        DropTableStatement result = new DropTableStatement();
        result.setIfExists(null != ctx.ifExists());
        result.setContainsCascade(containsCascade);
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNamesClause())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        result.setIfNotExists(null != ctx.ifNotExists());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.indexParams())).getValue());
        if (null == ctx.indexName()) {
            result.setGeneratedIndexStartIndex(ctx.ON().getSymbol().getStartIndex() - 1);
        } else {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
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
            if (null != each.functionExprWindowless()) {
                FunctionSegment functionSegment = (FunctionSegment) visit(each.functionExprWindowless());
                functionSegment.getParameters().forEach(param -> {
                    if (param instanceof ColumnSegment) {
                        result.getValue().add((ColumnSegment) param);
                    }
                });
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitFunctionExprWindowless(final FunctionExprWindowlessContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcApplication().funcName().getText(), getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx.funcApplication().funcArgList().funcArgExpr()));
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressions(final Collection<FuncArgExprContext> aExprContexts) {
        if (null == aExprContexts) {
            return Collections.emptyList();
        }
        Collection<ExpressionSegment> result = new ArrayList<>(aExprContexts.size());
        for (FuncArgExprContext each : aExprContexts) {
            result.add((ExpressionSegment) visit(each.aExpr()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        AlterIndexStatement result = new AlterIndexStatement();
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
        DropIndexStatement result = new DropIndexStatement();
        result.setIfExists(null != ctx.ifExists());
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
        return new AlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new AlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new CreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new CreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new DropFunctionStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        DropViewStatement result = new DropViewStatement();
        result.setIfExists(null != ctx.ifExists());
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        CreateViewStatement result = new CreateViewStatement();
        result.setReplaceView(null != ctx.REPLACE());
        result.setView((SimpleTableSegment) visit(ctx.qualifiedName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        AlterViewStatement result = new AlterViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.qualifiedName()));
        if (ctx.alterViewClauses() instanceof AlterRenameViewContext) {
            NameContext nameContext = ((AlterRenameViewContext) ctx.alterViewClauses()).name();
            result.setRenameView(
                    new SimpleTableSegment(new TableNameSegment(nameContext.getStart().getStartIndex(), nameContext.getStop().getStopIndex(), (IdentifierValue) visit(nameContext.identifier()))));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(((IdentifierValue) visit(ctx.name())).getValue(), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new DropServerStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropPublication(final DropPublicationContext ctx) {
        return new PostgreSQLDropPublicationStatement();
    }
    
    @Override
    public ASTNode visitDropCast(final DropCastContext ctx) {
        return new PostgreSQLDropCastStatement();
    }
    
    @Override
    public ASTNode visitDropRule(final DropRuleContext ctx) {
        return new PostgreSQLDropRuleStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new CreateDatabaseStatement(((IdentifierValue) visit(ctx.name())).getValue(), false);
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        return new CreateSequenceStatement(((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        return new AlterSequenceStatement(((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        return new DropSequenceStatement(((CollectionValue) visit(ctx.qualifiedNameList())).getValue());
    }
    
    @Override
    public ASTNode visitDropSynonym(final DropSynonymContext ctx) {
        return new DropSynonymStatement();
    }
    
    @Override
    public ASTNode visitDropType(final DropTypeContext ctx) {
        return new PostgreSQLDropTypeStatement();
    }
    
    @Override
    public ASTNode visitDropDirectory(final DropDirectoryContext ctx) {
        return new DropDirectoryStatement();
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        PrepareStatement result = new PrepareStatement();
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
        return new DeallocateStatement();
    }
    
    @Override
    public ASTNode visitCreateSynonym(final CreateSynonymContext ctx) {
        return new CreateSynonymStatement();
    }
    
    @Override
    public ASTNode visitCreateAggregate(final CreateAggregateContext ctx) {
        return new PostgreSQLCreateAggregateStatement();
    }
    
    @Override
    public ASTNode visitCreatePublication(final CreatePublicationContext ctx) {
        return new PostgreSQLCreatePublicationStatement();
    }
    
    @Override
    public ASTNode visitCreateDirectory(final CreateDirectoryContext ctx) {
        return new CreateDirectoryStatement();
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new CreateTablespaceStatement();
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        return new AlterTablespaceStatement(null, null);
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new DropTablespaceStatement();
    }
    
    @Override
    public ASTNode visitDropDomain(final DropDomainContext ctx) {
        return new PostgreSQLDropDomainStatement();
    }
    
    @Override
    public ASTNode visitCreateDomain(final CreateDomainContext ctx) {
        return new CreateDomainStatement();
    }
    
    @Override
    public ASTNode visitCreateRule(final CreateRuleContext ctx) {
        return new PostgreSQLCreateRuleStatement();
    }
    
    @Override
    public ASTNode visitCreateLanguage(final CreateLanguageContext ctx) {
        return new PostgreSQLCreateLanguageStatement();
    }
    
    @Override
    public ASTNode visitCreateSchema(final CreateSchemaContext ctx) {
        CreateSchemaStatement result = new CreateSchemaStatement();
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
        AlterSchemaStatement result = new AlterSchemaStatement();
        result.setSchemaName((IdentifierValue) visit(ctx.name().get(0)));
        if (ctx.name().size() > 1) {
            result.setRenameSchema((IdentifierValue) visit(ctx.name().get(1)));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        DropSchemaStatement result = new DropSchemaStatement();
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
        return new PostgreSQLAlterLanguageStatement();
    }
    
    @Override
    public ASTNode visitAlterSynonym(final AlterSynonymContext ctx) {
        return new AlterSynonymStatement();
    }
    
    @Override
    public ASTNode visitAlterDirectory(final AlterDirectoryContext ctx) {
        return new OpenGaussAlterDirectoryStatement();
    }
    
    @Override
    public ASTNode visitAlterRule(final AlterRuleContext ctx) {
        return new PostgreSQLAlterRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new AlterTypeStatement();
    }
    
    @Override
    public ASTNode visitDropLanguage(final DropLanguageContext ctx) {
        return new PostgreSQLDropLanguageStatement();
    }
    
    @Override
    public ASTNode visitCreateConversion(final CreateConversionContext ctx) {
        return new PostgreSQLCreateConversionStatement();
    }
    
    @Override
    public ASTNode visitCreateCast(final CreateCastContext ctx) {
        return new PostgreSQLCreateCastStatement();
    }
    
    @Override
    public ASTNode visitCreateType(final CreateTypeContext ctx) {
        return new CreateTypeStatement();
    }
    
    @Override
    public ASTNode visitDropConversion(final DropConversionContext ctx) {
        return new PostgreSQLDropConversionStatement();
    }
    
    @Override
    public ASTNode visitAlterConversion(final AlterConversionContext ctx) {
        return new PostgreSQLAlterConversionStatement();
    }
    
    @Override
    public ASTNode visitCreateTextSearch(final CreateTextSearchContext ctx) {
        return new PostgreSQLCreateTextSearchStatement();
    }
    
    @Override
    public ASTNode visitAlterTextSearchDictionary(final AlterTextSearchDictionaryContext ctx) {
        return new PostgreSQLAlterTextSearchStatement();
    }
    
    @Override
    public ASTNode visitAlterTextSearchTemplate(final AlterTextSearchTemplateContext ctx) {
        return new PostgreSQLAlterTextSearchStatement();
    }
    
    @Override
    public ASTNode visitAlterTextSearchParser(final AlterTextSearchParserContext ctx) {
        return new PostgreSQLAlterTextSearchStatement();
    }
    
    @Override
    public ASTNode visitCreateExtension(final CreateExtensionContext ctx) {
        return new PostgreSQLCreateExtensionStatement();
    }
    
    @Override
    public ASTNode visitAlterExtension(final AlterExtensionContext ctx) {
        return new PostgreSQLAlterExtensionStatement();
    }
    
    @Override
    public ASTNode visitDropExtension(final DropExtensionContext ctx) {
        return new PostgreSQLDropExtensionStatement();
    }
    
    @Override
    public ASTNode visitDeclare(final DeclareContext ctx) {
        return new DeclareStatement((CursorNameSegment) visit(ctx.cursorName()), (SelectStatement) visit(ctx.select()));
    }
    
    @Override
    public ASTNode visitComment(final CommentContext ctx) {
        if (null != ctx.commentClauses().objectTypeAnyName() && null != ctx.commentClauses().objectTypeAnyName().TABLE()) {
            return commentOnTable(ctx);
        }
        if (null != ctx.commentClauses().COLUMN()) {
            return commentOnColumn(ctx);
        }
        return new CommentStatement();
    }
    
    @SuppressWarnings("unchecked")
    private CommentStatement commentOnColumn(final CommentContext ctx) {
        CommentStatement result = new CommentStatement();
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        Optional<NameSegment> columnName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        columnName.ifPresent(optional -> result.setColumn(new ColumnSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier())));
        result.setComment(new IdentifierValue(ctx.commentClauses().commentText().getText()));
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private CommentStatement commentOnTable(final CommentContext ctx) {
        CommentStatement result = new CommentStatement();
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        result.setComment(new IdentifierValue(ctx.commentClauses().commentText().getText()));
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    private void setTableSegment(final CommentStatement statement, final Iterator<NameSegment> nameSegmentIterator) {
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
        return new CursorStatement((CursorNameSegment) visit(ctx.cursorName()), (SelectStatement) visit(ctx.select()));
    }
    
    @Override
    public ASTNode visitCursorName(final CursorNameContext ctx) {
        return new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public ASTNode visitClose(final CloseContext ctx) {
        return new CloseStatement(null == ctx.cursorName() ? null : (CursorNameSegment) visit(ctx.cursorName()), null != ctx.ALL());
    }
    
    @Override
    public ASTNode visitMove(final MoveContext ctx) {
        return new MoveStatement((CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitFetch(final FetchContext ctx) {
        return new FetchStatement((CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitNext(final NextContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.NEXT);
    }
    
    @Override
    public ASTNode visitPrior(final PriorContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.PRIOR);
    }
    
    @Override
    public ASTNode visitFirst(final FirstContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.FIRST);
    }
    
    @Override
    public ASTNode visitLast(final LastContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.LAST);
    }
    
    @Override
    public ASTNode visitAbsoluteCount(final AbsoluteCountContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.ABSOLUTE_COUNT, ((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
    }
    
    @Override
    public ASTNode visitRelativeCount(final RelativeCountContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.RELATIVE_COUNT, ((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
    }
    
    @Override
    public ASTNode visitCount(final CountContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.COUNT, ((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
    }
    
    @Override
    public ASTNode visitAll(final AllContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.ALL);
    }
    
    @Override
    public ASTNode visitForward(final ForwardContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.FORWARD);
    }
    
    @Override
    public ASTNode visitForwardCount(final ForwardCountContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.FORWARD_COUNT, ((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
    }
    
    @Override
    public ASTNode visitForwardAll(final ForwardAllContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.FORWARD_ALL);
    }
    
    @Override
    public ASTNode visitBackward(final BackwardContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.BACKWARD);
    }
    
    @Override
    public ASTNode visitBackwardCount(final BackwardCountContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.BACKWARD_COUNT, ((NumberLiteralValue) visit(ctx.signedIconst())).getValue().longValue());
    }
    
    @Override
    public ASTNode visitBackwardAll(final BackwardAllContext ctx) {
        return new DirectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), DirectionType.BACKWARD_ALL);
    }
}
