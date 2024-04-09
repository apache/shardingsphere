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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterAnalyticViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterAttributeDimensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterAuditPolicyContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterClusterContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDatabaseDictionaryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDatabaseLinkContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDimensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterDiskgroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterFlashbackArchiveContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterHierarchyContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterIndexTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterInmemoryJoinGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterJavaContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterLibraryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterLockdownProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterMaterializedViewLogContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterMaterializedZonemapContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterOperatorContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterOutlineContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterPackageContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterPluggableDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterRollbackSegmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterSessionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterSynonymContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterSystemContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AnalyzeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AssociateStatisticsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AuditContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AuditTraditionalContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AuditUnifiedContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BodyContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CollectionVariableDeclContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnClausesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnOrVirtualDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CommentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ConstraintClausesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateClusterContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateContextContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateControlFileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDatabaseLinkContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDimensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDirectoryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateDiskgroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateEditionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateFlashbackArchiveContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateInmemoryJoinGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateJavaContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateLibraryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateLockdownProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateMaterializedViewLogContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateOperatorContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreatePFileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateRelationalTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateRestorePointContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateRollbackSegmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateSPFileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateSynonymContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateTablespaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CursorDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CursorForLoopStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DisassociateStatisticsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DmlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropClusterContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropColumnClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropConstraintClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropContextContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropDatabaseLinkContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropDimensionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropDirectoryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropDiskgroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropEditionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropFlashbackArchiveContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropIndexTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropInmemoryJoinGroupContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropJavaContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropLibraryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropLockdownProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropMaterializedViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropMaterializedViewLogContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropMaterializedZonemapContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropOperatorContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropOutlineContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropPackageContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropPluggableDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropProcedureContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropProfileContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropRestorePointContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropRollbackSegmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropSequenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropSynonymContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropTableSpaceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropTriggerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DynamicSqlStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExceptionHandlerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FlashbackDatabaseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FlashbackTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexExpressionsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InlineConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ItemDeclarationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ModifyColPropertiesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ModifyCollectionRetrievalContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ModifyConstraintClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.NestedTableTypeSpecContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.NoAuditContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ObjectBaseTypeDefContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ObjectSubTypeDefContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ObjectTypeDefContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OpenForStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OperateColumnClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OutOfLineConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OutOfLineRefConstraintContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PackageNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ParameterDeclarationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PlsqlBlockContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PlsqlFunctionSourceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PlsqlProcedureSourceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ProcedureCallContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PurgeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RelationalPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RenameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectIntoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SqlStatementInPlsqlContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.StatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SwitchContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SystemActionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.VariableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.VarrayTypeSpecContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropDatabaseContext;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyCollectionRetrievalSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.tablespace.TablespaceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.type.TypeDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterAnalyticViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterAttributeDimensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterAuditPolicyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterDatabaseDictionaryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterDatabaseLinkStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterDimensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterDiskgroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterFlashbackArchiveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterHierarchyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterIndexTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterInmemoryJoinGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterJavaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterLibraryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterLockdownProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterMaterializedViewLogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterMaterializedZonemapStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterOperatorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterOutlineStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterPackageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterPluggableDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterRollbackSegmentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterSessionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterSystemStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAnalyzeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAssociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAuditStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCommentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateContextStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateControlFileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateDatabaseLinkStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateDimensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateDirectoryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateDiskgroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateEditionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateFlashbackArchiveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateInmemoryJoinGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateJavaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateLibraryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateLockdownProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateMaterializedViewLogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateNestedTableTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateObjectTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateOperatorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreatePFileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateRestorePointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateRollbackSegmentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateSPFileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateSubTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateVarrayTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDisassociateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropContextStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropDatabaseLinkStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropDimensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropDirectoryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropDiskgroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropEditionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropFlashbackArchiveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropIndexTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropInmemoryJoinGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropJavaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropLibraryStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropLockdownProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropMaterializedViewLogStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropMaterializedZonemapStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropOperatorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropOutlineStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropPackageStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropPluggableDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropProfileStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropRestorePointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropRollbackSegmentStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropSynonymStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTableSpaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTriggerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTypeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleFlashbackDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleFlashbackTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleNoAuditStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OraclePLSQLBlockStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OraclePurgeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleRenameStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleSwitchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleSystemActionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleTruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.CursorForLoopStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.ProcedureBodyEndNameSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.SQLStatementSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DDL statement visitor for Oracle.
 */
public final class OracleDDLStatementVisitor extends OracleStatementVisitor implements DDLStatementVisitor {
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        OracleCreateViewStatement result = new OracleCreateViewStatement();
        OracleDMLStatementVisitor visitor = new OracleDMLStatementVisitor();
        getGlobalParameterMarkerSegments().addAll(visitor.getGlobalParameterMarkerSegments());
        getStatementParameterMarkerSegments().addAll(visitor.getStatementParameterMarkerSegments());
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setSelect((SelectStatement) visitor.visit(ctx.select()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.addParameterMarkerSegments(getGlobalParameterMarkerSegments());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        OracleCreateTableStatement result = new OracleCreateTableStatement();
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
    public ASTNode visitCreateType(final CreateTypeContext ctx) {
        boolean isReplace = null != ctx.REPLACE();
        boolean isEditionable = null == ctx.NONEDITIONABLE();
        TypeSegment typeSegment = (TypeSegment) visit(ctx.plsqlTypeSource().typeName());
        if (null != ctx.plsqlTypeSource().objectSubTypeDef()) {
            ObjectSubTypeDefContext objectSubTypeDefContext = ctx.plsqlTypeSource().objectSubTypeDef();
            return new OracleCreateSubTypeStatement(isReplace, isEditionable,
                    null == objectSubTypeDefContext.finalClause() || null == objectSubTypeDefContext.finalClause().NOT(),
                    null == objectSubTypeDefContext.instantiableClause() || null == objectSubTypeDefContext.instantiableClause().NOT(),
                    typeSegment,
                    objectSubTypeDefContext.dataTypeDefinition().stream().map(definition -> (TypeDefinitionSegment) visit(definition)).collect(Collectors.toList()));
        } else {
            return visitCreateTypeObjectBaseTypeDef(ctx.plsqlTypeSource().objectBaseTypeDef(), isReplace, isEditionable, typeSegment);
        }
    }
    
    private ASTNode visitCreateTypeObjectBaseTypeDef(final ObjectBaseTypeDefContext ctx, final boolean isReplace, final boolean isEditionable, final TypeSegment typeSegment) {
        if (null != ctx.objectTypeDef()) {
            ObjectTypeDefContext objectTypeDefContext = ctx.objectTypeDef();
            return new OracleCreateObjectTypeStatement(isReplace, isEditionable, null == objectTypeDefContext.finalClause() || null == objectTypeDefContext.finalClause().NOT(),
                    null == objectTypeDefContext.instantiableClause() || null == objectTypeDefContext.instantiableClause().NOT(),
                    null == objectTypeDefContext.persistableClause() || null == objectTypeDefContext.persistableClause().NOT(),
                    typeSegment, objectTypeDefContext.dataTypeDefinition().stream().map(definition -> (TypeDefinitionSegment) visit(definition)).collect(Collectors.toList()));
        } else if (null != ctx.varrayTypeSpec()) {
            VarrayTypeSpecContext varrayTypeSpecContext = ctx.varrayTypeSpec();
            return new OracleCreateVarrayTypeStatement(isReplace, isEditionable,
                    null == varrayTypeSpecContext.INTEGER_() ? -1 : Integer.parseInt(varrayTypeSpecContext.INTEGER_().getText()),
                    null != varrayTypeSpecContext.typeSpec().NULL(),
                    null == varrayTypeSpecContext.typeSpec().persistableClause() || null == varrayTypeSpecContext.typeSpec().persistableClause().NOT(),
                    typeSegment,
                    (DataTypeSegment) visit(varrayTypeSpecContext.typeSpec().dataType()));
        } else {
            NestedTableTypeSpecContext nestedTableTypeSpecContext = ctx.nestedTableTypeSpec();
            return new OracleCreateNestedTableTypeStatement(isReplace, isEditionable,
                    null != nestedTableTypeSpecContext.typeSpec().NULL(),
                    null == nestedTableTypeSpecContext.typeSpec().persistableClause() || null == nestedTableTypeSpecContext.typeSpec().persistableClause().NOT(),
                    typeSegment,
                    (DataTypeSegment) visit(nestedTableTypeSpecContext.typeSpec().dataType()));
        }
    }
    
    @Override
    public ASTNode visitDataTypeDefinition(final DataTypeDefinitionContext ctx) {
        return new TypeDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.name().getText(), (DataTypeSegment) visit(ctx.dataType()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.createRelationalTableClause()) {
            result.combine((CollectionValue<CreateDefinitionSegment>) visit(ctx.createRelationalTableClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateRelationalTableClause(final CreateRelationalTableClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        if (null == ctx.relationalProperties()) {
            return result;
        }
        for (RelationalPropertyContext each : ctx.relationalProperties().relationalProperty()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
            if (null != each.outOfLineConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.outOfLineConstraint()));
            }
            if (null != each.outOfLineRefConstraint()) {
                result.getValue().add((ConstraintDefinitionSegment) visit(each.outOfLineRefConstraint()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataType = null == ctx.dataType() ? null : (DataTypeSegment) visit(ctx.dataType());
        boolean isPrimaryKey = ctx.inlineConstraint().stream().anyMatch(each -> null != each.primaryKey());
        boolean isNotNull = ctx.inlineConstraint().stream().anyMatch(each -> null != each.NOT() && null != each.NULL());
        ColumnDefinitionSegment result = new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, isPrimaryKey, isNotNull);
        if (null != ctx.REF() && null != ctx.dataType()) {
            result.setRef(true);
        }
        for (InlineConstraintContext each : ctx.inlineConstraint()) {
            if (null != each.referencesClause()) {
                result.getReferencedTables().add((SimpleTableSegment) visit(each.referencesClause().tableName()));
            }
        }
        if (null != ctx.inlineRefConstraint()) {
            result.getReferencedTables().add((SimpleTableSegment) visit(ctx.inlineRefConstraint().tableName()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitOutOfLineConstraint(final OutOfLineConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintName()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintName()));
        }
        if (null != ctx.primaryKey()) {
            result.getPrimaryKeyColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue());
        }
        if (null != ctx.UNIQUE()) {
            result.getIndexColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue());
        }
        if (null != ctx.referencesClause()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.referencesClause().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitOutOfLineRefConstraint(final OutOfLineRefConstraintContext ctx) {
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.constraintName()) {
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintName()));
        }
        if (null != ctx.referencesClause()) {
            result.setReferencedTable((SimpleTableSegment) visit(ctx.referencesClause().tableName()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        OracleAlterTableStatement result = new OracleAlterTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
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
                } else if (each instanceof ModifyConstraintDefinitionSegment) {
                    result.getModifyConstraintDefinitions().add((ModifyConstraintDefinitionSegment) each);
                } else if (each instanceof DropConstraintDefinitionSegment) {
                    result.getDropConstraintDefinitions().add((DropConstraintDefinitionSegment) each);
                } else if (each instanceof ModifyCollectionRetrievalSegment) {
                    result.setModifyCollectionRetrieval((ModifyCollectionRetrievalSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTablespace(final AlterTablespaceContext ctx) {
        OracleAlterTablespaceStatement result = new OracleAlterTablespaceStatement();
        if (null != ctx.tablespaceName()) {
            result.setTablespaceSegment(
                    new TablespaceSegment(ctx.tablespaceName().getStart().getStartIndex(), ctx.tablespaceName().getStop().getStopIndex(), (IdentifierValue) visit(ctx.tablespaceName())));
        }
        if (null != ctx.newTablespaceName()) {
            result.setRenameTablespaceSegment(
                    new TablespaceSegment(ctx.newTablespaceName().getStart().getStartIndex(), ctx.newTablespaceName().getStop().getStopIndex(), (IdentifierValue) visit(ctx.newTablespaceName())));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.columnClauses()) {
            result.getValue().addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(ctx.columnClauses())).getValue());
        }
        if (null != ctx.constraintClauses()) {
            result.getValue().addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(ctx.constraintClauses())).getValue());
        }
        // TODO More alter definition parse
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitColumnClauses(final ColumnClausesContext ctx) {
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        for (OperateColumnClauseContext each : ctx.operateColumnClause()) {
            if (null != each.addColumnSpecification()) {
                result.getValue().addAll(((CollectionValue<AddColumnDefinitionSegment>) visit(each.addColumnSpecification())).getValue());
            }
            if (null != each.modifyColumnSpecification()) {
                result.getValue().add((ModifyColumnDefinitionSegment) visit(each.modifyColumnSpecification()));
            }
            if (null != each.dropColumnClause()) {
                result.getValue().add((DropColumnDefinitionSegment) visit(each.dropColumnClause()));
            }
        }
        if (null != ctx.modifyCollectionRetrieval()) {
            result.getValue().add((ModifyCollectionRetrievalSegment) visit(ctx.modifyCollectionRetrieval()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitConstraintClauses(final ConstraintClausesContext ctx) {
        // TODO Support rename constraint
        CollectionValue<AlterDefinitionSegment> result = new CollectionValue<>();
        if (null != ctx.addConstraintSpecification()) {
            result.combine((CollectionValue<AlterDefinitionSegment>) visit(ctx.addConstraintSpecification()));
        }
        if (null != ctx.modifyConstraintClause()) {
            result.getValue().add((AlterDefinitionSegment) visit(ctx.modifyConstraintClause()));
        }
        for (DropConstraintClauseContext each : ctx.dropConstraintClause()) {
            if (null != each.constraintName()) {
                result.getValue().add((AlterDefinitionSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyCollectionRetrieval(final ModifyCollectionRetrievalContext ctx) {
        return new ModifyCollectionRetrievalSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
        for (ColumnOrVirtualDefinitionContext each : ctx.columnOrVirtualDefinitions().columnOrVirtualDefinition()) {
            if (null != each.columnDefinition()) {
                AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
                        each.columnDefinition().getStart().getStartIndex(), each.columnDefinition().getStop().getStopIndex(),
                        Collections.singletonList((ColumnDefinitionSegment) visit(each.columnDefinition())));
                result.getValue().add(addColumnDefinition);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        // TODO handle no columnDefinition and multiple columnDefinitions
        ColumnDefinitionSegment columnDefinition = null;
        for (ModifyColPropertiesContext each : ctx.modifyColProperties()) {
            columnDefinition = (ColumnDefinitionSegment) visit(each);
        }
        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnDefinition);
    }
    
    @Override
    public ASTNode visitDropColumnClause(final DropColumnClauseContext ctx) {
        if (null != ctx.dropColumnSpecification()) {
            return visit(ctx.dropColumnSpecification());
        }
        Collection<ColumnSegment> columns = new LinkedList<>();
        if (null != ctx.columnOrColumnList().columnName()) {
            columns.add((ColumnSegment) visit(ctx.columnOrColumnList().columnName()));
        } else {
            for (ColumnNameContext each : ctx.columnOrColumnList().columnNames().columnName()) {
                columns.add((ColumnSegment) visit(each));
            }
        }
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    @Override
    public ASTNode visitModifyColProperties(final ModifyColPropertiesContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        DataTypeSegment dataType = null == ctx.dataType() ? null : (DataTypeSegment) visit(ctx.dataType());
        // TODO visit pk and reference table
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataType, false, false);
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        Collection<ColumnSegment> columns = new LinkedList<>();
        if (null != ctx.columnOrColumnList().columnName()) {
            columns.add((ColumnSegment) visit(ctx.columnOrColumnList().columnName()));
        } else {
            for (ColumnNameContext each : ctx.columnOrColumnList().columnNames().columnName()) {
                columns.add((ColumnSegment) visit(each));
            }
        }
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    @Override
    public ASTNode visitAddConstraintSpecification(final AddConstraintSpecificationContext ctx) {
        CollectionValue<AddConstraintDefinitionSegment> result = new CollectionValue<>();
        for (OutOfLineConstraintContext each : ctx.outOfLineConstraint()) {
            result.getValue().add(new AddConstraintDefinitionSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), (ConstraintDefinitionSegment) visit(each)));
        }
        if (null != ctx.outOfLineRefConstraint()) {
            result.getValue().add(new AddConstraintDefinitionSegment(ctx.outOfLineRefConstraint().getStart().getStartIndex(), ctx.outOfLineRefConstraint().getStop().getStopIndex(),
                    (ConstraintDefinitionSegment) visit(ctx.outOfLineRefConstraint())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitModifyConstraintClause(final ModifyConstraintClauseContext ctx) {
        if (null != ctx.constraintOption().constraintWithName()) {
            return new ModifyConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    (ConstraintSegment) visit(ctx.constraintOption().constraintWithName().constraintName()));
        } else {
            return new ModifyConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null);
        }
    }
    
    @Override
    public ASTNode visitDropConstraintClause(final DropConstraintClauseContext ctx) {
        return new DropConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ConstraintSegment) visit(ctx.constraintName()));
    }
    
    @Override
    public ASTNode visitDropContext(final DropContextContext ctx) {
        return new OracleDropContextStatement();
    }
    
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        OracleDropTableStatement result = new OracleDropTableStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropDatabaseLink(final DropDatabaseLinkContext ctx) {
        return new OracleDropDatabaseLinkStatement();
    }
    
    @Override
    public ASTNode visitAlterDatabaseLink(final AlterDatabaseLinkContext ctx) {
        return new OracleAlterDatabaseLinkStatement();
    }
    
    @Override
    public ASTNode visitAlterDatabaseDictionary(final AlterDatabaseDictionaryContext ctx) {
        return new OracleAlterDatabaseDictionaryStatement();
    }
    
    @Override
    public ASTNode visitAlterView(final AlterViewContext ctx) {
        OracleAlterViewStatement result = new OracleAlterViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setConstraintDefinitionSegment((ConstraintDefinitionSegment) getAlterViewConstraintDefinition(ctx));
        return result;
    }
    
    private ASTNode getAlterViewConstraintDefinition(final AlterViewContext ctx) {
        ConstraintDefinitionSegment result = null;
        if (null != ctx.outOfLineConstraint()) {
            result = (ConstraintDefinitionSegment) visit(ctx.outOfLineConstraint());
        } else if (null != ctx.constraintName()) {
            result = new ConstraintDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
            result.setConstraintName((ConstraintSegment) visit(ctx.constraintName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropPackage(final DropPackageContext ctx) {
        return new OracleDropPackageStatement();
    }
    
    @Override
    public ASTNode visitAlterPackage(final AlterPackageContext ctx) {
        return new OracleAlterPackageStatement();
    }
    
    @Override
    public ASTNode visitCreateSynonym(final CreateSynonymContext ctx) {
        return new OracleCreateSynonymStatement();
    }
    
    @Override
    public ASTNode visitDropSynonym(final DropSynonymContext ctx) {
        return new OracleDropSynonymStatement();
    }
    
    @Override
    public ASTNode visitCreateDirectory(final CreateDirectoryContext ctx) {
        return new OracleCreateDirectoryStatement();
    }
    
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        OracleDropViewStatement result = new OracleDropViewStatement();
        result.getViews().add((SimpleTableSegment) visit(ctx.viewName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateEdition(final CreateEditionContext ctx) {
        return new OracleCreateEditionStatement();
    }
    
    @Override
    public ASTNode visitDropTrigger(final DropTriggerContext ctx) {
        return new OracleDropTriggerStatement();
    }
    
    @Override
    public ASTNode visitCreateTrigger(final CreateTriggerContext ctx) {
        return new OracleCreateTriggerStatement();
    }
    
    @Override
    public ASTNode visitAlterTrigger(final AlterTriggerContext ctx) {
        return new OracleAlterTriggerStatement();
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        OracleTruncateStatement result = new OracleTruncateStatement();
        result.getTables().add((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        OracleCreateIndexStatement result = new OracleCreateIndexStatement();
        if (null != ctx.createIndexDefinitionClause().tableIndexClause()) {
            result.setTable((SimpleTableSegment) visit(ctx.createIndexDefinitionClause().tableIndexClause().tableName()));
            result.getColumns().addAll(((CollectionValue) visit(ctx.createIndexDefinitionClause().tableIndexClause().indexExpressions())).getValue());
        }
        result.setIndex((IndexSegment) visit(ctx.indexName()));
        return result;
    }
    
    @Override
    public ASTNode visitIndexExpressions(final IndexExpressionsContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (IndexExpressionContext each : ctx.indexExpression()) {
            ASTNode astNode = visit(each);
            if (astNode instanceof ColumnSegment) {
                result.getValue().add((ColumnSegment) astNode);
            }
            if (astNode instanceof FunctionSegment) {
                ((FunctionSegment) astNode).getParameters().forEach(parameter -> {
                    if (parameter instanceof ColumnSegment) {
                        result.getValue().add((ColumnSegment) parameter);
                    }
                });
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitIndexExpression(final IndexExpressionContext ctx) {
        return null == ctx.expr() ? visit(ctx.columnName()) : visit(ctx.expr());
    }
    
    @Override
    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
        OracleAlterIndexStatement result = new OracleAlterIndexStatement();
        result.setIndex((IndexSegment) visit(ctx.indexName()));
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        OracleDropIndexStatement result = new OracleDropIndexStatement();
        result.getIndexes().add((IndexSegment) visit(ctx.indexName()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterSynonym(final AlterSynonymContext ctx) {
        return new OracleAlterSynonymStatement();
    }
    
    @Override
    public ASTNode visitAlterSession(final AlterSessionContext ctx) {
        return new OracleAlterSessionStatement();
    }
    
    @Override
    public ASTNode visitAlterDatabase(final AlterDatabaseContext ctx) {
        return new OracleAlterDatabaseStatement();
    }
    
    @Override
    public ASTNode visitAlterSystem(final AlterSystemContext ctx) {
        return new OracleAlterSystemStatement();
    }
    
    @Override
    public ASTNode visitAnalyze(final AnalyzeContext ctx) {
        OracleAnalyzeStatement result = new OracleAnalyzeStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAssociateStatistics(final AssociateStatisticsContext ctx) {
        OracleAssociateStatisticsStatement result = new OracleAssociateStatisticsStatement();
        if (null != ctx.columnAssociation()) {
            for (TableNameContext each : ctx.columnAssociation().tableName()) {
                result.getTables().add((SimpleTableSegment) visit(each));
            }
            for (ColumnNameContext each : ctx.columnAssociation().columnName()) {
                result.getColumns().add((ColumnSegment) visit(each));
            }
        }
        if (null != ctx.functionAssociation()) {
            for (IndexNameContext each : ctx.functionAssociation().indexName()) {
                result.getIndexes().add((IndexSegment) visit(each));
            }
            for (FunctionContext each : ctx.functionAssociation().function()) {
                result.getFunctions().add((FunctionSegment) visit(each));
            }
            for (PackageNameContext each : ctx.functionAssociation().packageName()) {
                result.getPackages().add((PackageSegment) visit(each));
            }
            for (TypeNameContext each : ctx.functionAssociation().typeName()) {
                result.getTypes().add((TypeSegment) visit(each));
            }
            for (IndexTypeNameContext each : ctx.functionAssociation().indexTypeName()) {
                result.getIndexTypes().add((IndexTypeSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitDisassociateStatistics(final DisassociateStatisticsContext ctx) {
        OracleDisassociateStatisticsStatement result = new OracleDisassociateStatisticsStatement();
        if (null != ctx.tableName()) {
            for (TableNameContext each : ctx.tableName()) {
                result.getTables().add((SimpleTableSegment) visit(each));
            }
            for (ColumnNameContext each : ctx.columnName()) {
                result.getColumns().add((ColumnSegment) visit(each));
            }
        }
        if (null != ctx.indexName()) {
            for (IndexNameContext each : ctx.indexName()) {
                result.getIndexes().add((IndexSegment) visit(each));
            }
        }
        if (null != ctx.function()) {
            for (FunctionContext each : ctx.function()) {
                result.getFunctions().add((FunctionSegment) visit(each));
            }
        }
        if (null != ctx.packageName()) {
            for (PackageNameContext each : ctx.packageName()) {
                result.getPackages().add((PackageSegment) visit(each));
            }
        }
        if (null != ctx.typeName()) {
            for (TypeNameContext each : ctx.typeName()) {
                result.getTypes().add((TypeSegment) visit(each));
            }
        }
        if (null != ctx.indexTypeName()) {
            for (IndexTypeNameContext each : ctx.indexTypeName()) {
                result.getIndexTypes().add((IndexTypeSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAudit(final AuditContext ctx) {
        return null == ctx.auditTraditional() ? visit(ctx.auditUnified()) : visit(ctx.auditTraditional());
    }
    
    @Override
    public ASTNode visitAuditTraditional(final AuditTraditionalContext ctx) {
        return new OracleAuditStatement();
    }
    
    @Override
    public ASTNode visitAuditUnified(final AuditUnifiedContext ctx) {
        return new OracleAuditStatement();
    }
    
    @Override
    public ASTNode visitNoAudit(final NoAuditContext ctx) {
        return new OracleNoAuditStatement();
    }
    
    @Override
    public ASTNode visitComment(final CommentContext ctx) {
        OracleCommentStatement result = new OracleCommentStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.columnName()) {
            result.setColumn((ColumnSegment) visit(ctx.columnName()));
        }
        if (null != ctx.indexTypeName()) {
            result.setIndexType((IndexTypeSegment) visit(ctx.indexTypeName()));
        }
        result.setComment(new IdentifierValue(ctx.STRING_().getText()));
        return result;
    }
    
    @Override
    public ASTNode visitFlashbackDatabase(final FlashbackDatabaseContext ctx) {
        return new OracleFlashbackDatabaseStatement();
    }
    
    @Override
    public ASTNode visitFlashbackTable(final FlashbackTableContext ctx) {
        OracleFlashbackTableStatement result = new OracleFlashbackTableStatement();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        
        if (null != ctx.renameToTable()) {
            result.setRenameTable((SimpleTableSegment) visit(ctx.renameToTable().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitPurge(final PurgeContext ctx) {
        OraclePurgeStatement result = new OraclePurgeStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.indexName()) {
            result.setIndex((IndexSegment) visit(ctx.indexName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRename(final RenameContext ctx) {
        return new OracleRenameStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabase(final CreateDatabaseContext ctx) {
        return new OracleCreateDatabaseStatement();
    }
    
    @Override
    public ASTNode visitCreateDatabaseLink(final CreateDatabaseLinkContext ctx) {
        return new OracleCreateDatabaseLinkStatement();
    }
    
    @Override
    public ASTNode visitCreateDimension(final CreateDimensionContext ctx) {
        return new OracleCreateDimensionStatement();
    }
    
    @Override
    public ASTNode visitAlterDimension(final AlterDimensionContext ctx) {
        return new OracleAlterDimensionStatement();
    }
    
    @Override
    public ASTNode visitDropDimension(final DropDimensionContext ctx) {
        return new OracleDropDimensionStatement();
    }
    
    @Override
    public ASTNode visitDropDirectory(final DropDirectoryContext ctx) {
        return new OracleDropDirectoryStatement();
    }
    
    @Override
    public ASTNode visitCreateFunction(final CreateFunctionContext ctx) {
        return visitCreateFunction0(ctx);
    }
    
    private ASTNode visitCreateFunction0(final CreateFunctionContext ctx) {
        if (null != ctx.plsqlFunctionSource().declareSection()) {
            visit(ctx.plsqlFunctionSource().declareSection());
        }
        if (null != ctx.plsqlFunctionSource().body()) {
            visit(ctx.plsqlFunctionSource().body());
        }
        getSqlStatementsInPlsql().sort(Comparator.comparingInt(SQLStatementSegment::getStartIndex));
        getProcedureCallNames().sort(Comparator.comparingInt(ProcedureCallNameSegment::getStartIndex));
        getDynamicSqlStatementExpressions().sort(Comparator.comparingInt(ExpressionSegment::getStartIndex));
        OracleCreateFunctionStatement result = new OracleCreateFunctionStatement(getSqlStatementsInPlsql(), getProcedureCallNames(), getDynamicSqlStatementExpressions());
        result.setFunctionName(visitFunctionName(ctx.plsqlFunctionSource()));
        return result;
    }
    
    private FunctionNameSegment visitFunctionName(final PlsqlFunctionSourceContext ctx) {
        OwnerContext schema = ctx.function().owner();
        IdentifierValue functionName = (IdentifierValue) visit(ctx.function().name().identifier());
        if (null == schema) {
            return new FunctionNameSegment(ctx.function().name().start.getStartIndex(), ctx.function().name().stop.getStopIndex(), functionName);
        }
        OwnerSegment owner = new OwnerSegment(schema.start.getStartIndex(), schema.stop.getStopIndex(), (IdentifierValue) visit(schema.identifier()));
        FunctionNameSegment result = new FunctionNameSegment(ctx.function().start.getStartIndex(), ctx.function().stop.getStopIndex(), functionName);
        result.setOwner(owner);
        return result;
    }
    
    @Override
    public ASTNode visitDropEdition(final DropEditionContext ctx) {
        return new OracleDropEditionStatement();
    }
    
    @Override
    public ASTNode visitDropOutline(final DropOutlineContext ctx) {
        return new OracleDropOutlineStatement();
    }
    
    @Override
    public ASTNode visitAlterOutline(final AlterOutlineContext ctx) {
        return new OracleAlterOutlineStatement();
    }
    
    @Override
    public ASTNode visitAlterAnalyticView(final AlterAnalyticViewContext ctx) {
        return new OracleAlterAnalyticViewStatement();
    }
    
    @Override
    public ASTNode visitAlterAttributeDimension(final AlterAttributeDimensionContext ctx) {
        return new OracleAlterAttributeDimensionStatement();
    }
    
    @Override
    public ASTNode visitCreateSequence(final CreateSequenceContext ctx) {
        OracleCreateSequenceStatement result = new OracleCreateSequenceStatement();
        result.setSequenceName(ctx.sequenceName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitAlterSequence(final AlterSequenceContext ctx) {
        return new OracleAlterSequenceStatement();
    }
    
    @Override
    public ASTNode visitCreateContext(final CreateContextContext ctx) {
        return new OracleCreateContextStatement();
    }
    
    @Override
    public ASTNode visitCreateSPFile(final CreateSPFileContext ctx) {
        return new OracleCreateSPFileStatement();
    }
    
    @Override
    public ASTNode visitCreatePFile(final CreatePFileContext ctx) {
        return new OracleCreatePFileStatement();
    }
    
    @Override
    public ASTNode visitCreateControlFile(final CreateControlFileContext ctx) {
        return new OracleCreateControlFileStatement();
    }
    
    @Override
    public ASTNode visitCreateFlashbackArchive(final CreateFlashbackArchiveContext ctx) {
        return new OracleCreateFlashbackArchiveStatement();
    }
    
    @Override
    public ASTNode visitAlterFlashbackArchive(final AlterFlashbackArchiveContext ctx) {
        return new OracleAlterFlashbackArchiveStatement();
    }
    
    @Override
    public ASTNode visitDropFlashbackArchive(final DropFlashbackArchiveContext ctx) {
        return new OracleDropFlashbackArchiveStatement();
    }
    
    @Override
    public ASTNode visitCreateDiskgroup(final CreateDiskgroupContext ctx) {
        return new OracleCreateDiskgroupStatement();
    }
    
    @Override
    public ASTNode visitDropDiskgroup(final DropDiskgroupContext ctx) {
        return new OracleDropDiskgroupStatement();
    }
    
    @Override
    public ASTNode visitCreateRollbackSegment(final CreateRollbackSegmentContext ctx) {
        return new OracleCreateRollbackSegmentStatement();
    }
    
    @Override
    public ASTNode visitDropRollbackSegment(final DropRollbackSegmentContext ctx) {
        return new OracleDropRollbackSegmentStatement();
    }
    
    @Override
    public ASTNode visitDropTableSpace(final DropTableSpaceContext ctx) {
        return new OracleDropTableSpaceStatement();
    }
    
    @Override
    public ASTNode visitCreateLockdownProfile(final CreateLockdownProfileContext ctx) {
        return new OracleCreateLockdownProfileStatement();
    }
    
    @Override
    public ASTNode visitDropLockdownProfile(final DropLockdownProfileContext ctx) {
        return new OracleDropLockdownProfileStatement();
    }
    
    @Override
    public ASTNode visitCreateInmemoryJoinGroup(final CreateInmemoryJoinGroupContext ctx) {
        return new OracleCreateInmemoryJoinGroupStatement();
    }
    
    @Override
    public ASTNode visitAlterInmemoryJoinGroup(final AlterInmemoryJoinGroupContext ctx) {
        return new OracleAlterInmemoryJoinGroupStatement();
    }
    
    @Override
    public ASTNode visitDropInmemoryJoinGroup(final DropInmemoryJoinGroupContext ctx) {
        return new OracleDropInmemoryJoinGroupStatement();
    }
    
    @Override
    public ASTNode visitCreateRestorePoint(final CreateRestorePointContext ctx) {
        return new OracleCreateRestorePointStatement();
    }
    
    @Override
    public ASTNode visitDropRestorePoint(final DropRestorePointContext ctx) {
        return new OracleDropRestorePointStatement();
    }
    
    @Override
    public ASTNode visitAlterOperator(final AlterOperatorContext ctx) {
        return new OracleAlterOperatorStatement();
    }
    
    @Override
    public ASTNode visitAlterProfile(final AlterProfileContext ctx) {
        return new OracleAlterProfileStatement();
    }
    
    @Override
    public ASTNode visitAlterRollbackSegment(final AlterRollbackSegmentContext ctx) {
        return new OracleAlterRollbackSegmentStatement();
    }
    
    @Override
    public ASTNode visitDropOperator(final DropOperatorContext ctx) {
        return new OracleDropOperatorStatement();
    }
    
    @Override
    public ASTNode visitDropSequence(final DropSequenceContext ctx) {
        return new OracleDropSequenceStatement();
    }
    
    @Override
    public ASTNode visitAlterLibrary(final AlterLibraryContext ctx) {
        return new OracleAlterLibraryStatement();
    }
    
    @Override
    public ASTNode visitDropType(final DropTypeContext ctx) {
        return new OracleDropTypeStatement();
    }
    
    @Override
    public ASTNode visitAlterMaterializedZonemap(final AlterMaterializedZonemapContext ctx) {
        return new OracleAlterMaterializedZonemapStatement();
    }
    
    @Override
    public ASTNode visitAlterJava(final AlterJavaContext ctx) {
        return new OracleAlterJavaStatement();
    }
    
    @Override
    public ASTNode visitAlterAuditPolicy(final AlterAuditPolicyContext ctx) {
        return new OracleAlterAuditPolicyStatement();
    }
    
    @Override
    public ASTNode visitAlterCluster(final AlterClusterContext ctx) {
        return new OracleAlterClusterStatement();
    }
    
    @Override
    public ASTNode visitAlterDiskgroup(final AlterDiskgroupContext ctx) {
        return new OracleAlterDiskgroupStatement();
    }
    
    @Override
    public ASTNode visitAlterIndexType(final AlterIndexTypeContext ctx) {
        return new OracleAlterIndexTypeStatement();
    }
    
    @Override
    public ASTNode visitAlterMaterializedView(final AlterMaterializedViewContext ctx) {
        return new OracleAlterMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitAlterMaterializedViewLog(final AlterMaterializedViewLogContext ctx) {
        return new OracleAlterMaterializedViewLogStatement();
    }
    
    @Override
    public ASTNode visitAlterFunction(final AlterFunctionContext ctx) {
        return new OracleAlterFunctionStatement();
    }
    
    @Override
    public ASTNode visitAlterHierarchy(final AlterHierarchyContext ctx) {
        return new OracleAlterHierarchyStatement();
    }
    
    @Override
    public ASTNode visitAlterLockdownProfile(final AlterLockdownProfileContext ctx) {
        return new OracleAlterLockdownProfileStatement();
    }
    
    @Override
    public ASTNode visitAlterPluggableDatabase(final AlterPluggableDatabaseContext ctx) {
        return new OracleAlterPluggableDatabaseStatement();
    }
    
    @Override
    public ASTNode visitCreateProcedure(final CreateProcedureContext ctx) {
        return visitCreateProcedure0(ctx);
    }
    
    private ASTNode visitCreateProcedure0(final CreateProcedureContext ctx) {
        if (null != ctx.plsqlProcedureSource().parameterDeclaration()) {
            for (ParameterDeclarationContext each : ctx.plsqlProcedureSource().parameterDeclaration()) {
                visit(each);
            }
        }
        if (null != ctx.plsqlProcedureSource().declareSection()) {
            visit(ctx.plsqlProcedureSource().declareSection());
        }
        if (null != ctx.plsqlProcedureSource().body()) {
            visit(ctx.plsqlProcedureSource().body());
        }
        getSqlStatementsInPlsql().sort(Comparator.comparingInt(SQLStatementSegment::getStartIndex));
        getProcedureCallNames().sort(Comparator.comparingInt(ProcedureCallNameSegment::getStartIndex));
        getDynamicSqlStatementExpressions().sort(Comparator.comparingInt(ExpressionSegment::getStartIndex));
        OracleCreateProcedureStatement result = new OracleCreateProcedureStatement(getSqlStatementsInPlsql(), getProcedureCallNames(), getProcedureBodyEndNameSegments(),
                getDynamicSqlStatementExpressions());
        result.setProcedureName(visitProcedureName(ctx.plsqlProcedureSource()));
        result.getVariableNames().addAll(getVariableNames());
        getSqlStatementsInPlsql().forEach(each -> each.getSqlStatement().getVariableNames().addAll(getVariableNames()));
        result.getCursorForLoopStatements().addAll(getCursorForLoopStatementSegments());
        return result;
    }
    
    @Override
    public ASTNode visitParameterDeclaration(final ParameterDeclarationContext ctx) {
        if (null != ctx.parameterName()) {
            IdentifierValue paramName = (IdentifierValue) visit(ctx.parameterName().identifier());
            getVariableNames().add(paramName.getValue().toLowerCase());
            return new VariableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), paramName.getValue());
        }
        return super.visitParameterDeclaration(ctx);
    }
    
    @Override
    public ASTNode visitItemDeclaration(final ItemDeclarationContext ctx) {
        CollectionValue<VariableSegment> result = new CollectionValue<>();
        if (null != ctx.collectionVariableDecl() && null != ctx.collectionVariableDecl().variableName()) {
            for (VariableNameContext each : ctx.collectionVariableDecl().variableName()) {
                getVariableSegment(each).ifPresent(optional -> result.getValue().add(optional));
            }
        }
        if (null != ctx.constantDeclaration() && null != ctx.constantDeclaration().variableName()) {
            getVariableSegment(ctx.constantDeclaration().variableName()).ifPresent(optional -> result.getValue().add(optional));
        }
        if (null != ctx.cursorVariableDeclaration() && null != ctx.cursorVariableDeclaration().variableName()) {
            getVariableSegment(ctx.cursorVariableDeclaration().variableName()).ifPresent(optional -> result.getValue().add(optional));
        }
        if (null != ctx.exceptionDeclaration() && null != ctx.exceptionDeclaration().variableName()) {
            getVariableSegment(ctx.exceptionDeclaration().variableName()).ifPresent(optional -> result.getValue().add(optional));
        }
        if (null != ctx.recordVariableDeclaration() && null != ctx.recordVariableDeclaration().variableName()) {
            getVariableSegment(ctx.recordVariableDeclaration().variableName()).ifPresent(optional -> result.getValue().add(optional));
        }
        if (null != ctx.variableDeclaration() && null != ctx.variableDeclaration().variableName()) {
            getVariableSegment(ctx.variableDeclaration().variableName()).ifPresent(optional -> result.getValue().add(optional));
        }
        return result;
    }
    
    private Optional<VariableSegment> getVariableSegment(final VariableNameContext variableNameContext) {
        if (null == variableNameContext) {
            return Optional.empty();
        }
        if (null != variableNameContext.identifier()) {
            String variableName = ((IdentifierValue) visitIdentifier(variableNameContext.identifier())).getValue().toLowerCase();
            getVariableNames().add(variableName);
            return Optional.of(new VariableSegment(variableNameContext.start.getStartIndex(), variableNameContext.stop.getStopIndex(), variableName));
        }
        if (null != variableNameContext.stringLiterals()) {
            String variableName = variableNameContext.stringLiterals().STRING_().getText().toLowerCase();
            getVariableNames().add(variableName);
            return Optional.of(new VariableSegment(variableNameContext.start.getStartIndex(), variableNameContext.stop.getStopIndex(), variableName));
        }
        return Optional.empty();
    }
    
    @Override
    public ASTNode visitCollectionVariableDecl(final CollectionVariableDeclContext ctx) {
        if (null == ctx.variableName()) {
            return super.visitCollectionVariableDecl(ctx);
        }
        CollectionValue<VariableSegment> result = new CollectionValue<>();
        for (VariableNameContext each : ctx.variableName()) {
            getVariableSegment(each).ifPresent(optional -> result.getValue().add(optional));
        }
        return result;
    }
    
    private FunctionNameSegment visitProcedureName(final PlsqlProcedureSourceContext ctx) {
        SchemaNameContext schemaName = ctx.schemaName();
        IdentifierValue procedureName = (IdentifierValue) visit(ctx.procedureName().identifier());
        if (null == schemaName) {
            return new FunctionNameSegment(ctx.procedureName().start.getStartIndex(), ctx.procedureName().stop.getStopIndex(), procedureName);
        }
        OwnerSegment owner = new OwnerSegment(schemaName.start.getStartIndex(), schemaName.stop.getStopIndex(), (IdentifierValue) visit(schemaName.identifier()));
        FunctionNameSegment result = new FunctionNameSegment(schemaName.start.getStartIndex(), ctx.procedureName().stop.getStopIndex(), procedureName);
        result.setOwner(owner);
        return result;
    }
    
    @Override
    public ASTNode visitCursorDefinition(final CursorDefinitionContext ctx) {
        SQLStatement statement = visitSelect0(ctx.select());
        getCursorStatements().put(null != ctx.variableName().identifier() ? new IdentifierValue(ctx.variableName().getText()).getValue()
                : new StringLiteralValue(ctx.variableName().getText()).getValue(), statement);
        return defaultResult();
    }
    
    @Override
    public ASTNode visitBody(final BodyContext ctx) {
        for (StatementContext each : ctx.statement()) {
            visit(each);
        }
        for (ExceptionHandlerContext eachExceptionHandler : ctx.exceptionHandler()) {
            for (StatementContext each : eachExceptionHandler.statement()) {
                visit(each);
            }
        }
        if (null != ctx.identifier()) {
            getProcedureBodyEndNameSegments().add(
                    new ProcedureBodyEndNameSegment(ctx.identifier().getStart().getStartIndex(), ctx.identifier().getStop().getStopIndex(), new IdentifierValue(ctx.identifier().getText())));
        }
        return defaultResult();
    }
    
    @Override
    public ASTNode visitProcedureCall(final ProcedureCallContext ctx) {
        int startIndex = ctx.procedureName().start.getStartIndex();
        PackageSegment packageSegment = null;
        if (null != ctx.packageName()) {
            startIndex = ctx.packageName().start.getStartIndex();
            packageSegment = (PackageSegment) visit(ctx.packageName());
        }
        ProcedureCallNameSegment result = new ProcedureCallNameSegment(startIndex, ctx.procedureName().stop.getStopIndex(), (IdentifierValue) visit(ctx.procedureName().identifier()));
        result.setPackageSegment(packageSegment);
        getProcedureCallNames().add(result);
        return defaultResult();
    }
    
    @Override
    public ASTNode visitCursorForLoopStatement(final CursorForLoopStatementContext ctx) {
        SQLStatement relatedCursorStatement;
        String cursorName = null;
        if (null != ctx.select()) {
            relatedCursorStatement = visitSelect0(ctx.select());
        } else {
            cursorName = null == ctx.cursor().variableName().identifier()
                    ? new StringLiteralValue(ctx.cursor().getText()).getValue()
                    : new IdentifierValue(ctx.cursor().getText()).getValue();
            relatedCursorStatement = getCursorStatements().get(cursorName);
        }
        increaseCursorForLoopLevel();
        for (StatementContext each : ctx.statement()) {
            visit(each);
        }
        Set<SQLStatement> sqlStatements = getTempCursorForLoopStatements().remove(getCursorForLoopLevel());
        CursorForLoopStatementSegment cursorForLoopStatementSegment = new CursorForLoopStatementSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                new IdentifierValue(ctx.record().getText()).getValue(), cursorName, relatedCursorStatement, null == sqlStatements ? Collections.emptyList() : sqlStatements);
        getCursorForLoopStatementSegments().add(cursorForLoopStatementSegment);
        decreaseCursorForLoopLevel();
        return defaultResult();
    }
    
    @Override
    public ASTNode visitOpenForStatement(final OpenForStatementContext ctx) {
        if (null != ctx.select()) {
            visitSelect0(ctx.select());
        }
        // TODO handle SQL in dynamicString
        return defaultResult();
    }
    
    private SQLStatement visitSelect0(final SelectContext select) {
        OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
        SQLStatement result = (SQLStatement) visitor.visitSelect(select);
        getSqlStatementsInPlsql().add(new SQLStatementSegment(select.start.getStartIndex(), select.stop.getStopIndex(), result));
        addToTempCursorForLoopStatements(result);
        return result;
    }
    
    private void addToTempCursorForLoopStatements(final SQLStatement sqlStatement) {
        if (0 == getCursorForLoopLevel()) {
            return;
        }
        for (int i = 1; i <= getCursorForLoopLevel(); i++) {
            getTempCursorForLoopStatements().computeIfAbsent(i, key -> new LinkedHashSet<>()).add(sqlStatement);
        }
    }
    
    @Override
    public ASTNode visitSqlStatementInPlsql(final SqlStatementInPlsqlContext ctx) {
        if (null != ctx.commit()) {
            OracleStatementVisitor visitor = createOracleTCLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitCommit(ctx.commit());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.commit().start.getStartIndex(), ctx.commit().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        // TODO visit collection_method_call
        if (null != ctx.delete()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitDelete(ctx.delete());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.delete().start.getStartIndex(), ctx.delete().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.insert()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitInsert(ctx.insert());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.insert().start.getStartIndex(), ctx.insert().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.lockTable()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitLockTable(ctx.lockTable());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.lockTable().start.getStartIndex(), ctx.lockTable().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.merge()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitMerge(ctx.merge());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.merge().start.getStartIndex(), ctx.merge().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.rollback()) {
            OracleStatementVisitor visitor = createOracleTCLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitRollback(ctx.rollback());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.rollback().start.getStartIndex(), ctx.rollback().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.savepoint()) {
            OracleStatementVisitor visitor = createOracleTCLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitSavepoint(ctx.savepoint());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.savepoint().start.getStartIndex(), ctx.savepoint().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.setTransaction()) {
            OracleStatementVisitor visitor = createOracleTCLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitSetTransaction(ctx.setTransaction());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.setTransaction().start.getStartIndex(), ctx.setTransaction().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.update()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitUpdate(ctx.update());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.update().start.getStartIndex(), ctx.update().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        return defaultResult();
    }
    
    private OracleStatementVisitor createOracleTCLStatementVisitor() {
        OracleStatementVisitor result = new OracleTCLStatementVisitor();
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    private OracleStatementVisitor createOracleDMLStatementVisitor() {
        OracleStatementVisitor result = new OracleDMLStatementVisitor();
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    @Override
    public ASTNode visitDmlStatement(final DmlStatementContext ctx) {
        if (null != ctx.insert()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitInsert(ctx.insert());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.insert().start.getStartIndex(), ctx.insert().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.update()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitUpdate(ctx.update());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.update().start.getStartIndex(), ctx.update().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.delete()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitDelete(ctx.delete());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.delete().start.getStartIndex(), ctx.delete().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        if (null != ctx.merge()) {
            OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
            SQLStatement result = (SQLStatement) visitor.visitMerge(ctx.merge());
            getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.merge().start.getStartIndex(), ctx.merge().stop.getStopIndex(), result));
            addToTempCursorForLoopStatements(result);
        }
        // TODO Handling dynamicSqlStmt if we can
        return defaultResult();
    }
    
    @Override
    public ASTNode visitSelectIntoStatement(final SelectIntoStatementContext ctx) {
        // TODO Visit intoClause
        OracleStatementVisitor visitor = createOracleDMLStatementVisitor();
        OracleSelectStatement result = (OracleSelectStatement) visitor.visitSelectIntoStatement(ctx);
        getSqlStatementsInPlsql().add(new SQLStatementSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), result));
        addToTempCursorForLoopStatements(result);
        return result;
    }
    
    @Override
    public ASTNode visitDynamicSqlStmt(final DynamicSqlStmtContext ctx) {
        ExpressionSegment result = (ExpressionSegment) visit(ctx.expression().expr());
        getDynamicSqlStatementExpressions().add(result);
        return result;
    }
    
    @Override
    public ASTNode visitPlsqlBlock(final PlsqlBlockContext ctx) {
        return new OraclePLSQLBlockStatement();
    }
    
    @Override
    public ASTNode visitAlterProcedure(final AlterProcedureContext ctx) {
        return new OracleAlterProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropProcedure(final DropProcedureContext ctx) {
        return new OracleDropProcedureStatement();
    }
    
    @Override
    public ASTNode visitDropIndexType(final DropIndexTypeContext ctx) {
        return new OracleDropIndexTypeStatement();
    }
    
    @Override
    public ASTNode visitDropProfile(final DropProfileContext ctx) {
        return new OracleDropProfileStatement();
    }
    
    @Override
    public ASTNode visitDropPluggableDatabase(final DropPluggableDatabaseContext ctx) {
        return new OracleDropPluggableDatabaseStatement();
    }
    
    @Override
    public ASTNode visitDropJava(final DropJavaContext ctx) {
        return new OracleDropJavaStatement();
    }
    
    @Override
    public ASTNode visitDropFunction(final DropFunctionContext ctx) {
        return new OracleDropFunctionStatement();
    }
    
    @Override
    public ASTNode visitDropLibrary(final DropLibraryContext ctx) {
        return new OracleDropLibraryStatement();
    }
    
    @Override
    public ASTNode visitDropCluster(final DropClusterContext ctx) {
        return new OracleDropClusterStatement();
    }
    
    @Override
    public ASTNode visitDropMaterializedView(final DropMaterializedViewContext ctx) {
        return new OracleDropMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitDropMaterializedViewLog(final DropMaterializedViewLogContext ctx) {
        return new OracleDropMaterializedViewLogStatement();
    }
    
    @Override
    public ASTNode visitDropMaterializedZonemap(final DropMaterializedZonemapContext ctx) {
        return new OracleDropMaterializedZonemapStatement();
    }
    
    @Override
    public ASTNode visitCreateTablespace(final CreateTablespaceContext ctx) {
        return new OracleCreateTablespaceStatement();
    }
    
    @Override
    public ASTNode visitCreateMaterializedView(final CreateMaterializedViewContext ctx) {
        return new OracleCreateMaterializedViewStatement();
    }
    
    @Override
    public ASTNode visitCreateMaterializedViewLog(final CreateMaterializedViewLogContext ctx) {
        return new OracleCreateMaterializedViewLogStatement();
    }
    
    @Override
    public ASTNode visitCreateCluster(final CreateClusterContext ctx) {
        return new OracleCreateClusterStatement();
    }
    
    @Override
    public ASTNode visitSystemAction(final SystemActionContext ctx) {
        return new OracleSystemActionStatement();
    }
    
    @Override
    public ASTNode visitAlterType(final AlterTypeContext ctx) {
        return new OracleAlterTypeStatement();
    }
    
    @Override
    public ASTNode visitCreateJava(final CreateJavaContext ctx) {
        return new OracleCreateJavaStatement();
    }
    
    @Override
    public ASTNode visitCreateLibrary(final CreateLibraryContext ctx) {
        return new OracleCreateLibraryStatement();
    }
    
    @Override
    public ASTNode visitSwitch(final SwitchContext ctx) {
        return new OracleSwitchStatement();
    }
    
    @Override
    public ASTNode visitCreateProfile(final CreateProfileContext ctx) {
        return new OracleCreateProfileStatement();
    }
    
    @Override
    public ASTNode visitDropDatabase(final DropDatabaseContext ctx) {
        return new OracleDropDatabaseStatement();
    }
    
    @Override
    public ASTNode visitCreateOperator(final CreateOperatorContext ctx) {
        return new OracleCreateOperatorStatement();
    }
}
