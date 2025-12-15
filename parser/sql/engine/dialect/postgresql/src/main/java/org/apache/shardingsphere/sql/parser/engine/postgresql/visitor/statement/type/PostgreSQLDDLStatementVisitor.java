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

package org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AbsoluteCountContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AllContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterAggregateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterDatabaseContext;
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
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.PostgreSQLStatementVisitor;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DeallocateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.collation.AlterCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.collation.CreateCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.collation.DropCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.AlterDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.CreateDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.DropDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.operator.AlterOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.operator.CreateOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.operator.DropOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.AlterServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.DropServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.AlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.DropTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.AlterTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.CreateTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.DropTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.RefreshMatViewStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDiscardStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropOperatorClassStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropOperatorFamilyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropOwnedStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLListenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLNotifyStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLOpenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLSecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLUnlistenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.accessmethod.PostgreSQLCreateAccessMethodStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.accessmethod.PostgreSQLDropAccessMethodStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.aggregate.PostgreSQLAlterAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.aggregate.PostgreSQLCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.aggregate.PostgreSQLDropAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.cast.PostgreSQLCreateCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.cast.PostgreSQLDropCastStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLAlterConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLCreateConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLDropConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.event.PostgreSQLCreateEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.event.PostgreSQLDropEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.extension.PostgreSQLAlterExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.extension.PostgreSQLCreateExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.extension.PostgreSQLDropExtensionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigndata.PostgreSQLAlterForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigndata.PostgreSQLCreateForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigndata.PostgreSQLDropForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigntable.PostgreSQLAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigntable.PostgreSQLCreateForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigntable.PostgreSQLDropForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.group.PostgreSQLAlterGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.group.PostgreSQLDropGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.index.PostgreSQLReindexStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.language.PostgreSQLAlterLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.language.PostgreSQLCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.language.PostgreSQLDropLanguageStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.policy.PostgreSQLAlterPolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.policy.PostgreSQLCreatePolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.policy.PostgreSQLDropPolicyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLAlterPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLDropPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.routine.PostgreSQLAlterRoutineStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.routine.PostgreSQLDropRoutineStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.rule.PostgreSQLAlterRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.rule.PostgreSQLCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.rule.PostgreSQLDropRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.statistics.PostgreSQLAlterStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.statistics.PostgreSQLDropStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.subscription.PostgreSQLAlterSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.subscription.PostgreSQLDropSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.textsearch.PostgreSQLAlterTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.textsearch.PostgreSQLCreateTextSearchStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.textsearch.PostgreSQLDropTextSearchStatement;

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
    
    public PostgreSQLDDLStatementVisitor(final DatabaseType databaseType) {
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
    public ASTNode visitAlterCollation(final AlterCollationContext ctx) {
        return new AlterCollationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterDefaultPrivileges(final AlterDefaultPrivilegesContext ctx) {
        return new PostgreSQLAlterDefaultPrivilegesStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterForeignDataWrapper(final AlterForeignDataWrapperContext ctx) {
        return new PostgreSQLAlterForeignDataWrapperStatement(getDatabaseType());
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
    public ASTNode visitDropForeignTable(final DropForeignTableContext ctx) {
        return new PostgreSQLDropForeignTableStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterGroup(final AlterGroupContext ctx) {
        return new PostgreSQLAlterGroupStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new AlterMaterializedViewStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterOperator(final AlterOperatorContext ctx) {
        return new AlterOperatorStatement(getDatabaseType());
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
    public ASTNode visitAlterPolicy(final AlterPolicyContext ctx) {
        return new PostgreSQLAlterPolicyStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterPublication(final AlterPublicationContext ctx) {
        return new PostgreSQLAlterPublicationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterSubscription(final AlterSubscriptionContext ctx) {
        return new PostgreSQLAlterSubscriptionStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new AlterTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new AlterTypeStatement(getDatabaseType());
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
    
    @Override
    public ASTNode visitDropPolicy(final DropPolicyContext ctx) {
        return new PostgreSQLDropPolicyStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropRule(final DropRuleContext ctx) {
        return new PostgreSQLDropRuleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropStatistics(final DropStatisticsContext ctx) {
        return new PostgreSQLDropStatisticsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropPublication(final DropPublicationContext ctx) {
        return new PostgreSQLDropPublicationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropSubscription(final DropSubscriptionContext ctx) {
        return new PostgreSQLDropSubscriptionStatement(getDatabaseType());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement(getDatabaseType());
        result.setIfNotExists(null != ctx.ifNotExists());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.indexParams())).getValue());
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        } else {
            result.setAnonymousIndexStartIndex(ctx.ON().getSymbol().getStartIndex() - 1);
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
    
    @Override
    public ASTNode visitDropGroup(final DropGroupContext ctx) {
        return new PostgreSQLDropGroupStatement(getDatabaseType());
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
            result.setRenameView(new SimpleTableSegment(new TableNameSegment(nameContext.getStart().getStartIndex(),
                    nameContext.getStop().getStopIndex(), (IdentifierValue) visit(nameContext.identifier()))));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new DropDatabaseStatement(getDatabaseType(), ((IdentifierValue) visit(ctx.name())).getValue(), null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new AlterDatabaseStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterRoutine(final AlterRoutineContext ctx) {
        return new PostgreSQLAlterRoutineStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterRule(final AlterRuleContext ctx) {
        return new PostgreSQLAlterRuleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new DropProcedureStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropRoutine(final DropRoutineContext ctx) {
        return new PostgreSQLDropRoutineStatement(getDatabaseType());
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
    public ASTNode visitDropCast(final DropCastContext ctx) {
        return new PostgreSQLDropCastStatement(getDatabaseType());
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
    public ASTNode visitDropTextSearch(final DropTextSearchContext ctx) {
        return new PostgreSQLDropTextSearchStatement(getDatabaseType());
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
    public ASTNode visitAlterServer(final AlterServerContext ctx) {
        return new AlterServerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterStatistics(final AlterStatisticsContext ctx) {
        return new PostgreSQLAlterStatisticsStatement(getDatabaseType());
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
    public ASTNode visitAlterTextSearchConfiguration(final AlterTextSearchConfigurationContext ctx) {
        return new PostgreSQLAlterTextSearchStatement(getDatabaseType());
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
    public ASTNode visitDiscard(final DiscardContext ctx) {
        return new PostgreSQLDiscardStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropOwned(final DropOwnedContext ctx) {
        return new PostgreSQLDropOwnedStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropOperator(final DropOperatorContext ctx) {
        return new DropOperatorStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropMaterializedView(final DropMaterializedViewContext ctx) {
        return new DropMaterializedViewStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropEventTrigger(final DropEventTriggerContext ctx) {
        return new PostgreSQLDropEventTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropAggregate(final DropAggregateContext ctx) {
        return new PostgreSQLDropAggregateStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropCollation(final DropCollationContext ctx) {
        return new DropCollationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropForeignDataWrapper(final DropForeignDataWrapperContext ctx) {
        return new PostgreSQLDropForeignDataWrapperStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new DropTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropType(final DropTypeContext ctx) {
        return new DropTypeStatement(getDatabaseType());
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
    
    private CommentStatement getTableFromComment(final CommentContext ctx) {
        CommentStatement result = new CommentStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.commentClauses().tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropOperatorClass(final DropOperatorClassContext ctx) {
        return new PostgreSQLDropOperatorClassStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropOperatorFamily(final DropOperatorFamilyContext ctx) {
        return new PostgreSQLDropOperatorFamilyStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropAccessMethod(final DropAccessMethodContext ctx) {
        return new PostgreSQLDropAccessMethodStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropServer(final DropServerContext ctx) {
        return new DropServerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDeclare(final DeclareContext ctx) {
        return new PostgreSQLDeclareStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), (SelectStatement) visit(ctx.select()));
    }
    
    @Override
    public ASTNode visitFetch(final FetchContext ctx) {
        return new FetchStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitMove(final MoveContext ctx) {
        return new MoveStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()), null == ctx.direction() ? null : (DirectionSegment) visit(ctx.direction()));
    }
    
    @Override
    public ASTNode visitClose(final CloseContext ctx) {
        return new CloseStatement(getDatabaseType(), null == ctx.cursorName() ? null : (CursorNameSegment) visit(ctx.cursorName()), null != ctx.ALL());
    }
    
    @Override
    public ASTNode visitCursorName(final CursorNameContext ctx) {
        return null != ctx.name() ? new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()))
                : new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.hostVariable()));
    }
    
    @Override
    public ASTNode visitCluster(final ClusterContext ctx) {
        return new PostgreSQLClusterStatement(getDatabaseType(), null == ctx.tableName() ? null : (SimpleTableSegment) visit(ctx.tableName()),
                null == ctx.clusterIndexSpecification() ? null : (IndexSegment) visit(ctx.clusterIndexSpecification().indexName()));
    }
    
    @Override
    public ASTNode visitCreateAccessMethod(final CreateAccessMethodContext ctx) {
        return new PostgreSQLCreateAccessMethodStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateAggregate(final CreateAggregateContext ctx) {
        return new PostgreSQLCreateAggregateStatement(getDatabaseType());
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
        return new PostgreSQLCreateCastStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitListen(final ListenContext ctx) {
        return new PostgreSQLListenStatement(getDatabaseType(), ctx.channelName().getText());
    }
    
    @Override
    public ASTNode visitUnlisten(final UnlistenContext ctx) {
        return new PostgreSQLUnlistenStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitNotifyStmt(final NotifyStmtContext ctx) {
        return new PostgreSQLNotifyStmtStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateCollation(final CreateCollationContext ctx) {
        return new CreateCollationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitRefreshMatViewStmt(final RefreshMatViewStmtContext ctx) {
        return new RefreshMatViewStmtStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitReindex(final ReindexContext ctx) {
        return new PostgreSQLReindexStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSecurityLabelStmt(final SecurityLabelStmtContext ctx) {
        return new PostgreSQLSecurityLabelStmtStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateEventTrigger(final CreateEventTriggerContext ctx) {
        return new PostgreSQLCreateEventTriggerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateForeignDataWrapper(final CreateForeignDataWrapperContext ctx) {
        return new PostgreSQLCreateForeignDataWrapperStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateForeignTable(final CreateForeignTableContext ctx) {
        return new PostgreSQLCreateForeignTableStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateMaterializedView(final CreateMaterializedViewContext ctx) {
        return new CreateMaterializedViewStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreateOperator(final CreateOperatorContext ctx) {
        return new CreateOperatorStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreatePolicy(final CreatePolicyContext ctx) {
        return new PostgreSQLCreatePolicyStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitCreatePublication(final CreatePublicationContext ctx) {
        return new PostgreSQLCreatePublicationStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitOpen(final OpenContext ctx) {
        return new PostgreSQLOpenStatement(getDatabaseType(), (CursorNameSegment) visit(ctx.cursorName()));
    }
}
