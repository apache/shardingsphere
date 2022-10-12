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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DDLSQLVisitor;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterAggregateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterCollationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterConversionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterExtensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterOperatorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterPolicyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterPublicationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterRoutineStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterStatisticsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTextSearchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCloseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCommentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateAccessMethodStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateCastStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateCollationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateConversionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateExtensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateForeignTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateOperatorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreatePolicyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTextSearchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDeallocateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDiscardStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropAccessMethodStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropAggregateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropCastStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropCollationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropConversionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDomainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropExtensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropForeignDataWrapperStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropForeignTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropLanguageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropOperatorClassStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropOperatorFamilyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropOperatorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropOwnedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropPolicyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropPublicationStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropRoutineStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropStatisticsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTextSearchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLFetchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLListenStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLMoveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLNotifyStmtStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLRefreshMatViewStmtStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLReindexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLSecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLUnlistenStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

/**
 * DDL Statement SQL visitor for PostgreSQL.
 */
@NoArgsConstructor
public final class PostgreSQLDDLStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DDLSQLVisitor, SQLStatementVisitor {
    
    public PostgreSQLDDLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        PostgreSQLCreateTableStatement result = new PostgreSQLCreateTableStatement(null != ctx.ifNotExists());
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
        PostgreSQLAlterTableStatement result = new PostgreSQLAlterTableStatement();
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
        return new PostgreSQLAlterMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitAlterOperator(final AlterOperatorContext ctx) {
        return new PostgreSQLAlterOperatorStatement();
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
        return new PostgreSQLAlterDomainStatement();
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
        return new PostgreSQLAlterTriggerStatement();
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new PostgreSQLAlterTypeStatement();
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
        PostgreSQLDropTableStatement result = new PostgreSQLDropTableStatement(null != ctx.ifExists(), containsCascade);
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        PostgreSQLTruncateStatement result = new PostgreSQLTruncateStatement();
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
        PostgreSQLCreateIndexStatement result = new PostgreSQLCreateIndexStatement(null != ctx.ifNotExists());
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
        PostgreSQLAlterIndexStatement result = new PostgreSQLAlterIndexStatement();
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
        PostgreSQLDropIndexStatement result = new PostgreSQLDropIndexStatement(null != ctx.ifExists());
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
        return new PostgreSQLAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new PostgreSQLAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return new PostgreSQLCreateFunctionStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return new PostgreSQLCreateProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new PostgreSQLDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropGroup(final DropGroupContext ctx) {
        return new PostgreSQLDropGroupStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        PostgreSQLDropViewStatement result = new PostgreSQLDropViewStatement();
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        PostgreSQLCreateViewStatement result = new PostgreSQLCreateViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.qualifiedName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        PostgreSQLAlterViewStatement result = new PostgreSQLAlterViewStatement();
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
        PostgreSQLDropDatabaseStatement result = new PostgreSQLDropDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        result.setIfExists(null != ctx.ifExists());
        return result;
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
        return new PostgreSQLDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropRoutine(final DropRoutineContext ctx) {
        return new PostgreSQLDropRoutineStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        PostgreSQLCreateDatabaseStatement result = new PostgreSQLCreateDatabaseStatement();
        result.setDatabaseName(((IdentifierValue) visit(ctx.name())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        PostgreSQLCreateSequenceStatement result = new PostgreSQLCreateSequenceStatement();
        result.setSequenceName(((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        PostgreSQLAlterSequenceStatement result = new PostgreSQLAlterSequenceStatement();
        result.setSequenceName(((SimpleTableSegment) visit(ctx.qualifiedName())).getTableName().getIdentifier().getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        PostgreSQLDropSequenceStatement result = new PostgreSQLDropSequenceStatement();
        result.setSequenceNames(((CollectionValue) visit(ctx.qualifiedNameList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitPrepare(final PrepareContext ctx) {
        PostgreSQLPrepareStatement result = new PostgreSQLPrepareStatement();
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
        return new PostgreSQLDeallocateStatement();
    }
    
    @Override
    public ASTNode visitDropCast(final DropCastContext ctx) {
        return new PostgreSQLDropCastStatement();
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new PostgreSQLCreateTablespaceStatement();
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        return new PostgreSQLAlterTablespaceStatement();
    }
    
    @Override
    public ASTNode visitDropTablespace(final DropTablespaceContext ctx) {
        return new PostgreSQLDropTablespaceStatement();
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
        return new PostgreSQLCreateDomainStatement();
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
        PostgreSQLCreateSchemaStatement result = new PostgreSQLCreateSchemaStatement();
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
        PostgreSQLAlterSchemaStatement result = new PostgreSQLAlterSchemaStatement();
        result.setSchemaName((IdentifierValue) visit(ctx.name().get(0)));
        if (ctx.name().size() > 1) {
            result.setRenameSchema((IdentifierValue) visit(ctx.name().get(1)));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropSchema(final DropSchemaContext ctx) {
        PostgreSQLDropSchemaStatement result = new PostgreSQLDropSchemaStatement();
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
        return new PostgreSQLAlterServerStatement();
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
        return new PostgreSQLCreateTypeStatement();
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
        return new PostgreSQLDropOperatorStatement();
    }
    
    @Override
    public ASTNode visitDropMaterializedView(final DropMaterializedViewContext ctx) {
        return new PostgreSQLDropMaterializedViewStatement();
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
        return new PostgreSQLDropTriggerStatement();
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
        return new PostgreSQLCommentStatement();
    }
    
    @SuppressWarnings("unchecked")
    private PostgreSQLCommentStatement commentOnColumn(final CommentContext ctx) {
        PostgreSQLCommentStatement result = new PostgreSQLCommentStatement();
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        Optional<NameSegment> columnName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        columnName.ifPresent(optional -> result.setColumn(new ColumnSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier())));
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private PostgreSQLCommentStatement commentOnTable(final CommentContext ctx) {
        PostgreSQLCommentStatement result = new PostgreSQLCommentStatement();
        Iterator<NameSegment> nameSegmentIterator = ((CollectionValue<NameSegment>) visit(ctx.commentClauses().anyName())).getValue().iterator();
        setTableSegment(result, nameSegmentIterator);
        return result;
    }
    
    private void setTableSegment(final PostgreSQLCommentStatement statement, final Iterator<NameSegment> nameSegmentIterator) {
        Optional<NameSegment> tableName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        tableName.ifPresent(optional -> statement.setTable(new SimpleTableSegment(new TableNameSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier()))));
        Optional<NameSegment> schemaName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        schemaName.ifPresent(optional -> statement.getTable().setOwner(new OwnerSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier())));
        Optional<NameSegment> databaseName = nameSegmentIterator.hasNext() ? Optional.of(nameSegmentIterator.next()) : Optional.empty();
        databaseName.ifPresent(optional -> statement.getTable().getOwner()
                .ifPresent(owner -> owner.setOwner(new OwnerSegment(optional.getStartIndex(), optional.getStopIndex(), optional.getIdentifier()))));
    }
    
    private PostgreSQLCommentStatement getTableFromComment(final CommentContext ctx) {
        PostgreSQLCommentStatement result = new PostgreSQLCommentStatement();
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
        return new PostgreSQLDropServerStatement();
    }
    
    @Override
    public ASTNode visitDeclare(final DeclareContext ctx) {
        PostgreSQLDeclareStatement result = new PostgreSQLDeclareStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        result.setSelect((SelectStatement) visit(ctx.select()));
        return result;
    }
    
    @Override
    public ASTNode visitFetch(final FetchContext ctx) {
        PostgreSQLFetchStatement result = new PostgreSQLFetchStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        if (null != ctx.direction()) {
            result.setDirection((DirectionSegment) visit(ctx.direction()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitMove(final MoveContext ctx) {
        PostgreSQLMoveStatement result = new PostgreSQLMoveStatement();
        result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        if (null != ctx.direction()) {
            result.setDirection((DirectionSegment) visit(ctx.direction()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitClose(final CloseContext ctx) {
        PostgreSQLCloseStatement result = new PostgreSQLCloseStatement();
        if (null != ctx.cursorName()) {
            result.setCursorName((CursorNameSegment) visit(ctx.cursorName()));
        }
        result.setCloseAll(null != ctx.ALL());
        return result;
    }
    
    @Override
    public ASTNode visitCursorName(final CursorNameContext ctx) {
        return new CursorNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public ASTNode visitCluster(final ClusterContext ctx) {
        PostgreSQLClusterStatement result = new PostgreSQLClusterStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.clusterIndexSpecification()) {
            result.setIndex((IndexSegment) visit(ctx.clusterIndexSpecification().indexName()));
        }
        return result;
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
    
    @Override
    public ASTNode visitCreateCast(final CreateCastContext ctx) {
        return new PostgreSQLCreateCastStatement();
    }
    
    @Override
    public ASTNode visitListen(final ListenContext ctx) {
        String channelName = ctx.channelName().getText();
        PostgreSQLListenStatement result = new PostgreSQLListenStatement();
        result.setChannelName(channelName);
        return result;
    }
    
    @Override
    public ASTNode visitUnlisten(final UnlistenContext ctx) {
        return new PostgreSQLUnlistenStatement();
    }
    
    @Override
    public ASTNode visitNotifyStmt(final NotifyStmtContext ctx) {
        return new PostgreSQLNotifyStmtStatement();
    }
    
    @Override
    public ASTNode visitCreateCollation(final CreateCollationContext ctx) {
        return new PostgreSQLCreateCollationStatement();
    }
    
    @Override
    public ASTNode visitRefreshMatViewStmt(final RefreshMatViewStmtContext ctx) {
        return new PostgreSQLRefreshMatViewStmtStatement();
    }
    
    @Override
    public ASTNode visitReindex(final ReindexContext ctx) {
        return new PostgreSQLReindexStatement();
    }
    
    @Override
    public ASTNode visitSecurityLabelStmt(final SecurityLabelStmtContext ctx) {
        return new PostgreSQLSecurityLabelStmtStatement();
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
        return new PostgreSQLCreateMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitCreateOperator(final CreateOperatorContext ctx) {
        return new PostgreSQLCreateOperatorStatement();
    }
    
    @Override
    public ASTNode visitCreatePolicy(final CreatePolicyContext ctx) {
        return new PostgreSQLCreatePolicyStatement();
    }
    
    @Override
    public ASTNode visitCreatePublication(final CreatePublicationContext ctx) {
        return new PostgreSQLCreatePublicationStatement();
    }
}
