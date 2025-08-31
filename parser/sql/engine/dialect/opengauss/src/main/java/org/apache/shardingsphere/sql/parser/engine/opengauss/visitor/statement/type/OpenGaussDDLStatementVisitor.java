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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
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
import org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeallocateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.directory.AlterDirectoryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.directory.CreateDirectoryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.directory.DropDirectoryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.AlterDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.CreateDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.DropDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.AlterPackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.DropServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.synonym.AlterSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.synonym.CreateSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.synonym.DropSynonymStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.AlterTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.CreateTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.DropTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.aggregate.PostgreSQLAlterAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.aggregate.PostgreSQLCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.cast.PostgreSQLCreateCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.cast.PostgreSQLDropCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLAlterConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLCreateConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLDropConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.extension.PostgreSQLAlterExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.extension.PostgreSQLCreateExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.extension.PostgreSQLDropExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigntable.PostgreSQLAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.group.PostgreSQLAlterGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.language.PostgreSQLAlterLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.language.PostgreSQLCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.language.PostgreSQLDropLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLDropPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.rule.PostgreSQLAlterRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.rule.PostgreSQLCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.rule.PostgreSQLDropRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.textsearch.PostgreSQLAlterTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.textsearch.PostgreSQLCreateTextSearchStatement;

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
    
    public OpenGaussDDLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement(getDatabaseType());
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
        AlterTableStatement result = new AlterTableStatement(getDatabaseType());
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
        return new PostgreSQLAlterAggregateStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterDefaultPrivileges(final AlterDefaultPrivilegesContext ctx) {
        return new PostgreSQLAlterDefaultPrivilegesStatement(getDatabaseType());
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
        return new PostgreSQLAlterForeignTableStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterGroup(final AlterGroupContext ctx) {
        return new PostgreSQLAlterGroupStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterPackage(final AlterPackageContext ctx) {
        return new AlterPackageStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new AlterMaterializedViewStatement(getDatabaseType());
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
        return new AlterDomainStatement(getDatabaseType());
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
        DropTableStatement result = new DropTableStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.setContainsCascade(containsCascade);
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        return new TruncateStatement(getDatabaseType(), ((CollectionValue<SimpleTableSegment>) visit(ctx.tableNamesClause())).getValue());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement(getDatabaseType());
        result.setIfNotExists(null != ctx.ifNotExists());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.indexParams())).getValue());
        if (null == ctx.indexName()) {
            result.setAnonymousIndexStartIndex(ctx.ON().getSymbol().getStartIndex() - 1);
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
        AlterIndexStatement result = new AlterIndexStatement(getDatabaseType());
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
        DropIndexStatement result = new DropIndexStatement(getDatabaseType());
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
        return new AlterFunctionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new AlterProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new CreateFunctionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new CreateProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new DropFunctionStatement(getDatabaseType());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        DropViewStatement result = new DropViewStatement(getDatabaseType());
        result.setIfExists(null != ctx.ifExists());
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        CreateViewStatement result = new CreateViewStatement(getDatabaseType());
        result.setReplaceView(null != ctx.REPLACE());
        result.setView((SimpleTableSegment) visit(ctx.qualifiedName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        AlterViewStatement result = new AlterViewStatement(getDatabaseType());
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
        return new DropDatabaseStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.name())).getValue(), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new DropServerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropPublication(final DropPublicationContext ctx) {
        return new PostgreSQLDropPublicationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropCast(final DropCastContext ctx) {
        return new PostgreSQLDropCastStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropRule(final DropRuleContext ctx) {
        return new PostgreSQLDropRuleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new CreateDatabaseStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.name())).getValue(), false);
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        return new CreateSequenceStatement(getDatabaseType(), ((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        return new AlterSequenceStatement(getDatabaseType(), ((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        return new DropSequenceStatement(getDatabaseType(), ((CollectionValue) visit(ctx.qualifiedNameList())).getValue());
    }
    
    @Override
    public ASTNode visitDropSynonym(final DropSynonymContext ctx) {
        return new DropSynonymStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropType(final DropTypeContext ctx) {
        return new DropTypeStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropDirectory(final DropDirectoryContext ctx) {
        return new DropDirectoryStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        PrepareStatement result = new PrepareStatement(getDatabaseType());
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
        return new DeallocateStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateSynonym(final CreateSynonymContext ctx) {
        return new CreateSynonymStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateAggregate(final CreateAggregateContext ctx) {
        return new PostgreSQLCreateAggregateStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreatePublication(final CreatePublicationContext ctx) {
        return new PostgreSQLCreatePublicationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateDirectory(final CreateDirectoryContext ctx) {
        return new CreateDirectoryStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new CreateTablespaceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        return new AlterTablespaceStatement(getDatabaseType(), null, null);
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new DropTablespaceStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropDomain(final DropDomainContext ctx) {
        return new DropDomainStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateDomain(final CreateDomainContext ctx) {
        return new CreateDomainStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateRule(final CreateRuleContext ctx) {
        return new PostgreSQLCreateRuleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateLanguage(final CreateLanguageContext ctx) {
        return new PostgreSQLCreateLanguageStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateSchema(final CreateSchemaContext ctx) {
        CreateSchemaStatement result = new CreateSchemaStatement(getDatabaseType());
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
        AlterSchemaStatement result = new AlterSchemaStatement(getDatabaseType());
        result.setSchemaName((IdentifierValue) visit(ctx.name().get(0)));
        if (ctx.name().size() > 1) {
            result.setRenameSchema((IdentifierValue) visit(ctx.name().get(1)));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        DropSchemaStatement result = new DropSchemaStatement(getDatabaseType());
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
        return new PostgreSQLAlterLanguageStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterSynonym(final AlterSynonymContext ctx) {
        return new AlterSynonymStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterDirectory(final AlterDirectoryContext ctx) {
        return new AlterDirectoryStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterRule(final AlterRuleContext ctx) {
        return new PostgreSQLAlterRuleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new AlterTypeStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropLanguage(final DropLanguageContext ctx) {
        return new PostgreSQLDropLanguageStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateConversion(final CreateConversionContext ctx) {
        return new PostgreSQLCreateConversionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateCast(final CreateCastContext ctx) {
        return new PostgreSQLCreateCastStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateType(final CreateTypeContext ctx) {
        return new CreateTypeStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropConversion(final DropConversionContext ctx) {
        return new PostgreSQLDropConversionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterConversion(final AlterConversionContext ctx) {
        return new PostgreSQLAlterConversionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateTextSearch(final CreateTextSearchContext ctx) {
        return new PostgreSQLCreateTextSearchStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTextSearchDictionary(final AlterTextSearchDictionaryContext ctx) {
        return new PostgreSQLAlterTextSearchStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTextSearchTemplate(final AlterTextSearchTemplateContext ctx) {
        return new PostgreSQLAlterTextSearchStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTextSearchParser(final AlterTextSearchParserContext ctx) {
        return new PostgreSQLAlterTextSearchStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateExtension(final CreateExtensionContext ctx) {
        return new PostgreSQLCreateExtensionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterExtension(final AlterExtensionContext ctx) {
        return new PostgreSQLAlterExtensionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropExtension(final DropExtensionContext ctx) {
        return new PostgreSQLDropExtensionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDeclare(final DeclareContext ctx) {
        return new PostgreSQLDeclareStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), (SelectStatement) visit(ctx.select()));
    }
    
    @Override
    public ASTNode visitComment(final CommentContext ctx) {
        if (null != ctx.commentClauses().objectTypeAnyName() && null != ctx.commentClauses().objectTypeAnyName().TABLE()) {
            return commentOnTable(ctx);
        }
        if (null != ctx.commentClauses().COLUMN()) {
            return commentOnColumn(ctx);
        }
        return new CommentStatement(getDatabaseType());
    }
    
    @SuppressWarnings("unchecked")
    private CommentStatement commentOnColumn(final CommentContext ctx) {
        CommentStatement result = new CommentStatement(getDatabaseType());
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        Optional<NameSegment> columnName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        columnName.ifPresent(optional -> result.setColumn(new ColumnSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier())));
        result.setComment(new IdentifierValue(ctx.commentClauses().commentText().getText()));
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private CommentStatement commentOnTable(final CommentContext ctx) {
        CommentStatement result = new CommentStatement(getDatabaseType());
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
        return new CursorStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), (SelectStatement) visit(ctx.select()));
    }
    
    @Override
    public ASTNode visitCursorName(final CursorNameContext ctx) {
        return new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public ASTNode visitClose(final CloseContext ctx) {
        return new CloseStatement(getDatabaseType(), null == ctx.cursorName() ? null : (CursorNameSegment) visit(ctx.cursorName()), null != ctx.ALL());
    }
    
    @Override
    public ASTNode visitMove(final MoveContext ctx) {
        return new MoveStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitFetch(final FetchContext ctx) {
        return new FetchStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
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
