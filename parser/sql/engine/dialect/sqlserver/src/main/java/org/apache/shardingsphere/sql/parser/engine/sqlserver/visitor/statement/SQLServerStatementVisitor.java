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

package org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AggregationClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AiFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AiGenerateEmbeddingsFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ApproxFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ChangeTableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNameWithSortContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNamesWithSortContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ConversionFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ConvertFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CreateTableAsSelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CteClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CteClauseSetContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CurrentUserFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DelimitedIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DuplicateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExecContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExpressionListContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.FreetextTableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GraphAggFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GraphFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GroupByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GroupByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GroupingExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GroupingSetsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.HavingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertDefaultValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertExecClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertSelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.IntoClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JoinSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JsonArrayFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JsonFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JsonKeyValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JsonNullClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JsonObjectFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.LagLeadFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.LinkedServerNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MergeContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MergeInsertClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MergeUpdateClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MergeWhenClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MultipleTableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MultipleTablesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OpenDatasourceFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OpenJsonFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OpenQueryFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OpenRowSetFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OptionHintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputWithColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputWithColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ParseFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.PivotClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.PivotTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.PivotValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.PivotValueListContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.PredictFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProcedureNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.QualifiedShorthandContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RegularIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RollupCubeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.RowSetFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SampleOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ScalarExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ScriptVariableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SingleTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.StatisticsOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.StatisticsOptionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.StatisticsWithClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableHintExtendedContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableHintLimitedContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TopContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TrimFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TryParseFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UpdateStatisticsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.VariableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.VectorSearchFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.VectorSearchParametersContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ViewNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WithClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WithTableHintContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.XmlMethodCallContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ScanUnit;
import org.apache.shardingsphere.sql.parser.statement.core.enums.StatisticsDimension;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.exec.ExecSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.KeyValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.TableHintLimitedSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.json.JsonNullClauseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.sample.SampleOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.sample.SampleStrategy;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.statistics.StatisticsOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.statistics.StatisticsStrategySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.statistics.SQLServerUpdateStatisticsStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statement visitor for SQLServer.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class SQLServerStatementVisitor extends SQLServerStatementBaseVisitor<ASTNode> {
    
    private final DatabaseType databaseType;
    
    private final Collection<ParameterMarkerSegment> parameterMarkerSegments = new LinkedList<>();
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(parameterMarkerSegments.size(), ParameterMarkerType.QUESTION);
    }
    
    @Override
    public final ASTNode visitLiterals(final LiteralsContext ctx) {
        if (null != ctx.stringLiterals()) {
            return visit(ctx.stringLiterals());
        }
        if (null != ctx.numberLiterals()) {
            return visit(ctx.numberLiterals());
        }
        if (null != ctx.hexadecimalLiterals()) {
            return visit(ctx.hexadecimalLiterals());
        }
        if (null != ctx.bitValueLiterals()) {
            return visit(ctx.bitValueLiterals());
        }
        if (null != ctx.booleanLiterals()) {
            return visit(ctx.booleanLiterals());
        }
        if (null != ctx.nullValueLiterals()) {
            return visit(ctx.nullValueLiterals());
        }
        throw new IllegalStateException("Literals must have string, number, dateTime, hex, bit, boolean or null.");
    }
    
    @Override
    public final ASTNode visitStringLiterals(final StringLiteralsContext ctx) {
        if (null != ctx.STRING_()) {
            return new StringLiteralValue(ctx.getText());
        } else {
            return new StringLiteralValue(ctx.getText().substring(1));
        }
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitHexadecimalLiterals(final HexadecimalLiteralsContext ctx) {
        // TODO deal with hexadecimalLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBitValueLiterals(final BitValueLiteralsContext ctx) {
        // TODO deal with bitValueLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBooleanLiterals(final BooleanLiteralsContext ctx) {
        return new BooleanLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNullValueLiterals(final NullValueLiteralsContext ctx) {
        return new NullLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitIdentifier(final IdentifierContext ctx) {
        return null == ctx.regularIdentifier() ? visit(ctx.delimitedIdentifier()) : visit(ctx.regularIdentifier());
    }
    
    @Override
    public final ASTNode visitVariableName(final VariableNameContext ctx) {
        return new VariableSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((IdentifierValue) visit(ctx.identifier())).getValue());
    }
    
    @Override
    public final ASTNode visitRegularIdentifier(final RegularIdentifierContext ctx) {
        UnreservedWordContext unreservedWord = ctx.unreservedWord();
        return null == unreservedWord ? new IdentifierValue(ctx.getText()) : (IdentifierValue) visit(unreservedWord);
    }
    
    @Override
    public final ASTNode visitDelimitedIdentifier(final DelimitedIdentifierContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitUnreservedWord(final UnreservedWordContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(), ctx.name().getStop().getStopIndex(), (IdentifierValue) visit(ctx.name())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            OwnerSegment ownerSegment = new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier()));
            if (null != ctx.databaseName()) {
                DatabaseNameContext databaseName = ctx.databaseName();
                OwnerSegment databaseSegment = new OwnerSegment(databaseName.getStart().getStartIndex(), databaseName.getStop().getStopIndex(), (IdentifierValue) visit(databaseName.identifier()));
                ownerSegment.setOwner(databaseSegment);
                setLinkedServerForDatabase(databaseSegment, ctx);
            }
            result.setOwner(ownerSegment);
        } else if (null != ctx.databaseName()) {
            DatabaseNameContext databaseName = ctx.databaseName();
            result.setOwner(new OwnerSegment(databaseName.getStart().getStartIndex(), databaseName.getStop().getStopIndex(), (IdentifierValue) visit(databaseName.identifier())));
        }
        return result;
    }
    
    private void setLinkedServerForDatabase(final OwnerSegment databaseSegment, final TableNameContext ctx) {
        if (null != ctx.linkedServerName()) {
            LinkedServerNameContext linkedServerName = ctx.linkedServerName();
            OwnerSegment linkedServerSegment =
                    new OwnerSegment(linkedServerName.getStart().getStartIndex(), linkedServerName.getStop().getStopIndex(), (IdentifierValue) visit(linkedServerName.identifier()));
            databaseSegment.setOwner(linkedServerSegment);
        }
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        ColumnSegment result;
        if (null != ctx.name()) {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
        } else {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.scriptVariableName()));
        }
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            OwnerSegment ownerSegment = new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier()));
            if (null != ctx.databaseName()) {
                ownerSegment.setOwner(new OwnerSegment(ctx.databaseName().getStart().getStartIndex(), ctx.databaseName().getStop().getStopIndex(),
                        (IdentifierValue) visit(ctx.databaseName().identifier())));
            }
            result.setOwner(ownerSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitScriptVariableName(final ScriptVariableNameContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        IndexNameSegment indexName = new IndexNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName);
    }
    
    @Override
    public final ASTNode visitConstraintName(final ConstraintNameContext ctx) {
        return new ConstraintSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitTableNames(final TableNamesContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnNames(final ColumnNamesContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (ColumnNameContext each : ctx.columnName()) {
            result.getValue().add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnNamesWithSort(final ColumnNamesWithSortContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (ColumnNameWithSortContext each : ctx.columnNameWithSort()) {
            result.getValue().add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitExpr(final ExprContext ctx) {
        if (null != ctx.booleanPrimary()) {
            return visit(ctx.booleanPrimary());
        }
        if (null != ctx.LP_()) {
            return visit(ctx.expr(0));
        }
        if (null != ctx.andOperator()) {
            return createBinaryOperationExpression(ctx, ctx.andOperator().getText());
        }
        if (null != ctx.orOperator()) {
            return createBinaryOperationExpression(ctx, ctx.orOperator().getText());
        }
        if (null != ctx.distinctFrom()) {
            return createBinaryOperationExpression(ctx, ctx.distinctFrom().getText());
        }
        if (null != ctx.AT() && null != ctx.TIME() && null != ctx.ZONE()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.expr(1));
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, "AT TIME ZONE", text);
        }
        return new NotExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.expr(0)), false);
    }
    
    private ASTNode createBinaryOperationExpression(final ExprContext ctx, final String operator) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.expr(1));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.IS()) {
            String rightText = "";
            if (null != ctx.NOT()) {
                rightText = rightText + ctx.start.getInputStream().getText(new Interval(ctx.NOT().getSymbol().getStartIndex(), ctx.NOT().getSymbol().getStopIndex())) + " ";
            }
            Token operatorToken = null;
            if (null != ctx.NULL()) {
                operatorToken = ctx.NULL().getSymbol();
            }
            if (null != ctx.TRUE()) {
                operatorToken = ctx.TRUE().getSymbol();
            }
            if (null != ctx.FALSE()) {
                operatorToken = ctx.FALSE().getSymbol();
            }
            int startIndex = null == operatorToken ? ctx.IS().getSymbol().getStopIndex() + 2 : operatorToken.getStartIndex();
            rightText = rightText + ctx.start.getInputStream().getText(new Interval(startIndex, ctx.stop.getStopIndex()));
            ExpressionSegment right = new LiteralExpressionSegment(ctx.IS().getSymbol().getStopIndex() + 2, ctx.stop.getStopIndex(), rightText);
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
            String operator = "IS";
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        if (null != ctx.comparisonOperator() || null != ctx.SAFE_EQ_()) {
            return createCompareSegment(ctx);
        }
        return visit(ctx.predicate());
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        ExpressionSegment right;
        if (null != ctx.predicate()) {
            right = (ExpressionSegment) visit(ctx.predicate());
        } else {
            right = (ExpressionSegment) visit(ctx.subquery());
        }
        String operator = null == ctx.SAFE_EQ_() ? ctx.comparisonOperator().getText() : ctx.SAFE_EQ_().getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitPredicate(final PredicateContext ctx) {
        if (null != ctx.IN()) {
            return createInSegment(ctx);
        }
        if (null != ctx.BETWEEN()) {
            return createBetweenSegment(ctx);
        }
        if (null != ctx.LIKE()) {
            return createBinaryOperationExpressionFromLike(ctx);
        }
        return visit(ctx.bitExpr(0));
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromLike(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ListExpression right = new ListExpression(ctx.simpleExpr(0).start.getStartIndex(), ctx.simpleExpr().get(ctx.simpleExpr().size() - 1).stop.getStopIndex());
        for (SimpleExprContext each : ctx.simpleExpr()) {
            right.getItems().add((ExpressionSegment) visit(each));
        }
        String operator = null == ctx.NOT() ? "LIKE" : "NOT LIKE";
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private InExpression createInSegment(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right;
        if (null == ctx.subquery()) {
            ListExpression listExpression = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
            for (ExprContext each : ctx.expr()) {
                listExpression.getItems().add((ExpressionSegment) visit(each));
            }
            right = listExpression;
        } else {
            right = new SubqueryExpressionSegment(new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()),
                    getOriginalText(ctx.subquery())));
        }
        boolean not = null != ctx.NOT();
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
    }
    
    private BetweenExpression createBetweenSegment(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment between = (ExpressionSegment) visit(ctx.bitExpr(1));
        ExpressionSegment and = (ExpressionSegment) visit(ctx.predicate());
        boolean not = null != ctx.NOT();
        return new BetweenExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, between, and, not);
    }
    
    @Override
    public final ASTNode visitBitExpr(final BitExprContext ctx) {
        if (null != ctx.simpleExpr()) {
            return createExpressionSegment(visit(ctx.simpleExpr()), ctx);
        }
        ExpressionSegment left = (ExpressionSegment) visit(ctx.getChild(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.getChild(2));
        String operator = ctx.getChild(1).getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private ASTNode createExpressionSegment(final ASTNode astNode, final ParserRuleContext context) {
        if (astNode instanceof StringLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((StringLiteralValue) astNode).getValue());
        }
        if (astNode instanceof NumberLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((NumberLiteralValue) astNode).getValue());
        }
        if (astNode instanceof BooleanLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((BooleanLiteralValue) astNode).getValue());
        }
        if (astNode instanceof ParameterMarkerValue) {
            ParameterMarkerValue parameterMarker = (ParameterMarkerValue) astNode;
            ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(),
                    parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(segment);
            return segment;
        }
        if (astNode instanceof SubquerySegment) {
            return new SubqueryExpressionSegment((SubquerySegment) astNode);
        }
        if (astNode instanceof OtherLiteralValue) {
            return new CommonExpressionSegment(context.getStart().getStartIndex(), context.getStop().getStopIndex(), context.getText());
        }
        return astNode;
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        int startIndex = ctx.getStart().getStartIndex();
        int stopIndex = ctx.getStop().getStopIndex();
        if (null != ctx.subquery()) {
            return new SubquerySegment(startIndex, stopIndex, (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery()));
        }
        if (null != ctx.parameterMarker()) {
            ParameterMarkerValue parameterMarker = (ParameterMarkerValue) visit(ctx.parameterMarker());
            ParameterMarkerExpressionSegment result = new ParameterMarkerExpressionSegment(startIndex, stopIndex, parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(result);
            return result;
        }
        if (null != ctx.literals()) {
            return SQLUtils.createLiteralExpression(visit(ctx.literals()), startIndex, stopIndex, ctx.literals().start.getInputStream().getText(new Interval(startIndex, stopIndex)));
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return visit(ctx.columnName());
        }
        if (null != ctx.xmlMethodCall()) {
            return visit(ctx.xmlMethodCall());
        }
        if (null != ctx.LP_() && 1 == ctx.expr().size()) {
            return visit(ctx.expr(0));
        }
        return visitRemainSimpleExpr(ctx);
    }
    
    private ASTNode visitRemainSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.caseExpression()) {
            visit(ctx.caseExpression());
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new OtherLiteralValue(text);
        }
        for (ExprContext each : ctx.expr()) {
            visit(each);
        }
        for (SimpleExprContext each : ctx.simpleExpr()) {
            visit(each);
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction or specialFunction.");
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        if (AggregationType.isAggregationType(aggregationType)) {
            return createAggregationSegment(ctx, aggregationType);
        }
        FunctionSegment functionSegment = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), aggregationType, getOriginalText(ctx));
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                functionSegment.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return functionSegment;
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        if (null != ctx.distinct()) {
            AggregationDistinctProjectionSegment result =
                    new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx), getDistinctExpression(ctx));
            result.getParameters().addAll(getExpressions(ctx));
            return result;
        }
        AggregationProjectionSegment result = new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx));
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressions(final AggregationFunctionContext ctx) {
        if (null == ctx.expr()) {
            return Collections.emptyList();
        }
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (ExprContext each : ctx.expr()) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    private String getDistinctExpression(final AggregationFunctionContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    @Override
    public final ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.conversionFunction()) {
            return visit(ctx.conversionFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        if (null != ctx.openJsonFunction()) {
            return visit(ctx.openJsonFunction());
        }
        if (null != ctx.openRowSetFunction()) {
            return visit(ctx.openRowSetFunction());
        }
        if (null != ctx.jsonFunction()) {
            return visit(ctx.jsonFunction());
        }
        if (null != ctx.windowFunction()) {
            return visit(ctx.windowFunction());
        }
        if (null != ctx.approxFunction()) {
            return visit(ctx.approxFunction());
        }
        if (null != ctx.graphFunction()) {
            return visit(ctx.graphFunction());
        }
        if (null != ctx.trimFunction()) {
            return visit(ctx.trimFunction());
        }
        if (null != ctx.changeTableFunction()) {
            return visit(ctx.changeTableFunction());
        }
        if (null != ctx.aiFunction()) {
            return visit(ctx.aiFunction());
        }
        if (null != ctx.freetextTableFunction()) {
            return visit(ctx.freetextTableFunction());
        }
        if (null != ctx.currentUserFunction()) {
            return visit(ctx.currentUserFunction());
        }
        if (null != ctx.vectorSearchFunction()) {
            return visit(ctx.vectorSearchFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getChild(0).getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitVectorSearchFunction(final VectorSearchFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.VECTOR_SEARCH().getText(), getOriginalText(ctx));
        VectorSearchParametersContext params = ctx.vectorSearchParameters();
        if (null != params.vectorSearchTable()) {
            String tableText = params.vectorSearchTable().getText();
            result.getParameters().add(new LiteralExpressionSegment(params.vectorSearchTable().getStart().getStartIndex(), params.vectorSearchTable().getStop().getStopIndex(), tableText));
        }
        if (null != params.columnName()) {
            String columnText = params.columnName().getText();
            result.getParameters().add(new LiteralExpressionSegment(params.columnName().getStart().getStartIndex(), params.columnName().getStop().getStopIndex(), columnText));
        }
        if (null != params.expr() && !params.expr().isEmpty()) {
            result.getParameters().add((ExpressionSegment) visit(params.expr(0)));
        }
        if (null != params.vectorSearchMetric() && null != params.vectorSearchMetric().stringLiterals()) {
            String metricText = params.vectorSearchMetric().stringLiterals().getText();
            result.getParameters().add(new LiteralExpressionSegment(params.vectorSearchMetric().stringLiterals().getStart().getStartIndex(),
                    params.vectorSearchMetric().stringLiterals().getStop().getStopIndex(), metricText));
        }
        if (null != params.expr() && params.expr().size() > 1) {
            result.getParameters().add((ExpressionSegment) visit(params.expr(1)));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCurrentUserFunction(final CurrentUserFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CURRENT_USER().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitFreetextTableFunction(final FreetextTableFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.FREETEXTTABLE().getText(), getOriginalText(ctx));
        for (ExprContext each : ctx.expr()) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAiFunction(final AiFunctionContext ctx) {
        if (null != ctx.aiGenerateEmbeddingsFunction()) {
            return visit(ctx.aiGenerateEmbeddingsFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getChild(0).getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitAiGenerateEmbeddingsFunction(final AiGenerateEmbeddingsFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.AI_GENERATE_EMBEDDINGS().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr(0)));
        result.getParameters().add(new LiteralExpressionSegment(ctx.identifier().getStart().getStartIndex(), ctx.identifier().getStop().getStopIndex(), ctx.identifier().getText()));
        if (ctx.expr().size() > 1) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr(1)));
        }
        return result;
    }
    
    @Override
    public ASTNode visitChangeTableFunction(final ChangeTableFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHANGETABLE().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlMethodCall(final XmlMethodCallContext ctx) {
        String fullMethodName;
        if (null == ctx.alias()) {
            fullMethodName = ctx.columnName().getText() + "." + ctx.xmlMethodName().getText();
        } else {
            fullMethodName = ctx.alias().getText() + "." + ctx.columnName().getText() + "." + ctx.xmlMethodName().getText();
        }
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                fullMethodName, getOriginalText(ctx));
        if (null == ctx.alias()) {
            OwnerSegment owner = new OwnerSegment(ctx.columnName().getStart().getStartIndex(), ctx.columnName().getStop().getStopIndex(), new IdentifierValue(ctx.columnName().getText()));
            result.setOwner(owner);
        } else {
            String ownerName = ctx.alias().getText() + "." + ctx.columnName().getText();
            OwnerSegment owner = new OwnerSegment(ctx.alias().getStart().getStartIndex(), ctx.columnName().getStop().getStopIndex(), new IdentifierValue(ownerName));
            result.setOwner(owner);
        }
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    private ASTNode getFunctionSegment(final int startIndex, final int stopIndex, final String functionName, final String text, final List<ExprContext> exprList) {
        FunctionSegment result = new FunctionSegment(startIndex, stopIndex, functionName, text);
        if (null != exprList) {
            for (ExprContext each : exprList) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitTrimFunction(final TrimFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TRIM().getText(), getOriginalText(ctx));
        if (null != ctx.BOTH()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.BOTH().getSymbol().getStartIndex(), ctx.BOTH().getSymbol().getStopIndex(),
                    new OtherLiteralValue(ctx.BOTH().getSymbol().getText()).getValue()));
        }
        if (null != ctx.TRAILING()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.TRAILING().getSymbol().getStartIndex(), ctx.TRAILING().getSymbol().getStopIndex(),
                    new OtherLiteralValue(ctx.TRAILING().getSymbol().getText()).getValue()));
        }
        if (null != ctx.LEADING()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.LEADING().getSymbol().getStartIndex(), ctx.LEADING().getSymbol().getStopIndex(),
                    new OtherLiteralValue(ctx.LEADING().getSymbol().getText()).getValue()));
        }
        for (ExprContext each : ctx.expr()) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGraphFunction(final GraphFunctionContext ctx) {
        if (null != ctx.graphAggFunction()) {
            return visit(ctx.graphAggFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getChild(0).getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitGraphAggFunction(final GraphAggFunctionContext ctx) {
        return getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.graphAggFunctionName().getText(), getOriginalText(ctx), ctx.expr());
    }
    
    @Override
    public final ASTNode visitApproxFunction(final ApproxFunctionContext ctx) {
        return getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcName.getText(), getOriginalText(ctx), ctx.expr());
    }
    
    @Override
    public final ASTNode visitConversionFunction(final ConversionFunctionContext ctx) {
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.convertFunction()) {
            return visit(ctx.convertFunction());
        }
        if (null != ctx.parseFunction()) {
            return visit(ctx.parseFunction());
        }
        if (null != ctx.tryParseFunction()) {
            return visit(ctx.tryParseFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getChild(0).getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitParseFunction(final ParseFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.PARSE().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr(0)));
        result.getParameters().add((DataTypeSegment) visit(ctx.dataType()));
        if (null != ctx.USING() && ctx.expr().size() > 1) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr(1)));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTryParseFunction(final TryParseFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TRY_PARSE().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr(0)));
        result.getParameters().add((DataTypeSegment) visit(ctx.dataType()));
        if (null != ctx.USING() && ctx.expr().size() > 1) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr(1)));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitWindowFunction(final WindowFunctionContext ctx) {
        if (null != ctx.lagLeadFunction()) {
            return visit(ctx.lagLeadFunction());
        }
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcName.getText(), getOriginalText(ctx));
        if (null != ctx.NTILE() || null != ctx.FIRST_VALUE() || null != ctx.LAST_VALUE() || null != ctx.PERCENTILE_CONT() || null != ctx.PERCENTILE_DISC()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.getChild(2)));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitLagLeadFunction(final LagLeadFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcName.getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> parameters = getLagLeadFunctionParameters(ctx);
        result.getParameters().addAll(parameters);
        return result;
    }
    
    private Collection<ExpressionSegment> getLagLeadFunctionParameters(final LagLeadFunctionContext ctx) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        boolean insideParentheses = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            String childText = ctx.getChild(i).getText();
            if ("(".equals(childText)) {
                insideParentheses = true;
                continue;
            }
            if (")".equals(childText) && insideParentheses) {
                break;
            }
            if (insideParentheses && ctx.getChild(i) instanceof ExprContext) {
                result.add((ExpressionSegment) visit(ctx.getChild(i)));
            }
        }
        return result;
    }
    
    @Override
    public final ASTNode visitJsonFunction(final JsonFunctionContext ctx) {
        if (null != ctx.jsonArrayFunction()) {
            return visit(ctx.jsonArrayFunction());
        }
        if (null != ctx.jsonObjectFunction()) {
            return visit(ctx.jsonObjectFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText(), getOriginalText(ctx));
    }
    
    @Override
    public final ASTNode visitJsonArrayFunction(final JsonArrayFunctionContext ctx) {
        FunctionSegment result = (FunctionSegment) getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.JSON_ARRAY().getText(), getOriginalText(ctx), ctx.expr());
        if (null != ctx.jsonNullClause()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.jsonNullClause().start.getStartIndex(), ctx.jsonNullClause().stop.getStopIndex(), ctx.jsonNullClause().getText()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitJsonObjectFunction(final JsonObjectFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.JSON_OBJECT().getText(), getOriginalText(ctx));
        if (null != ctx.jsonKeyValue()) {
            for (JsonKeyValueContext each : ctx.jsonKeyValue()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        if (null != ctx.jsonNullClause()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.jsonNullClause()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitJsonNullClause(final JsonNullClauseContext ctx) {
        return new JsonNullClauseSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
    }
    
    @Override
    public final ASTNode visitJsonKeyValue(final JsonKeyValueContext ctx) {
        KeyValueSegment result = new KeyValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
        if (null != ctx.expr()) {
            result.setKey((ExpressionSegment) visit(ctx.expr(0)));
            result.setValue((ExpressionSegment) visit(ctx.expr(1)));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        String functionName = null == ctx.CAST() ? ctx.TRY_CAST().getText() : ctx.CAST().getText();
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), functionName, getOriginalText(ctx));
        ASTNode exprSegment = visit(ctx.expr());
        if (exprSegment instanceof ColumnSegment) {
            result.getParameters().add((ColumnSegment) exprSegment);
        } else if (exprSegment instanceof LiteralExpressionSegment) {
            result.getParameters().add((LiteralExpressionSegment) exprSegment);
        } else if (exprSegment instanceof FunctionSegment) {
            result.getParameters().add((FunctionSegment) exprSegment);
        }
        result.getParameters().add((DataTypeSegment) visit(ctx.dataType()));
        return result;
    }
    
    @Override
    public ASTNode visitConvertFunction(final ConvertFunctionContext ctx) {
        String functionName = null == ctx.CONVERT() ? ctx.TRY_CONVERT().getText() : ctx.CONVERT().getText();
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), functionName, getOriginalText(ctx));
        result.getParameters().add((DataTypeSegment) visit(ctx.dataType()));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        if (null != ctx.NUMBER_()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.NUMBER_().getSymbol().getStartIndex(), ctx.NUMBER_().getSymbol().getStopIndex(), ctx.NUMBER_().getText()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitCharFunction(final CharFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHAR().getText(), getOriginalText(ctx));
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public final ASTNode visitOpenJsonFunction(final OpenJsonFunctionContext ctx) {
        return getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.OPENJSON().getText(), getOriginalText(ctx), ctx.expr());
    }
    
    @Override
    public final ASTNode visitOpenRowSetFunction(final OpenRowSetFunctionContext ctx) {
        FunctionSegment result = (FunctionSegment) getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.OPENROWSET().getText(), getOriginalText(ctx), ctx.expr());
        if (null != ctx.tableName()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.tableName().getStart().getStartIndex(), ctx.tableName().getStop().getStopIndex(), ctx.tableName().getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitOpenQueryFunction(final OpenQueryFunctionContext ctx) {
        return getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.OPENQUERY().getText(), getOriginalText(ctx), ctx.expr());
    }
    
    @Override
    public ASTNode visitOpenDatasourceFunction(final OpenDatasourceFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.OPENDATASOURCE().getText(), getOriginalText(ctx));
        for (ExprContext each : ctx.expr()) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        if (null != ctx.tableName()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.tableName().getStart().getStartIndex(), ctx.tableName().getStop().getStopIndex(), ctx.tableName().getText()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        return getFunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.regularFunctionName().getText(), getOriginalText(ctx), ctx.expr());
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        return new KeywordValue(ctx.getText());
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
    }
    
    @Override
    public final ASTNode visitOrderByItem(final OrderByItemContext ctx) {
        OrderDirection orderDirection = null == ctx.DESC() ? OrderDirection.ASC : OrderDirection.DESC;
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            return new ColumnOrderByItemSegment(column, orderDirection, null);
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtils.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection, null);
        }
        return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(), ctx.expr().getStop().getStopIndex(), getOriginalText(ctx.expr()), orderDirection, null,
                (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public final ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setDataTypeName(((KeywordValue) visit(ctx.dataTypeName())).getValue());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.dataTypeLength()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.dataTypeLength());
            result.setDataLength(dataTypeLengthSegment);
        }
        return result;
    }
    
    @Override
    public final ASTNode visitDataTypeLength(final DataTypeLengthContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        List<TerminalNode> numbers = ctx.NUMBER_();
        if (numbers.size() == 1) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        }
        if (numbers.size() == 2) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
            result.setScale(Integer.parseInt(numbers.get(1).getText()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitViewName(final ViewNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), (IdentifierValue) visit(ctx.name())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        SelectStatement result = (SelectStatement) visit(ctx.aggregationClause());
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitAggregationClause(final AggregationClauseContext ctx) {
        // TODO :Unsupported for union | except | intersect SQL.
        return visit(ctx.selectClause(0));
    }
    
    @Override
    public ASTNode visitSelectClause(final SelectClauseContext ctx) {
        SelectStatement result = new SelectStatement(databaseType);
        result.setProjections((ProjectionsSegment) visit(ctx.projections()));
        if (null != ctx.selectWithClause() && null != ctx.selectWithClause().cteClauseSet()) {
            Collection<CommonTableExpressionSegment> commonTableExpressionSegments = getCommonTableExpressionSegmentsUsingCteClauseSet(ctx.selectWithClause().cteClauseSet());
            WithSegment withSegment = new WithSegment(ctx.selectWithClause().start.getStartIndex(), ctx.selectWithClause().stop.getStopIndex(), commonTableExpressionSegments);
            result.setWith(withSegment);
        }
        if (null != ctx.duplicateSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.intoClause()) {
            result.setInto((TableSegment) visit(ctx.intoClause()));
        }
        if (null != ctx.fromClause()) {
            TableSegment tableSource = (TableSegment) visit(ctx.fromClause().tableReferences());
            result.setFrom(tableSource);
        }
        if (null != ctx.withTableHint()) {
            result.setWithTableHint((WithTableHintSegment) visit(ctx.withTableHint()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.groupByClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupByClause()));
        }
        if (null != ctx.havingClause()) {
            result.setHaving((HavingSegment) visit(ctx.havingClause()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy(getOrderBySegment(ctx.orderByClause()));
            result.setLimit(getLimitSegment(ctx.orderByClause()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<CommonTableExpressionSegment> getCommonTableExpressionSegmentsUsingCteClauseSet(final CteClauseSetContext ctx) {
        Collection<CommonTableExpressionSegment> result = new LinkedList<>();
        for (CteClauseContext each : ctx.cteClause()) {
            SubquerySegment subquery = new SubquerySegment(each.subquery().aggregationClause().start.getStartIndex(),
                    each.subquery().aggregationClause().stop.getStopIndex(), (SelectStatement) visit(each.subquery()), getOriginalText(each.subquery()));
            CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(each.start.getStartIndex(), each.stop.getStopIndex(), (AliasSegment) visit(each.alias()), subquery);
            if (null != each.columnNames()) {
                ColumnNamesContext columnNames = each.columnNames();
                CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(columnNames);
                commonTableExpression.getColumns().addAll(columns.getValue());
            }
            result.add(commonTableExpression);
        }
        return result;
    }
    
    @Override
    public ASTNode visitHavingClause(final HavingClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.expr());
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    private LimitSegment getLimitSegment(final OrderByClauseContext ctx) {
        LimitSegment result = null;
        PaginationValueSegment offset = null;
        PaginationValueSegment rowCount = null;
        if (null != ctx.OFFSET()) {
            ASTNode astNode = visit(ctx.expr(0));
            if (astNode instanceof LiteralExpressionSegment && ((LiteralExpressionSegment) astNode).getLiterals() instanceof Number) {
                offset = new NumberLiteralLimitValueSegment(ctx.expr(0).start.getStartIndex(), ctx.expr(0).stop.getStopIndex(),
                        ((Number) ((LiteralExpressionSegment) astNode).getLiterals()).longValue());
            } else if (astNode instanceof ParameterMarkerExpressionSegment) {
                offset = new ParameterMarkerLimitValueSegment(ctx.expr(0).start.getStartIndex(), ctx.expr(0).stop.getStopIndex(), parameterMarkerSegments.size() - 1);
            }
        }
        if (null != ctx.FETCH()) {
            ASTNode astNode = visit(ctx.expr(1));
            if (astNode instanceof LiteralExpressionSegment && ((LiteralExpressionSegment) astNode).getLiterals() instanceof Number) {
                rowCount = new NumberLiteralLimitValueSegment(ctx.expr(1).start.getStartIndex(), ctx.expr(1).stop.getStopIndex(),
                        ((Number) ((LiteralExpressionSegment) astNode).getLiterals()).longValue());
            } else if (astNode instanceof ParameterMarkerExpressionSegment) {
                rowCount = new ParameterMarkerLimitValueSegment(ctx.expr(1).start.getStartIndex(), ctx.expr(1).stop.getStopIndex(), parameterMarkerSegments.size() - 1);
            }
        }
        if (null != offset) {
            result = new LimitSegment(ctx.OFFSET().getSymbol().getStartIndex(), ctx.stop.getStopIndex(), offset, rowCount);
        }
        return result;
    }
    
    private OrderBySegment getOrderBySegment(final OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        int orderByStartIndex = ctx.start.getStartIndex();
        int orderByStopIndex = ctx.start.getStartIndex();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
            orderByStopIndex = each.stop.getStopIndex();
        }
        return new OrderBySegment(orderByStartIndex, orderByStopIndex, items);
    }
    
    private boolean isDistinct(final SelectClauseContext ctx) {
        return ((BooleanLiteralValue) visit(ctx.duplicateSpecification())).getValue();
    }
    
    @Override
    public ASTNode visitProjections(final ProjectionsContext ctx) {
        List<ProjectionSegment> projections = new LinkedList<>();
        if (null != ctx.top()) {
            projections.add((ProjectionSegment) visit(ctx.top()));
        }
        for (ProjectionContext each : ctx.projection()) {
            projections.add((ProjectionSegment) visit(each));
        }
        ProjectionsSegment result = new ProjectionsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Override
    public ASTNode visitTableReferences(final TableReferencesContext ctx) {
        TableSegment result = (TableSegment) visit(ctx.tableReference(0));
        if (ctx.tableReference().size() > 1) {
            for (int i = 1; i < ctx.tableReference().size(); i++) {
                result = generateJoinTableSourceFromTableReference(ctx.tableReference(i), result);
            }
        }
        return result;
    }
    
    private JoinTableSegment generateJoinTableSourceFromTableReference(final TableReferenceContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft(tableSegment);
        result.setRight((TableSegment) visit(ctx));
        result.setJoinType(JoinType.COMMA.name());
        return result;
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitGroupByClause(final GroupByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        if (!ctx.groupByItem().isEmpty()) {
            for (GroupByItemContext each : ctx.groupByItem()) {
                items.addAll(generateOrderByItemsFromGroupByItem(each));
            }
        } else if (!ctx.orderByItem().isEmpty()) {
            for (OrderByItemContext each : ctx.orderByItem()) {
                items.add((OrderByItemSegment) visit(each));
            }
        }
        return new GroupBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemsFromGroupByItem(final GroupByItemContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        if (null != ctx.expr()) {
            OrderByItemSegment item = (OrderByItemSegment) extractValueFromGroupByItemExpression(ctx.expr());
            result.add(item);
        } else if (null != ctx.rollupCubeClause()) {
            result.addAll(generateOrderByItemSegmentsFromRollupCubeClause(ctx.rollupCubeClause()));
        } else {
            result.addAll(generateOrderByItemSegmentsFromGroupingSetsClause(ctx.groupingSetsClause()));
        }
        return result;
    }
    
    private ASTNode extractValueFromGroupByItemExpression(final ExprContext ctx) {
        ASTNode expression = visit(ctx);
        if (expression instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) expression;
            return new ColumnOrderByItemSegment(column, OrderDirection.ASC, null);
        }
        if (expression instanceof LiteralExpressionSegment) {
            LiteralExpressionSegment literalExpression = (LiteralExpressionSegment) expression;
            return new IndexOrderByItemSegment(literalExpression.getStartIndex(), literalExpression.getStopIndex(),
                    SQLUtils.getExactlyNumber(literalExpression.getLiterals().toString(), 10).intValue(), OrderDirection.ASC, null);
        }
        return new ExpressionOrderByItemSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx), OrderDirection.ASC, null, (ExpressionSegment) expression);
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromRollupCubeClause(final RollupCubeClauseContext ctx) {
        return new LinkedList<>(generateOrderByItemSegmentsFromGroupingExprList(ctx.groupingExprList()));
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromGroupingSetsClause(final GroupingSetsClauseContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        if (null != ctx.rollupCubeClause()) {
            for (RollupCubeClauseContext each : ctx.rollupCubeClause()) {
                result.addAll(generateOrderByItemSegmentsFromRollupCubeClause(each));
            }
        }
        if (null != ctx.groupingExprList()) {
            for (GroupingExprListContext each : ctx.groupingExprList()) {
                result.addAll(generateOrderByItemSegmentsFromGroupingExprList(each));
            }
        }
        return result;
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromGroupingExprList(final GroupingExprListContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        for (ExpressionListContext each : ctx.expressionList()) {
            result.addAll(generateOrderByItemSegmentsFromExpressionList(each));
        }
        return result;
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromExpressionList(final ExpressionListContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.add((OrderByItemSegment) extractValueFromGroupByItemExpression(each));
            }
        }
        return result;
    }
    
    /**
     * Get original text.
     *
     * @param ctx context
     * @return original text
     */
    protected String getOriginalText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        InsertStatement result;
        if (null != ctx.insertDefaultValue()) {
            result = (InsertStatement) visit(ctx.insertDefaultValue());
        } else if (null != ctx.insertValuesClause()) {
            result = (InsertStatement) visit(ctx.insertValuesClause());
        } else if (null != ctx.insertExecClause()) {
            result = (InsertStatement) visit(ctx.insertExecClause());
        } else {
            result = (InsertStatement) visit(ctx.insertSelectClause());
        }
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        if (null != ctx.withTableHint()) {
            result.setWithTableHint((WithTableHintSegment) visit(ctx.withTableHint()));
        }
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.rowSetFunction()) {
            result.setRowSetFunction((FunctionSegment) visit(ctx.rowSetFunction()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitRowSetFunction(final RowSetFunctionContext ctx) {
        if (null != ctx.openRowSetFunction()) {
            return visit(ctx.openRowSetFunction());
        } else if (null != ctx.openQueryFunction()) {
            return visit(ctx.openQueryFunction());
        } else if (null != ctx.predictFunction()) {
            return visit(ctx.predictFunction());
        } else {
            return visit(ctx.openDatasourceFunction());
        }
    }
    
    @Override
    public ASTNode visitPredictFunction(final PredictFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.PREDICT().getText(), getOriginalText(ctx));
        if (null != ctx.variableName()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.variableName()));
        } else if (null != ctx.literals()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.literals()));
        }
        result.getParameters().add(new LiteralExpressionSegment(ctx.tableName().getStart().getStartIndex(), ctx.tableName().getStop().getStopIndex(), ctx.tableName().getText()));
        if (null != ctx.alias()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.alias().getStart().getStartIndex(), ctx.alias().getStop().getStopIndex(), ctx.alias().getText()));
        }
        if (null != ctx.ONNX()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.ONNX().getSymbol().getStartIndex(), ctx.ONNX().getSymbol().getStopIndex(), ctx.ONNX().getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWithTableHint(final WithTableHintContext ctx) {
        WithTableHintSegment result = new WithTableHintSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        Collection<TableHintLimitedSegment> tableHintLimitedSegments = new LinkedList<>();
        if (null != ctx.tableHintLimited()) {
            for (TableHintLimitedContext each : ctx.tableHintLimited()) {
                tableHintLimitedSegments.add((TableHintLimitedSegment) visit(each));
            }
        }
        if (null != ctx.tableHintExtended()) {
            for (TableHintExtendedContext each : ctx.tableHintExtended()) {
                TableHintLimitedSegment segment = new TableHintLimitedSegment(each.start.getStartIndex(), each.stop.getStopIndex());
                segment.setValue(each.getText());
                tableHintLimitedSegments.add(segment);
            }
        }
        result.getTableHintLimitedSegments().addAll(tableHintLimitedSegments);
        return result;
    }
    
    @Override
    public ASTNode visitTableHintLimited(final TableHintLimitedContext ctx) {
        TableHintLimitedSegment result = new TableHintLimitedSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setValue(ctx.getText());
        return result;
    }
    
    @Override
    public ASTNode visitInsertDefaultValue(final InsertDefaultValueContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertExecClause(final InsertExecClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        result.setExec((ExecSegment) visit(ctx.exec()));
        return result;
    }
    
    @Override
    public ASTNode visitExec(final ExecContext ctx) {
        ExecSegment result = new ExecSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.procedureName()) {
            result.setProcedureName((FunctionNameSegment) visitProcedureName(ctx.procedureName()));
        }
        if (null != ctx.expr()) {
            Collection<ExpressionSegment> items = new LinkedList<>();
            for (ExprContext each : ctx.expr()) {
                items.add((ExpressionSegment) visit(each));
            }
            result.getExpressionSegments().addAll(items);
        }
        return result;
    }
    
    @Override
    public ASTNode visitProcedureName(final ProcedureNameContext ctx) {
        FunctionNameSegment result = new FunctionNameSegment(ctx.name().start.getStartIndex(), ctx.name().stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
        if (null != ctx.owner()) {
            result.setOwner(new OwnerSegment(ctx.owner().start.getStartIndex(), ctx.owner().stop.getStopIndex(), (IdentifierValue) visit(ctx.owner())));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitOutputClause(final OutputClauseContext ctx) {
        OutputSegment result = new OutputSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.outputWithColumns()) {
            OutputWithColumnsContext outputWithColumnsContext = ctx.outputWithColumns();
            ProjectionsSegment outputColumns = new ProjectionsSegment(outputWithColumnsContext.start.getStartIndex(), outputWithColumnsContext.stop.getStopIndex());
            for (int i = 0; i < outputWithColumnsContext.getChildCount(); i += 2) {
                ParseTree each = outputWithColumnsContext.getChild(i);
                if (each instanceof OutputWithColumnContext) {
                    outputColumns.getProjections().add(createColumnProjectionSegment((OutputWithColumnContext) each));
                }
                if (each instanceof ScalarExpressionContext) {
                    outputColumns.getProjections().add(createScalarExpressionContext((ScalarExpressionContext) each));
                }
            }
            result.setOutputColumns(outputColumns);
        }
        if (null != ctx.outputTableName()) {
            if (null != ctx.outputTableName().tableName()) {
                result.setTable((SimpleTableSegment) visit(ctx.outputTableName().tableName()));
            }
            if (null != ctx.columnNames()) {
                ColumnNamesContext columnNames = ctx.columnNames();
                CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(columnNames);
                result.getTableColumns().addAll(columns.getValue());
            }
        }
        return result;
    }
    
    private ProjectionSegment createScalarExpressionContext(final ScalarExpressionContext context) {
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(context.start.getStartIndex(), context.stop.getStopIndex(),
                getOriginalText(context), (ExpressionSegment) visit(context.expr()));
        if (null != context.alias()) {
            result.setAlias(new AliasSegment(context.alias().start.getStartIndex(), context.alias().stop.getStopIndex(), new IdentifierValue(context.alias().getText())));
        }
        return result;
    }
    
    private ProjectionSegment createColumnProjectionSegment(final OutputWithColumnContext context) {
        ColumnSegment column = new ColumnSegment(context.start.getStartIndex(), context.stop.getStopIndex(), new IdentifierValue(context.name().getText()));
        ColumnProjectionSegment result = new ColumnProjectionSegment(column);
        if (null != context.alias()) {
            result.setAlias(new AliasSegment(context.alias().start.getStartIndex(), context.alias().stop.getStopIndex(), new IdentifierValue(context.alias().getText())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        result.getValues().addAll(createInsertValuesSegments(ctx.assignmentValues()));
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        return result;
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final Collection<AssignmentValuesContext> assignmentValuesContexts) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (AssignmentValuesContext each : assignmentValuesContexts) {
            result.add((InsertValuesSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertSelectClause(final InsertSelectClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        result.setInsertSelect(createInsertSelectSegment(ctx));
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private InsertColumnsSegment createInsertColumns(final ColumnNamesContext columnNames, final int startIndex) {
        if (null == columnNames) {
            return new InsertColumnsSegment(startIndex - 1, startIndex - 1, Collections.emptyList());
        } else {
            CollectionValue<ColumnSegment> columnSegments = (CollectionValue<ColumnSegment>) visit(columnNames);
            return new InsertColumnsSegment(columnNames.start.getStartIndex(), columnNames.stop.getStopIndex(), columnSegments.getValue());
        }
    }
    
    private SubquerySegment createInsertSelectSegment(final InsertSelectClauseContext ctx) {
        SelectStatement selectStatement = (SelectStatement) visit(ctx.select());
        return new SubquerySegment(ctx.select().start.getStartIndex(), ctx.select().stop.getStopIndex(), selectStatement, getOriginalText(ctx.select()));
    }
    
    @Override
    public ASTNode visitWithClause(final WithClauseContext ctx) {
        Collection<CommonTableExpressionSegment> commonTableExpressionSegments = getCommonTableExpressionSegmentsUsingCteClauseSet(ctx.cteClauseSet());
        return new WithSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), commonTableExpressionSegments);
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement(databaseType);
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        result.setTable((TableSegment) visit(ctx.tableReferences()));
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        if (null != ctx.fromClause()) {
            result.setFrom((TableSegment) visit(ctx.fromClause()));
        }
        if (null != ctx.withTableHint()) {
            result.setWithTableHint((WithTableHintSegment) visit(ctx.withTableHint()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.optionHint()) {
            result.setOptionHint((OptionHintSegment) visit(ctx.optionHint()));
        }
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitOptionHint(final OptionHintContext ctx) {
        return new OptionHintSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitSetAssignmentsClause(final SetAssignmentsClauseContext ctx) {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentContext each : ctx.assignment()) {
            assignments.add((ColumnAssignmentSegment) visit(each));
        }
        return new SetAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitAssignmentValues(final AssignmentValuesContext ctx) {
        List<ExpressionSegment> segments = new LinkedList<>();
        for (AssignmentValueContext each : ctx.assignmentValue()) {
            segments.add((ExpressionSegment) visit(each));
        }
        return new InsertValuesSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), segments);
    }
    
    @Override
    public ASTNode visitAssignment(final AssignmentContext ctx) {
        ColumnSegment column = (ColumnSegment) visitColumnName(ctx.columnName());
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(column);
        ExpressionSegment value = (ExpressionSegment) visit(ctx.assignmentValue());
        return new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
    }
    
    @Override
    public ASTNode visitAssignmentValue(final AssignmentValueContext ctx) {
        ExprContext expr = ctx.expr();
        if (null != expr) {
            return visit(expr);
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement(databaseType);
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        if (null != ctx.multipleTablesClause()) {
            result.setTable((TableSegment) visit(ctx.multipleTablesClause()));
        } else {
            result.setTable((TableSegment) visit(ctx.singleTableClause()));
        }
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause(final SingleTableClauseContext ctx) {
        if (null != ctx.tableName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        } else if (null != ctx.rowSetFunction()) {
            FunctionSegment functionSegment = (FunctionSegment) visit(ctx.rowSetFunction());
            FunctionTableSegment result = new FunctionTableSegment(functionSegment.getStartIndex(), functionSegment.getStopIndex(), functionSegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        return null;
    }
    
    @Override
    public ASTNode visitMultipleTablesClause(final MultipleTablesClauseContext ctx) {
        DeleteMultiTableSegment result = new DeleteMultiTableSegment();
        TableSegment relateTableSource = (TableSegment) visit(ctx.tableReferences());
        result.setRelationTable(relateTableSource);
        result.setActualDeleteTables(generateTablesFromTableMultipleTableNames(ctx.multipleTableNames()));
        return result;
    }
    
    private List<SimpleTableSegment> generateTablesFromTableMultipleTableNames(final MultipleTableNamesContext ctx) {
        List<SimpleTableSegment> result = new LinkedList<>();
        for (TableNameContext each : ctx.tableName()) {
            result.add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDuplicateSpecification(final DuplicateSpecificationContext ctx) {
        return new BooleanLiteralValue(null != ctx.DISTINCT());
    }
    
    @Override
    public ASTNode visitProjection(final ProjectionContext ctx) {
        if (null != ctx.qualifiedShorthand()) {
            QualifiedShorthandContext shorthand = ctx.qualifiedShorthand();
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(shorthand.getStart().getStartIndex(), shorthand.getStop().getStopIndex());
            IdentifierValue identifier = new IdentifierValue(shorthand.identifier().getText());
            result.setOwner(new OwnerSegment(shorthand.identifier().getStart().getStartIndex(), shorthand.identifier().getStop().getStopIndex(), identifier));
            return result;
        }
        if (null != ctx.unqualifiedShorthand()) {
            return new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex());
        }
        AliasSegment alias = null == ctx.alias() ? null : (AliasSegment) visit(ctx.alias());
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            ColumnProjectionSegment result = new ColumnProjectionSegment(column);
            result.setAlias(alias);
            return result;
        }
        return createProjection(ctx, alias);
    }
    
    @Override
    public ASTNode visitTop(final TopContext ctx) {
        int startIndex = ctx.topNum().getStart().getStartIndex();
        int stopIndex = ctx.topNum().getStop().getStopIndex();
        ASTNode topNum = visit(ctx.topNum());
        if (topNum instanceof NumberLiteralValue) {
            NumberLiteralRowNumberValueSegment rowNumberSegment = new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, ((NumberLiteralValue) topNum).getValue().longValue(), false);
            return new TopProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), rowNumberSegment, null != ctx.alias() ? ctx.alias().getText() : null);
        }
        ParameterMarkerSegment parameterSegment = new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerValue) topNum).getValue(), false);
        parameterMarkerSegments.add(parameterSegment);
        return new TopProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (RowNumberValueSegment) parameterSegment, null != ctx.alias() ? ctx.alias().getText() : null);
    }
    
    @Override
    public ASTNode visitAlias(final AliasContext ctx) {
        if (null != ctx.identifier()) {
            return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        }
        if (null != ctx.NCHAR_TEXT()) {
            return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.NCHAR_TEXT().getText().substring(1)));
        }
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.STRING_().getText()));
    }
    
    private ASTNode createProjection(final ProjectionContext ctx, final AliasSegment alias) {
        ASTNode projection = visit(ctx.expr());
        if (projection instanceof AggregationProjectionSegment) {
            ((AggregationProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof ExpressionProjectionSegment) {
            ((ExpressionProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof FunctionSegment) {
            FunctionSegment segment = (FunctionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText(), segment);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof CommonExpressionSegment) {
            CommonExpressionSegment segment = (CommonExpressionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText(), segment);
            result.setAlias(alias);
            return result;
        }
        // FIXME :For DISTINCT()
        if (projection instanceof ColumnSegment) {
            ColumnProjectionSegment result = new ColumnProjectionSegment((ColumnSegment) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryExpressionSegment subqueryExpressionSegment = (SubqueryExpressionSegment) projection;
            String text = ctx.start.getInputStream().getText(new Interval(subqueryExpressionSegment.getStartIndex(), subqueryExpressionSegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(((SubqueryExpressionSegment) projection).getSubquery(), text);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpression = (BinaryOperationExpression) projection;
            int startIndex = getStartIndexWithAlias(binaryExpression, alias);
            int stopIndex = getStopIndexWithAlias(binaryExpression, alias);
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, binaryExpression.getText(), binaryExpression);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof ParameterMarkerExpressionSegment) {
            ParameterMarkerExpressionSegment result = (ParameterMarkerExpressionSegment) projection;
            result.setAlias(alias);
            return projection;
        }
        ExpressionSegment column = (ExpressionSegment) projection;
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(getStartIndexWithAlias(column, alias), getStopIndexWithAlias(column, alias), String.valueOf(column.getText()), column);
        result.setAlias(alias);
        return result;
    }
    
    private int getStartIndexWithAlias(final SQLSegment sqlSegment, final AliasSegment alias) {
        return null != alias && alias.getStartIndex() < sqlSegment.getStartIndex() ? alias.getStartIndex() : sqlSegment.getStartIndex();
    }
    
    private int getStopIndexWithAlias(final SQLSegment sqlSegment, final AliasSegment alias) {
        return null != alias && alias.getStopIndex() > sqlSegment.getStopIndex() ? alias.getStopIndex() : sqlSegment.getStopIndex();
    }
    
    @Override
    public ASTNode visitIntoClause(final IntoClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitFromClause(final FromClauseContext ctx) {
        return visit(ctx.tableReferences());
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = (TableSegment) visit(ctx.tableFactor());
        if (!ctx.joinedTable().isEmpty()) {
            for (JoinedTableContext each : ctx.joinedTable()) {
                left = visitJoinedTable(each, left);
            }
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.subquery()) {
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), getOriginalText(ctx.subquery()));
            if (null != ctx.subquery().merge()) {
                subquerySegment.setMerge((MergeStatement) visit(ctx.subquery()));
            } else {
                subquerySegment.setSelect((SelectStatement) visit(ctx.subquery()));
            }
            SubqueryTableSegment result = new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.tableName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.expr()) {
            ExpressionSegment exprSegment = (ExpressionSegment) visit(ctx.expr());
            FunctionTableSegment result = new FunctionTableSegment(exprSegment.getStartIndex(), exprSegment.getStopIndex(), exprSegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.xmlMethodCall()) {
            FunctionSegment functionSegment = (FunctionSegment) visit(ctx.xmlMethodCall());
            FunctionTableSegment result = new FunctionTableSegment(functionSegment.getStartIndex(), functionSegment.getStopIndex(), functionSegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.pivotTable()) {
            return visit(ctx.pivotTable());
        }
        if (null != ctx.rowSetFunction()) {
            FunctionSegment functionSegment = (FunctionSegment) visit(ctx.rowSetFunction());
            FunctionTableSegment result = new FunctionTableSegment(functionSegment.getStartIndex(), functionSegment.getStopIndex(), functionSegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        return visit(ctx.tableReferences());
    }
    
    @Override
    public ASTNode visitPivotTable(final PivotTableContext ctx) {
        FunctionSegment pivotFunction = (FunctionSegment) visit(ctx.pivotClause());
        FunctionTableSegment result = new FunctionTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), pivotFunction);
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitPivotClause(final PivotClauseContext ctx) {
        String pivotType = null == ctx.PIVOT() ? "UNPIVOT" : "PIVOT";
        String functionText = getOriginalText(ctx);
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), pivotType, functionText);
        if (null != ctx.tableName()) {
            SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.tableName());
            result.getParameters().add(new LiteralExpressionSegment(tableSegment.getStartIndex(), tableSegment.getStopIndex(), tableSegment.getTableName().getIdentifier().getValue()));
        } else if (null != ctx.subquery()) {
            SubquerySegment subquerySegment =
                    new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery()));
            if (null == ctx.alias()) {
                result.getParameters().add(new SubqueryExpressionSegment(subquerySegment));
            } else {
                AliasSegment aliasSegment = (AliasSegment) visit(ctx.alias());
                SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(subquerySegment.getStartIndex(), subquerySegment.getStopIndex(), subquerySegment);
                subqueryTableSegment.setAlias(aliasSegment);
                result.getParameters().add(new SubqueryExpressionSegment(subquerySegment));
            }
        }
        if (null == ctx.PIVOT()) {
            result.getParameters().add((ColumnSegment) visit(ctx.columnName(0)));
            result.getParameters().add((ColumnSegment) visit(ctx.columnName(1)));
            CollectionValue<ExpressionSegment> pivotValues = (CollectionValue<ExpressionSegment>) visit(ctx.pivotValueList());
            result.getParameters().addAll(pivotValues.getValue());
        } else {
            result.getParameters().add((ExpressionSegment) visit(ctx.aggregationFunction()));
            result.getParameters().add((ColumnSegment) visit(ctx.columnName(0)));
            CollectionValue<ExpressionSegment> pivotValues = (CollectionValue<ExpressionSegment>) visit(ctx.pivotValueList());
            result.getParameters().addAll(pivotValues.getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitPivotValueList(final PivotValueListContext ctx) {
        CollectionValue<ExpressionSegment> result = new CollectionValue<>();
        for (PivotValueContext pivotValueContext : ctx.pivotValue()) {
            result.getValue().add((ExpressionSegment) visit(pivotValueContext));
        }
        return result;
    }
    
    @Override
    public ASTNode visitPivotValue(final PivotValueContext ctx) {
        if (null != ctx.expr()) {
            return visit(ctx.expr());
        }
        return new LiteralExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
    }
    
    private JoinTableSegment visitJoinedTable(final JoinedTableContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(tableSegment);
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        TableSegment right = (TableSegment) visit(ctx.tableFactor());
        result.setRight(right);
        result.setJoinType(getJoinType(ctx));
        if (null != ctx.joinSpecification()) {
            visitJoinSpecification(ctx.joinSpecification(), result);
        }
        if (null != ctx.joinHint()) {
            result.setJoinHint(ctx.joinHint().getChild(0).getText());
        }
        return result;
    }
    
    private String getJoinType(final JoinedTableContext ctx) {
        if (null != ctx.LEFT()) {
            return JoinType.LEFT.name();
        } else if (null != ctx.RIGHT()) {
            return JoinType.RIGHT.name();
        } else if (null != ctx.FULL()) {
            return JoinType.FULL.name();
        } else if (null != ctx.INNER()) {
            return JoinType.INNER.name();
        } else if (null != ctx.CROSS()) {
            return JoinType.CROSS.name();
        }
        return JoinType.INNER.name();
    }
    
    private void visitJoinSpecification(final JoinSpecificationContext ctx, final JoinTableSegment joinTableSource) {
        if (null != ctx.expr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.expr());
            joinTableSource.setCondition(condition);
        }
        if (null != ctx.USING()) {
            joinTableSource.setUsing(ctx.columnNames().columnName().stream().map(each -> (ColumnSegment) visit(each)).collect(Collectors.toList()));
        }
    }
    
    @Override
    public ASTNode visitSubquery(final SubqueryContext ctx) {
        if (null != ctx.merge()) {
            return visit(ctx.merge());
        }
        return visit(ctx.aggregationClause());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTableAsSelectClause(final CreateTableAsSelectClauseContext ctx) {
        CreateTableStatement result = new CreateTableStatement(databaseType);
        if (null != ctx.createTableAsSelect()) {
            result.setTable((SimpleTableSegment) visit(ctx.createTableAsSelect().tableName()));
            result.setSelectStatement((SelectStatement) visit(ctx.createTableAsSelect().select()));
            if (null != ctx.createTableAsSelect().columnNames()) {
                CollectionValue<ColumnSegment> columnSegments = (CollectionValue<ColumnSegment>) visit(ctx.createTableAsSelect().columnNames());
                for (ColumnSegment each : columnSegments.getValue()) {
                    result.getColumns().add(each);
                }
            }
        } else {
            result.setTable((SimpleTableSegment) visit(ctx.createRemoteTableAsSelect().tableName()));
            result.setSelectStatement((SelectStatement) visit(ctx.createRemoteTableAsSelect().select()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdateStatistics(final UpdateStatisticsContext ctx) {
        List<IndexSegment> indexSegments = null;
        if (null != ctx.indexName() && !ctx.indexName().isEmpty()) {
            indexSegments = new LinkedList<>();
            for (IndexNameContext indexNameContext : ctx.indexName()) {
                indexSegments.add((IndexSegment) visit(indexNameContext));
            }
        }
        return new SQLServerUpdateStatisticsStatement(databaseType, null == ctx.tableName() ? null : (SimpleTableSegment) visit(ctx.tableName()),
                indexSegments, null == ctx.statisticsWithClause() ? null : (StatisticsStrategySegment) visit(ctx.statisticsWithClause()));
    }
    
    @Override
    public ASTNode visitStatisticsWithClause(final StatisticsWithClauseContext ctx) {
        StatisticsStrategySegment result = new StatisticsStrategySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.sampleOption()) {
            result.setSampleOption((SampleOptionSegment) visit(ctx.sampleOption()));
        }
        if (null != ctx.statisticsOptions()) {
            result.setStatisticsOptions((StatisticsOptionSegment) visit(ctx.statisticsOptions()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSampleOption(final SampleOptionContext ctx) {
        SampleOptionSegment result = new SampleOptionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.FULLSCAN()) {
            result.setStrategy(SampleStrategy.FULLSCAN);
        } else if (null != ctx.SAMPLE()) {
            result.setStrategy(SampleStrategy.SAMPLE);
            if (null != ctx.NUMBER_()) {
                List<TerminalNode> number = ctx.NUMBER_();
                result.setSampleNumber(number.get(0).getText());
            }
            if (null != ctx.PERCENT()) {
                result.setScanUnit(ScanUnit.PERCENT);
            } else if (null != ctx.ROWS()) {
                result.setScanUnit(ScanUnit.ROWS);
            }
        } else if (null != ctx.RESAMPLE()) {
            result.setStrategy(SampleStrategy.RESAMPLE);
            if (null != ctx.NUMBER_()) {
                List<String> partitions = new LinkedList<>();
                for (TerminalNode terminalNode : ctx.NUMBER_()) {
                    partitions.add(terminalNode.getText());
                }
                result.setPartitions(partitions);
            }
        }
        if (null != ctx.PERSIST_SAMPLE_PERCENT()) {
            result.setPersistSamplePercent(null != ctx.ON());
        }
        return result;
    }
    
    @Override
    public ASTNode visitStatisticsOptions(final StatisticsOptionsContext ctx) {
        StatisticsOptionSegment result = new StatisticsOptionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (StatisticsOptionContext option : ctx.statisticsOption()) {
            if (null != option.ALL()) {
                result.setStatisticsDimension(StatisticsDimension.ALL);
            } else if (null != option.COLUMNS()) {
                result.setStatisticsDimension(StatisticsDimension.COLUMNS);
            } else if (null != option.INDEX()) {
                result.setStatisticsDimension(StatisticsDimension.INDEX);
            }
            if (null != option.NORECOMPUTE()) {
                result.setNoRecompute(true);
            }
            if (null != option.INCREMENTAL()) {
                result.setIncremental(null != option.ON());
            }
            if (null != option.MAXDOP()) {
                result.setMaxDegreeOfParallelism(option.NUMBER_().getText());
            }
            if (null != option.AUTO_DROP()) {
                result.setAutoDrop(null != option.ON());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitMerge(final MergeContext ctx) {
        MergeStatement result = new MergeStatement(databaseType);
        result.setTarget((TableSegment) visit(ctx.mergeIntoClause().tableReferences()));
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        if (null != ctx.withMergeHint()) {
            result.setWithTableHint((WithTableHintSegment) visit(ctx.withMergeHint().withTableHint()));
            if (null != ctx.withMergeHint().indexName()) {
                Collection<IndexSegment> indexSegments = new LinkedList<>();
                for (IndexNameContext each : ctx.withMergeHint().indexName()) {
                    indexSegments.add((IndexSegment) visit(each));
                }
                result.setIndexes(indexSegments);
            }
        }
        if (null != ctx.mergeUsingClause()) {
            result.setSource((TableSegment) visit(ctx.mergeUsingClause().tableReferences()));
            ExpressionWithParamsSegment onExpression = new ExpressionWithParamsSegment(ctx.mergeUsingClause().expr().start.getStartIndex(), ctx.mergeUsingClause().expr().stop.getStopIndex(),
                    (ExpressionSegment) visit(ctx.mergeUsingClause().expr()));
            result.setExpression(onExpression);
        }
        if (null != ctx.mergeWhenClause()) {
            for (MergeWhenClauseContext each : ctx.mergeWhenClause()) {
                result.getWhenAndThens().add((MergeWhenAndThenSegment) visit(each));
            }
        }
        if (null != ctx.outputClause()) {
            result.setOutput((OutputSegment) visit(ctx.outputClause()));
        }
        if (null != ctx.optionHint()) {
            result.setOptionHint((OptionHintSegment) visit(ctx.optionHint()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitMergeWhenClause(final MergeWhenClauseContext ctx) {
        MergeWhenAndThenSegment result = new MergeWhenAndThenSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
        if (null != ctx.mergeDeleteClause() && null != ctx.mergeDeleteClause().expr()) {
            result.setAndExpr((ExpressionSegment) visit(ctx.mergeDeleteClause().expr()));
        }
        if (null != ctx.mergeUpdateClause()) {
            result.setUpdate((UpdateStatement) visit(ctx.mergeUpdateClause()));
            if (null != ctx.mergeUpdateClause().expr()) {
                result.setAndExpr((ExpressionSegment) visit(ctx.mergeUpdateClause().expr()));
            }
        }
        if (null != ctx.mergeInsertClause()) {
            result.setInsert((InsertStatement) visit(ctx.mergeInsertClause()));
            if (null != ctx.mergeInsertClause().expr()) {
                result.setAndExpr((ExpressionSegment) visit(ctx.mergeInsertClause().expr()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitMergeInsertClause(final MergeInsertClauseContext ctx) {
        InsertStatement result;
        if (null != ctx.insertDefaultValue()) {
            result = (InsertStatement) visit(ctx.insertDefaultValue());
        } else {
            result = (InsertStatement) visit(ctx.insertValuesClause());
        }
        return result;
    }
    
    @Override
    public ASTNode visitMergeUpdateClause(final MergeUpdateClauseContext ctx) {
        UpdateStatement result = new UpdateStatement(databaseType);
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        return result;
    }
}
