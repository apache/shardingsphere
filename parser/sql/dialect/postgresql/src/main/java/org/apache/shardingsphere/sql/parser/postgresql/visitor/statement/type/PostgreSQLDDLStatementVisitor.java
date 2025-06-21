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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AbsoluteCountContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AllContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterAggregateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterCollationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterConversionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterDefaultPrivilegesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterExtensionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterForeignDataWrapperContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterForeignTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterOperatorContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterPolicyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterPublicationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterRenameViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterRoutineContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterServerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterStatisticsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterSubscriptionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTableActionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTextSearchConfigurationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTextSearchDictionaryContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTextSearchParserContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTextSearchTemplateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BackwardAllContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BackwardContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BackwardCountContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CloseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ClusterContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CommentContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CountContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateAccessMethodContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateAggregateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateCastContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateCollationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateConversionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateEventTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateExtensionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateForeignDataWrapperContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateForeignTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateOperatorContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreatePolicyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreatePublicationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateTextSearchContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CursorNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DeallocateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DeclareContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DiscardContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropAccessMethodContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropAggregateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropCastContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropCollationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropConversionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropDomainContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropEventTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropExtensionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropForeignDataWrapperContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropForeignTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropLanguageContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropOperatorClassContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropOperatorContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropOperatorFamilyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropOwnedContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropPolicyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropPublicationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropRoutineContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropRuleContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropServerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropStatisticsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropSubscriptionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTextSearchContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FetchContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FirstContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ForwardAllContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ForwardContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ForwardCountContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FuncArgExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FunctionExprWindowlessContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexElemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexParamsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.LastContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ListenContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ModifyConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.MoveContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NextContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NotifyStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.OpenContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PrepareContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PriorContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RefreshMatViewStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ReindexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelativeCountContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RenameTableSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SecurityLabelStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableConstraintUsingIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNameClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNamesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.UnlistenContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ValidateConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.PostgreSQLStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ClusterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeallocateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ListenStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.OpenStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.RefreshMatViewStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ReindexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.SecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.UnlistenStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterPolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterRoutineStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateAccessMethodStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreatePolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDiscardStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropAccessMethodStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropOperatorClassStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropOperatorFamilyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropOwnedStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropPolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropRoutineStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLNotifyStmtStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DDL statement visitor for PostgreSQL.
 */
public final class PostgreSQLDDLStatementVisitor extends PostgreSQLStatementVisitor implements DDLStatementVisitor {
    
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
    public ASTNode visitAlterCollation(final AlterCollationContext ctx) {
        return new PostgreSQLAlterCollationStatement();
    }
    
    @Override
    public ASTNode visitAlterDefaultPrivileges(final AlterDefaultPrivilegesContext ctx) {
        return new PostgreSQLAlterDefaultPrivilegesStatement();
    }
    
    @Override
    public ASTNode visitAlterForeignDataWrapper(final AlterForeignDataWrapperContext ctx) {
        return new PostgreSQLAlterForeignDataWrapperStatement();
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
    public ASTNode visitDropForeignTable(final DropForeignTableContext ctx) {
        return new PostgreSQLDropForeignTableStatement();
    }
    
    @Override
    public ASTNode visitAlterGroup(final AlterGroupContext ctx) {
        return new PostgreSQLAlterGroupStatement();
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new AlterMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitAlterOperator(final AlterOperatorContext ctx) {
        return new AlterOperatorStatement();
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
    public ASTNode visitAlterPolicy(final AlterPolicyContext ctx) {
        return new PostgreSQLAlterPolicyStatement();
    }
    
    @Override
    public ASTNode visitAlterPublication(final AlterPublicationContext ctx) {
        return new PostgreSQLAlterPublicationStatement();
    }
    
    @Override
    public ASTNode visitAlterSubscription(final AlterSubscriptionContext ctx) {
        return new PostgreSQLAlterSubscriptionStatement();
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new AlterTriggerStatement();
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new AlterTypeStatement();
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
    
    @Override
    public ASTNode visitDropPolicy(final DropPolicyContext ctx) {
        return new PostgreSQLDropPolicyStatement();
    }
    
    @Override
    public ASTNode visitDropRule(final DropRuleContext ctx) {
        return new PostgreSQLDropRuleStatement();
    }
    
    @Override
    public ASTNode visitDropStatistics(final DropStatisticsContext ctx) {
        return new PostgreSQLDropStatisticsStatement();
    }
    
    @Override
    public ASTNode visitDropPublication(final DropPublicationContext ctx) {
        return new PostgreSQLDropPublicationStatement();
    }
    
    @Override
    public ASTNode visitDropSubscription(final DropSubscriptionContext ctx) {
        return new PostgreSQLDropSubscriptionStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        result.setIfNotExists(null != ctx.ifNotExists());
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
    
    @Override
    public ASTNode visitDropGroup(final DropGroupContext ctx) {
        return new PostgreSQLDropGroupStatement();
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
            result.setRenameView(new SimpleTableSegment(new TableNameSegment(nameContext.getStart().getStartIndex(),
                    nameContext.getStop().getStopIndex(), (IdentifierValue) visit(nameContext.identifier()))));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(((IdentifierValue) visit(ctx.name())).getValue(), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitAlterRoutine(final AlterRoutineContext ctx) {
        return new PostgreSQLAlterRoutineStatement();
    }
    
    @Override
    public ASTNode visitAlterRule(final AlterRuleContext ctx) {
        return new PostgreSQLAlterRuleStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropRoutine(final DropRoutineContext ctx) {
        return new PostgreSQLDropRoutineStatement();
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
    public ASTNode visitDropCast(final DropCastContext ctx) {
        return new PostgreSQLDropCastStatement();
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
    public ASTNode visitDropTextSearch(final DropTextSearchContext ctx) {
        return new PostgreSQLDropTextSearchStatement();
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
    public ASTNode visitAlterServer(final AlterServerContext ctx) {
        return new AlterServerStatement();
    }
    
    @Override
    public ASTNode visitAlterStatistics(final AlterStatisticsContext ctx) {
        return new PostgreSQLAlterStatisticsStatement();
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
    public ASTNode visitAlterTextSearchConfiguration(final AlterTextSearchConfigurationContext ctx) {
        return new PostgreSQLAlterTextSearchStatement();
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
    public ASTNode visitDiscard(final DiscardContext ctx) {
        return new PostgreSQLDiscardStatement();
    }
    
    @Override
    public ASTNode visitDropOwned(final DropOwnedContext ctx) {
        return new PostgreSQLDropOwnedStatement();
    }
    
    @Override
    public ASTNode visitDropOperator(final DropOperatorContext ctx) {
        return new DropOperatorStatement();
    }
    
    @Override
    public ASTNode visitDropMaterializedView(final DropMaterializedViewContext ctx) {
        return new DropMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitDropEventTrigger(final DropEventTriggerContext ctx) {
        return new PostgreSQLDropEventTriggerStatement();
    }
    
    @Override
    public ASTNode visitDropAggregate(final DropAggregateContext ctx) {
        return new PostgreSQLDropAggregateStatement();
    }
    
    @Override
    public ASTNode visitDropCollation(final DropCollationContext ctx) {
        return new PostgreSQLDropCollationStatement();
    }
    
    @Override
    public ASTNode visitDropForeignDataWrapper(final DropForeignDataWrapperContext ctx) {
        return new PostgreSQLDropForeignDataWrapperStatement();
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new DropTriggerStatement();
    }
    
    @Override
    public ASTNode visitDropType(final DropTypeContext ctx) {
        return new PostgreSQLDropTypeStatement();
    }
    
    @Override
    public ASTNode visitComment(final CommentContext ctx) {
        if (null != ctx.commentClauses().objectTypeAnyName() && null != ctx.commentClauses().objectTypeAnyName().TABLE()) {
            return commentOnTable(ctx);
        }
        if (null != ctx.commentClauses().COLUMN()) {
            return commentOnColumn(ctx);
        }
        if (null != ctx.commentClauses().objectTypeNameOnAnyName()) {
            return getTableFromComment(ctx);
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
    
    private CommentStatement getTableFromComment(final CommentContext ctx) {
        CommentStatement result = new CommentStatement();
        result.setTable((SimpleTableSegment) visit(ctx.commentClauses().tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropOperatorClass(final DropOperatorClassContext ctx) {
        return new PostgreSQLDropOperatorClassStatement();
    }
    
    @Override
    public ASTNode visitDropOperatorFamily(final DropOperatorFamilyContext ctx) {
        return new PostgreSQLDropOperatorFamilyStatement();
    }
    
    @Override
    public ASTNode visitDropAccessMethod(final DropAccessMethodContext ctx) {
        return new PostgreSQLDropAccessMethodStatement();
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new DropServerStatement();
    }
    
    @Override
    public ASTNode visitDeclare(final DeclareContext ctx) {
        return new DeclareStatement((CursorNameSegment) visit(ctx.cursorName()), (SelectStatement) visit(ctx.select()));
    }
    
    @Override
    public ASTNode visitFetch(final FetchContext ctx) {
        return new FetchStatement((CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitMove(final MoveContext ctx) {
        return new MoveStatement((CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitClose(final CloseContext ctx) {
        return new CloseStatement(null == ctx.cursorName() ? null : (CursorNameSegment) visit(ctx.cursorName()), null != ctx.ALL());
    }
    
    @Override
    public ASTNode visitCursorName(final CursorNameContext ctx) {
        return null != ctx.name() ? new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()))
                : new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.hostVariable()));
    }
    
    @Override
    public ASTNode visitCluster(final ClusterContext ctx) {
        return new ClusterStatement(null == ctx.tableName() ? null : (SimpleTableSegment) visit(ctx.tableName()),
                null == ctx.clusterIndexSpecification() ? null : (IndexSegment) visit(ctx.clusterIndexSpecification().indexName()));
    }
    
    @Override
    public ASTNode visitCreateAccessMethod(final CreateAccessMethodContext ctx) {
        return new PostgreSQLCreateAccessMethodStatement();
    }
    
    @Override
    public ASTNode visitCreateAggregate(final CreateAggregateContext ctx) {
        return new PostgreSQLCreateAggregateStatement();
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
    
    @Override
    public ASTNode visitCreateCast(final CreateCastContext ctx) {
        return new PostgreSQLCreateCastStatement();
    }
    
    @Override
    public ASTNode visitListen(final ListenContext ctx) {
        return new ListenStatement(ctx.channelName().getText());
    }
    
    @Override
    public ASTNode visitUnlisten(final UnlistenContext ctx) {
        return new UnlistenStatement();
    }
    
    @Override
    public ASTNode visitNotifyStmt(final NotifyStmtContext ctx) {
        return new PostgreSQLNotifyStmtStatement();
    }
    
    @Override
    public ASTNode visitCreateCollation(final CreateCollationContext ctx) {
        return new CreateCollationStatement();
    }
    
    @Override
    public ASTNode visitRefreshMatViewStmt(final RefreshMatViewStmtContext ctx) {
        return new RefreshMatViewStmtStatement();
    }
    
    @Override
    public ASTNode visitReindex(final ReindexContext ctx) {
        return new ReindexStatement();
    }
    
    @Override
    public ASTNode visitSecurityLabelStmt(final SecurityLabelStmtContext ctx) {
        return new SecurityLabelStmtStatement();
    }
    
    @Override
    public ASTNode visitCreateEventTrigger(final CreateEventTriggerContext ctx) {
        return new PostgreSQLCreateEventTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateForeignDataWrapper(final CreateForeignDataWrapperContext ctx) {
        return new PostgreSQLCreateForeignDataWrapperStatement();
    }
    
    @Override
    public ASTNode visitCreateForeignTable(final CreateForeignTableContext ctx) {
        return new PostgreSQLCreateForeignTableStatement();
    }
    
    @Override
    public ASTNode visitCreateMaterializedView(final CreateMaterializedViewContext ctx) {
        return new CreateMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitCreateOperator(final CreateOperatorContext ctx) {
        return new CreateOperatorStatement();
    }
    
    @Override
    public ASTNode visitCreatePolicy(final CreatePolicyContext ctx) {
        return new PostgreSQLCreatePolicyStatement();
    }
    
    @Override
    public ASTNode visitCreatePublication(final CreatePublicationContext ctx) {
        return new PostgreSQLCreatePublicationStatement();
    }
    
    @Override
    public ASTNode visitOpen(final OpenContext ctx) {
        return new OpenStatement((CursorNameSegment) visit(ctx.cursorName()));
    }
}
