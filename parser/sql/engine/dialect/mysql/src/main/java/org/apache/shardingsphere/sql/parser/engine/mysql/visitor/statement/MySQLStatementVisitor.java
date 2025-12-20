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

package org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BlobValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CaseExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CaseWhenContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CastTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CharsetNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CollateClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnRefContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CompleteRegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConvertFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CteClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CurrentUserFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DuplicateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.EngineRefContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.EscapedTableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExtractFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FieldLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FieldsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GeomCollectionFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GroupByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GroupConcatFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.HavingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertSelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IntervalExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.JoinSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.JsonFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.JsonFunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.JsonTableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LimitClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LimitOffsetContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LimitRowCountContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LockClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LockClauseListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.MatchExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.MultipleTablesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NaturalJoinTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OnDuplicateKeyClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PositionFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QualifiedShorthandContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionParensContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QuerySpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReplaceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReplaceSelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReplaceValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RowAliasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RowConstructorListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ValueReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SelectSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SelectWithIntoContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SeparatorNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetRowAliasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShorthandRegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SingleTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SpecialAnalysisFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.String_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubstringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubstringParamContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SystemVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableAliasRefListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableIdentOptWildContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableValueConstructorContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TemporalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TimeStampAddFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TimeStampDiffFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TrimFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TypeDatetimePrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UdfFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UserVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ValuesFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.VariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ViewNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ViewNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WeightStringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WithClauseContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.engine.EngineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.RowAliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ValueReferenceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParenthesesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.IndexHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.TemporalLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.parametermarker.ParameterMarkerValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statement visitor for MySQL.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class MySQLStatementVisitor extends MySQLStatementBaseVisitor<ASTNode> {
    
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
        if (null != ctx.temporalLiterals()) {
            return visit(ctx.temporalLiterals());
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
        if (!ctx.string_().isEmpty()) {
            return new StringLiteralValue(ctx.string_().stream().map(each -> (StringLiteralValue) visit(each)).collect(Collectors.toList()));
        }
        return new StringLiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitString_(final String_Context ctx) {
        return new StringLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitTemporalLiterals(final TemporalLiteralsContext ctx) {
        String temporalType;
        if (null != ctx.DATE()) {
            temporalType = "DATE";
        } else if (null != ctx.TIME()) {
            temporalType = "TIME";
        } else {
            temporalType = "TIMESTAMP";
        }
        return new TemporalLiteralValue(temporalType, ctx.textString().getText());
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
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner((OwnerSegment) visit(owner));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitViewName(final ViewNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.identifier().getStart().getStartIndex(),
                ctx.identifier().getStop().getStopIndex(), new IdentifierValue(ctx.identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner((OwnerSegment) visit(owner));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitOwner(final OwnerContext ctx) {
        return new OwnerSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public ASTNode visitFunctionName(final FunctionNameContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.identifier().IDENTIFIER_().getText(), ctx.getText());
        if (null != ctx.owner()) {
            result.setOwner((OwnerSegment) visit(ctx.owner()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        return new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        IndexNameSegment indexName = new IndexNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName);
    }
    
    @Override
    public ASTNode visitTableList(final TableListContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitViewNames(final ViewNamesContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (ViewNameContext each : ctx.viewName()) {
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
    public final ASTNode visitExpr(final ExprContext ctx) {
        if (null != ctx.booleanPrimary()) {
            return visit(ctx.booleanPrimary());
        }
        if (null != ctx.XOR()) {
            return createBinaryOperationExpression(ctx, "XOR");
        }
        if (null != ctx.andOperator()) {
            return createBinaryOperationExpression(ctx, ctx.andOperator().getText());
        }
        if (null != ctx.orOperator()) {
            return createBinaryOperationExpression(ctx, ctx.orOperator().getText());
        }
        return new NotExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.expr(0)), false);
    }
    
    private BinaryOperationExpression createBinaryOperationExpression(final ExprContext ctx, final String operator) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.expr(1));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.IS()) {
            // TODO optimize operatorToken
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
        if (null != ctx.MEMBER()) {
            int startIndex = ctx.MEMBER().getSymbol().getStopIndex() + 5;
            int endIndex = ctx.stop.getStopIndex() - 1;
            String rightText = ctx.start.getInputStream().getText(new Interval(startIndex, endIndex));
            ExpressionSegment right = new ExpressionProjectionSegment(startIndex, endIndex, rightText);
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
            String operator = "MEMBER OF";
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        if (null != ctx.assignmentOperator()) {
            return createAssignmentSegment(ctx);
        }
        return visit(ctx.predicate());
    }
    
    private ASTNode createAssignmentSegment(final BooleanPrimaryContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        ExpressionSegment right = (ExpressionSegment) visit(ctx.predicate());
        String operator = ctx.assignmentOperator().getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        ExpressionSegment right;
        if (null != ctx.predicate()) {
            right = (ExpressionSegment) visit(ctx.predicate());
        } else {
            right = createRightExpression(ctx);
        }
        String operator = null == ctx.SAFE_EQ_() ? ctx.comparisonOperator().getText() : ctx.SAFE_EQ_().getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private ExpressionSegment createRightExpression(final BooleanPrimaryContext ctx) {
        SubqueryExpressionSegment result = new SubqueryExpressionSegment(
                new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery())));
        if (null != ctx.ANY() || null != ctx.ALL()) {
            int startIndex = null == ctx.ANY() ? ctx.ALL().getSymbol().getStartIndex() : ctx.ANY().getSymbol().getStartIndex();
            String quantifyOperator = null == ctx.ANY() ? ctx.ALL().getText() : ctx.ANY().getText();
            return new QuantifySubqueryExpression(startIndex, result.getStopIndex(), result.getSubquery(), quantifyOperator);
        } else {
            return result;
        }
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
        if (null != ctx.REGEXP()) {
            return createBinaryOperationExpressionFromRegexp(ctx);
        }
        if (null != ctx.RLIKE()) {
            return createBinaryOperationExpressionFromRlike(ctx);
        }
        return visit(ctx.bitExpr(0));
    }
    
    private InExpression createInSegment(final PredicateContext ctx) {
        boolean not = null != ctx.NOT();
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right;
        if (null != ctx.subquery()) {
            right = new SubqueryExpressionSegment(
                    new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery())));
        } else {
            right = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
            for (ExprContext each : ctx.expr()) {
                ((ListExpression) right).getItems().add((ExpressionSegment) visit(each));
            }
        }
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromLike(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        String operator;
        ExpressionSegment right;
        if (null != ctx.SOUNDS()) {
            right = (ExpressionSegment) visit(ctx.bitExpr(1));
            operator = "SOUNDS LIKE";
        } else {
            ListExpression listExpression = new ListExpression(ctx.simpleExpr(0).start.getStartIndex(), ctx.simpleExpr().get(ctx.simpleExpr().size() - 1).stop.getStopIndex());
            for (SimpleExprContext each : ctx.simpleExpr()) {
                listExpression.getItems().add((ExpressionSegment) visit(each));
            }
            right = listExpression;
            operator = null == ctx.NOT() ? "LIKE" : "NOT LIKE";
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromRegexp(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.bitExpr(1));
        String operator = null == ctx.NOT() ? "REGEXP" : "NOT REGEXP";
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromRlike(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.bitExpr(1));
        String operator = null == ctx.NOT() ? "RLIKE" : "NOT RLIKE";
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
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
            return visit(ctx.simpleExpr());
        }
        ExpressionSegment left = (ExpressionSegment) visit(ctx.getChild(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.getChild(2));
        String operator = ctx.getChild(1).getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        if (null != ctx.subquery()) {
            SubquerySegment subquerySegment = new SubquerySegment(
                    ctx.subquery().getStart().getStartIndex(), ctx.subquery().getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery()));
            if (null != ctx.EXISTS()) {
                subquerySegment.getSelect().setSubqueryType(SubqueryType.EXISTS);
                return new ExistsSubqueryExpression(startIndex, stopIndex, subquerySegment);
            }
            return new SubqueryExpressionSegment(subquerySegment);
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
        if (null != ctx.intervalExpression()) {
            return visit(ctx.intervalExpression());
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.collateClause()) {
            ExpressionSegment expr = null == ctx.simpleExpr() ? null : (ExpressionSegment) visit(ctx.simpleExpr(0));
            return new CollateExpression(startIndex, stopIndex, (SimpleExpressionSegment) visit(ctx.collateClause()), expr);
        }
        if (null != ctx.columnRef()) {
            return visit(ctx.columnRef());
        }
        if (null != ctx.matchExpression()) {
            return visit(ctx.matchExpression());
        }
        if (null != ctx.notOperator()) {
            ASTNode expression = visit(ctx.simpleExpr(0));
            if (expression instanceof ExistsSubqueryExpression) {
                ((ExistsSubqueryExpression) expression).setNot(true);
                return expression;
            }
            return new NotExpression(startIndex, stopIndex, (ExpressionSegment) expression, "!".equalsIgnoreCase(ctx.notOperator().getText()));
        }
        if (null != ctx.LP_() && 1 == ctx.expr().size()) {
            ASTNode result = visit(ctx.expr(0));
            if (result instanceof ColumnSegment) {
                ((ColumnSegment) result).setLeftParentheses(new ParenthesesSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.LP_().getSymbol().getStopIndex(), ctx.LP_().getSymbol().getText()));
                ((ColumnSegment) result).setRightParentheses(new ParenthesesSegment(ctx.RP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), ctx.RP_().getSymbol().getText()));
            }
            return result;
        }
        if (null != ctx.VERTICAL_BAR_() && 2 == ctx.VERTICAL_BAR_().size()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.simpleExpr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.simpleExpr(1));
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, ctx.VERTICAL_BAR_(0).getText() + ctx.VERTICAL_BAR_(1).getText(), text);
        }
        return visitRemainSimpleExpr(ctx);
    }
    
    @Override
    public ASTNode visitColumnRef(final ColumnRefContext ctx) {
        int identifierCount = ctx.identifier().size();
        ColumnSegment result;
        if (1 == identifierCount) {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier(0)));
        } else if (2 == identifierCount) {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier(1)));
            result.setOwner(new OwnerSegment(ctx.identifier(0).start.getStartIndex(), ctx.identifier(0).stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier(0))));
        } else {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier(2)));
            OwnerSegment owner = new OwnerSegment(ctx.identifier(1).start.getStartIndex(), ctx.identifier(1).stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier(1)));
            owner.setOwner(new OwnerSegment(ctx.identifier(0).start.getStartIndex(), ctx.identifier(0).stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier(0))));
            result.setOwner(owner);
        }
        return result;
    }
    
    @Override
    public ASTNode visitSubquery(final SubqueryContext ctx) {
        return visit(ctx.queryExpressionParens());
    }
    
    @Override
    public ASTNode visitQueryExpressionParens(final QueryExpressionParensContext ctx) {
        if (null != ctx.queryExpressionParens()) {
            return visit(ctx.queryExpressionParens());
        }
        SelectStatement result = (SelectStatement) visit(ctx.queryExpression());
        if (null != ctx.lockClauseList()) {
            result.setLock((LockSegment) visit(ctx.lockClauseList()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitLockClauseList(final LockClauseListContext ctx) {
        LockSegment result = new LockSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (LockClauseContext each : ctx.lockClause()) {
            if (null != each.tableLockingList()) {
                result.getTables().addAll(generateTablesFromTableAliasRefList(each.tableLockingList().tableAliasRefList()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitQueryExpression(final QueryExpressionContext ctx) {
        SelectStatement result;
        if (null != ctx.queryExpressionBody()) {
            result = (SelectStatement) visit(ctx.queryExpressionBody());
        } else {
            result = (SelectStatement) visit(ctx.queryExpressionParens());
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        if (null != result && null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelectWithInto(final SelectWithIntoContext ctx) {
        if (null != ctx.selectWithInto()) {
            return visit(ctx.selectWithInto());
        }
        SelectStatement result = (SelectStatement) visit(ctx.queryExpression());
        if (null != ctx.lockClauseList()) {
            result.setLock((LockSegment) visit(ctx.lockClauseList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWithClause(final WithClauseContext ctx) {
        Collection<CommonTableExpressionSegment> commonTableExpressions = new LinkedList<>();
        for (CteClauseContext each : ctx.cteClause()) {
            commonTableExpressions.add((CommonTableExpressionSegment) visit(each));
        }
        return new WithSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), commonTableExpressions, null != ctx.RECURSIVE());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCteClause(final CteClauseContext ctx) {
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (AliasSegment) visit(ctx.alias()),
                new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery())));
        if (null != ctx.columnNames()) {
            CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(ctx.columnNames());
            result.getColumns().addAll(columns.getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitQueryExpressionBody(final QueryExpressionBodyContext ctx) {
        if (1 == ctx.getChildCount() && ctx.getChild(0) instanceof QueryPrimaryContext) {
            return visit(ctx.queryPrimary());
        }
        if (null != ctx.queryExpressionBody() && ctx.queryExpressionBody().size() > 1) {
            SelectStatement result = new SelectStatement(databaseType);
            SubquerySegment left = new SubquerySegment(ctx.queryExpressionBody(0).start.getStartIndex(), ctx.queryExpressionBody(0).stop.getStopIndex(),
                    (SelectStatement) visit(ctx.queryExpressionBody(0)), getOriginalText(ctx.queryExpressionBody(0)));
            result.setProjections(left.getSelect().getProjections());
            left.getSelect().getFrom().ifPresent(result::setFrom);
            result.setCombine(createCombineSegment(ctx, left));
            return result;
        }
        return visit(ctx.queryExpressionParens());
    }
    
    private CombineSegment createCombineSegment(final QueryExpressionBodyContext ctx, final SubquerySegment left) {
        CombineType combineType;
        if (null != ctx.EXCEPT()) {
            combineType = null == ctx.combineOption() || null == ctx.combineOption().ALL() ? CombineType.EXCEPT : CombineType.EXCEPT_ALL;
        } else if (null != ctx.INTERSECT()) {
            combineType = null == ctx.combineOption() || null == ctx.combineOption().ALL() ? CombineType.INTERSECT : CombineType.INTERSECT_ALL;
        } else {
            combineType = null == ctx.combineOption() || null == ctx.combineOption().ALL() ? CombineType.UNION : CombineType.UNION_ALL;
        }
        ParserRuleContext ruleContext = ctx.queryExpressionBody(1);
        SubquerySegment right = new SubquerySegment(ruleContext.start.getStartIndex(), ruleContext.stop.getStopIndex(), (SelectStatement) visit(ruleContext), getOriginalText(ruleContext));
        return new CombineSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), left, combineType, right);
    }
    
    @Override
    public ASTNode visitQuerySpecification(final QuerySpecificationContext ctx) {
        SelectStatement result = new SelectStatement(databaseType);
        result.setProjections((ProjectionsSegment) visit(ctx.projections()));
        if (null != ctx.selectSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.fromClause()) {
            if (null != ctx.fromClause().tableReferences()) {
                TableSegment tableSource = (TableSegment) visit(ctx.fromClause().tableReferences());
                result.setFrom(tableSource);
            }
            if (null != ctx.fromClause().DUAL()) {
                TableSegment tableSource = new SimpleTableSegment(new TableNameSegment(ctx.fromClause().DUAL().getSymbol().getStartIndex(),
                        ctx.fromClause().DUAL().getSymbol().getStopIndex(), new IdentifierValue(ctx.fromClause().DUAL().getText())));
                result.setFrom(tableSource);
            }
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
        if (null != ctx.windowClause()) {
            result.setWindow((WindowSegment) visit(ctx.windowClause()));
        }
        if (null != ctx.lockClauseList()) {
            result.setLock((LockSegment) visit(ctx.lockClauseList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableValueConstructor(final TableValueConstructorContext ctx) {
        SelectStatement result = new SelectStatement(databaseType);
        int startIndex = ctx.getStart().getStartIndex();
        int stopIndex = ctx.getStop().getStopIndex();
        ValuesExpression valuesExpression = new ValuesExpression(startIndex, stopIndex);
        valuesExpression.getRowConstructorList().addAll(createRowConstructorList(ctx.rowConstructorList()));
        result.setProjections(new ProjectionsSegment(startIndex, stopIndex));
        result.getProjections().getProjections().add(new ExpressionProjectionSegment(startIndex, stopIndex, getOriginalText(ctx), valuesExpression));
        return result;
    }
    
    private Collection<InsertValuesSegment> createRowConstructorList(final RowConstructorListContext ctx) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (int index = 0; index < ctx.getChildCount(); index++) {
            if (!(ctx.getChild(index) instanceof AssignmentValuesContext)) {
                continue;
            }
            InsertValuesSegment valuesSegment = (InsertValuesSegment) visit(ctx.getChild(index));
            result.add(new InsertValuesSegment(((TerminalNodeImpl) ctx.getChild(index - 1)).symbol.getStartIndex(), valuesSegment.getStopIndex(), valuesSegment.getValues()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableStatement(final TableStatementContext ctx) {
        SelectStatement result = new SelectStatement(databaseType);
        result.setProjections(new ProjectionsSegment(ctx.start.getStartIndex(), ctx.start.getStartIndex()));
        result.getProjections().getProjections().add(new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.start.getStartIndex()));
        result.setFrom(new SimpleTableSegment(new TableNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.tableName().getText()))));
        return result;
    }
    
    @Override
    public ASTNode visitHavingClause(final HavingClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.expr());
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    @Override
    public final ASTNode visitIntervalExpression(final IntervalExpressionContext ctx) {
        IntervalUnit intervalUnit = IntervalUnit.valueOf(ctx.intervalValue().intervalUnit().getText().toUpperCase());
        return new IntervalExpression(ctx.INTERVAL().getSymbol().getStartIndex(), ctx.INTERVAL().getSymbol().getStopIndex(), (ExpressionSegment) visit(ctx.intervalValue().expr()), intervalUnit,
                getOriginalText(ctx));
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
        if (null != ctx.jsonFunction()) {
            return visit(ctx.jsonFunction());
        }
        if (null != ctx.udfFunction()) {
            return visit(ctx.udfFunction());
        }
        if (null != ctx.specialAnalysisFunction()) {
            return visit(ctx.specialAnalysisFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction, specialFunction, jsonFunction, udfFunction or specialAnalysisFunction.");
    }
    
    @Override
    public ASTNode visitUdfFunction(final UdfFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx), getOriginalText(ctx));
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitSpecialAnalysisFunction(final SpecialAnalysisFunctionContext ctx) {
        return null != ctx.geomCollectionFunction() ? visit(ctx.geomCollectionFunction())
                : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitGeomCollectionFunction(final GeomCollectionFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.GEOMCOLLECTION().getText(), getOriginalText(ctx));
        for (ExprContext each : ctx.expr()) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType)
                : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitSeparatorName(final SeparatorNameContext ctx) {
        return new StringLiteralValue(ctx.string_().getText());
    }
    
    @Override
    public final ASTNode visitJsonFunction(final JsonFunctionContext ctx) {
        JsonFunctionNameContext functionNameContext = ctx.jsonFunctionName();
        String functionName;
        FunctionSegment result;
        if (null != functionNameContext) {
            functionName = functionNameContext.getText();
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), functionName, getOriginalText(ctx));
            for (ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        } else if (null != ctx.JSON_SEPARATOR()) {
            result = createJsonSeparatorFunction(ctx.JSON_SEPARATOR().getText(), ctx);
        } else if (null != ctx.JSON_UNQUOTED_SEPARATOR()) {
            result = createJsonSeparatorFunction(ctx.JSON_UNQUOTED_SEPARATOR().getText(), ctx);
        } else {
            result = (FunctionSegment) visitJsonTableFunction(ctx.jsonTableFunction());
        }
        return result;
    }
    
    private FunctionSegment createJsonSeparatorFunction(final String functionName, final JsonFunctionContext ctx) {
        FunctionSegment result;
        result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), functionName, getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.columnRef()));
        result.getParameters().add(new LiteralExpressionSegment(ctx.path().getStart().getStartIndex(), ctx.path().getStop().getStopIndex(), ctx.path().getText()));
        return result;
    }
    
    @Override
    public final ASTNode visitJsonTableFunction(final JsonTableFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.JSON_TABLE().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        result.getParameters().add(new LiteralExpressionSegment(ctx.path().getStart().getStartIndex(), ctx.path().getStop().getStopIndex(), getOriginalText(ctx.path())));
        result.getParameters().add(new LiteralExpressionSegment(ctx.jsonTableColumns().getStart().getStartIndex(), ctx.jsonTableColumns().getStop().getStopIndex(),
                getOriginalText(ctx.jsonTableColumns())));
        return result;
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        String separator = null;
        if (null != ctx.separatorName()) {
            separator = new StringLiteralValue(ctx.separatorName().string_().getText()).getValue();
        }
        if (null != ctx.distinct()) {
            AggregationDistinctProjectionSegment result =
                    new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx), getDistinctExpression(ctx), separator);
            result.getParameters().addAll(getExpressions(ctx.aggregationExpression().expr()));
            return result;
        }
        AggregationProjectionSegment result = new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx), separator);
        result.getParameters().addAll(getExpressions(ctx.aggregationExpression().expr()));
        return result;
    }
    
    protected Collection<ExpressionSegment> getExpressions(final List<ExprContext> exprList) {
        if (null == exprList) {
            return Collections.emptyList();
        }
        Collection<ExpressionSegment> result = new ArrayList<>(exprList.size());
        for (ExprContext each : exprList) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    private String getDistinctExpression(final AggregationFunctionContext ctx) {
        return ctx.aggregationExpression().getText();
    }
    
    @Override
    public final ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.groupConcatFunction()) {
            return visit(ctx.groupConcatFunction());
        }
        if (null != ctx.windowFunction()) {
            return visit(ctx.windowFunction());
        }
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.convertFunction()) {
            return visit(ctx.convertFunction());
        }
        if (null != ctx.positionFunction()) {
            return visit(ctx.positionFunction());
        }
        if (null != ctx.substringFunction()) {
            return visit(ctx.substringFunction());
        }
        if (null != ctx.extractFunction()) {
            return visit(ctx.extractFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        if (null != ctx.trimFunction()) {
            return visit(ctx.trimFunction());
        }
        if (null != ctx.weightStringFunction()) {
            return visit(ctx.weightStringFunction());
        }
        if (null != ctx.valuesFunction()) {
            return visit(ctx.valuesFunction());
        }
        if (null != ctx.currentUserFunction()) {
            return visit(ctx.currentUserFunction());
        }
        if (null != ctx.timeStampDiffFunction()) {
            return visit(ctx.timeStampDiffFunction());
        }
        if (null != ctx.timeStampAddFunction()) {
            return visit(ctx.timeStampAddFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx), getOriginalText(ctx));
    }
    
    @Override
    public final ASTNode visitGroupConcatFunction(final GroupConcatFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.GROUP_CONCAT().getText(), getOriginalText(ctx));
        for (ExprContext each : getTargetRuleContextFromParseTree(ctx, ExprContext.class)) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    private <T extends ParseTree> Collection<T> getTargetRuleContextFromParseTree(final ParseTree parseTree, final Class<? extends T> clazz) {
        Collection<T> result = new LinkedList<>();
        for (int index = 0; index < parseTree.getChildCount(); index++) {
            ParseTree child = parseTree.getChild(index);
            if (clazz.isInstance(child)) {
                result.add(clazz.cast(child));
            } else {
                result.addAll(getTargetRuleContextFromParseTree(child, clazz));
            }
        }
        return result;
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CAST().getText(), getOriginalText(ctx));
        for (ExprContext each : ctx.expr()) {
            ASTNode expr = visit(each);
            result.getParameters().add((ExpressionSegment) expr);
        }
        if (null != ctx.castType()) {
            result.getParameters().add((DataTypeSegment) visit(ctx.castType()));
        }
        if (null != ctx.DATETIME()) {
            DataTypeSegment dataType = new DataTypeSegment();
            dataType.setDataTypeName(ctx.DATETIME().getText());
            dataType.setStartIndex(ctx.DATETIME().getSymbol().getStartIndex());
            dataType.setStopIndex(ctx.DATETIME().getSymbol().getStopIndex());
            if (null != ctx.typeDatetimePrecision()) {
                dataType.setDataLength((DataTypeLengthSegment) visit(ctx.typeDatetimePrecision()));
            }
            result.getParameters().add(dataType);
        }
        return result;
    }
    
    @Override
    public ASTNode visitCastType(final CastTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setDataTypeName(ctx.castTypeName.getText());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.fieldLength()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.fieldLength());
            result.setDataLength(dataTypeLengthSegment);
        }
        if (null != ctx.precision()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.precision());
            result.setDataLength(dataTypeLengthSegment);
        }
        return result;
    }
    
    @Override
    public final ASTNode visitConvertFunction(final ConvertFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CONVERT().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        if (null != ctx.castType()) {
            result.getParameters().add((DataTypeSegment) visit(ctx.castType()));
        } else if (null != ctx.charsetName()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.charsetName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCharsetName(final CharsetNameContext ctx) {
        String charsetName = "";
        if (null != ctx.textOrIdentifier() && null != ctx.textOrIdentifier().getText()) {
            charsetName = ctx.textOrIdentifier().getText();
        } else if (null != ctx.BINARY()) {
            charsetName = ctx.BINARY().getText();
        } else if (null != ctx.DEFAULT()) {
            charsetName = ctx.DEFAULT().getText();
        }
        return new LiteralExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), charsetName);
    }
    
    @Override
    public final ASTNode visitPositionFunction(final PositionFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.POSITION().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr(0)));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr(1)));
        return result;
    }
    
    @Override
    public final ASTNode visitSubstringFunction(final SubstringFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getFunctionName(ctx), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        for (SubstringParamContext each : ctx.substringParam()) {
            if (null != each.numberLiterals()) {
                result.getParameters()
                        .add(new LiteralExpressionSegment(each.numberLiterals().start.getStartIndex(), each.numberLiterals().stop.getStopIndex(), new NumberLiteralValue(each.getText()).getValue()));
            } else if (null != each.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each.expr()));
            }
        }
        return result;
    }
    
    @Override
    public final ASTNode visitExtractFunction(final ExtractFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.EXTRACT().getText(), getOriginalText(ctx));
        result.getParameters().add(new LiteralExpressionSegment(ctx.intervalUnit().getStart().getStartIndex(), ctx.intervalUnit().getStop().getStopIndex(), ctx.intervalUnit().getText()));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public final ASTNode visitCharFunction(final CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHAR().getText(), getOriginalText(ctx));
        for (ExprContext each : ctx.expr()) {
            ASTNode expr = visit(each);
            result.getParameters().add((ExpressionSegment) expr);
        }
        return result;
    }
    
    @Override
    public final ASTNode visitTrimFunction(final TrimFunctionContext ctx) {
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
    public final ASTNode visitWeightStringFunction(final WeightStringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.WEIGHT_STRING().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public final ASTNode visitValuesFunction(final ValuesFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.VALUES().getText(), getOriginalText(ctx));
        if (!ctx.columnRefList().columnRef().isEmpty()) {
            ColumnSegment columnSegment = (ColumnSegment) visit(ctx.columnRefList().columnRef(0));
            result.getParameters().add(columnSegment);
        }
        return result;
    }
    
    @Override
    public final ASTNode visitCurrentUserFunction(final CurrentUserFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CURRENT_USER().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitTimeStampAddFunction(final TimeStampAddFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TIMESTAMPADD().getText(), getOriginalText(ctx));
        result.getParameters().add(new LiteralExpressionSegment(ctx.intervalUnit().getStart().getStartIndex(), ctx.intervalUnit().getStop().getStopIndex(), ctx.intervalUnit().getText()));
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitTimeStampDiffFunction(final TimeStampDiffFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TIMESTAMPDIFF().getText(), getOriginalText(ctx));
        result.getParameters().add(new LiteralExpressionSegment(ctx.intervalUnit().getStart().getStartIndex(), ctx.intervalUnit().getStop().getStopIndex(), ctx.intervalUnit().getText()));
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        return null == ctx.completeRegularFunction() ? visit(ctx.shorthandRegularFunction()) : visit(ctx.completeRegularFunction());
    }
    
    @Override
    public ASTNode visitCompleteRegularFunction(final CompleteRegularFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.regularFunctionName().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitShorthandRegularFunction(final ShorthandRegularFunctionContext ctx) {
        String text = getOriginalText(ctx);
        FunctionSegment result;
        if (null != ctx.CURRENT_TIME()) {
            result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.CURRENT_TIME().getText(), text);
            if (null != ctx.NUMBER_()) {
                result.getParameters().add(new LiteralExpressionSegment(ctx.NUMBER_().getSymbol().getStartIndex(), ctx.NUMBER_().getSymbol().getStopIndex(),
                        new NumberLiteralValue(ctx.NUMBER_().getText())));
            }
        } else {
            result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText(), text);
        }
        return result;
    }
    
    private ASTNode visitRemainSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.caseExpression()) {
            return visit(ctx.caseExpression());
        }
        if (null != ctx.BINARY()) {
            return new UnaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.simpleExpr(0)), ctx.BINARY().getText(), ctx.getText());
        }
        if (null != ctx.TILDE_()) {
            return new UnaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.simpleExpr(0)), "~", ctx.getText());
        }
        if (null != ctx.variable()) {
            return visit(ctx.variable());
        }
        if (null != ctx.LP_()) {
            RowExpression result = new RowExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
            for (ExprContext each : ctx.expr()) {
                result.getItems().add((ExpressionSegment) visit(each));
            }
            return result;
        }
        if (null != ctx.RETURNING()) {
            ListExpression result = new ListExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
            result.getItems().add(new LiteralExpressionSegment(ctx.path().start.getStartIndex(), ctx.path().stop.getStopIndex(), ctx.path().getText()));
            result.getItems().add(new LiteralExpressionSegment(ctx.RETURNING().getSymbol().getStartIndex(), ctx.RETURNING().getSymbol().getStopIndex(), ctx.RETURNING().getSymbol().getText()));
            result.getItems().add((ExpressionSegment) visit(ctx.dataType()));
            return result;
        }
        if (null != ctx.LBE_()) {
            return visit(ctx.expr(0));
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
    public ASTNode visitCaseExpression(final CaseExpressionContext ctx) {
        Collection<ExpressionSegment> whenExprs = new LinkedList<>();
        Collection<ExpressionSegment> thenExprs = new LinkedList<>();
        for (CaseWhenContext each : ctx.caseWhen()) {
            whenExprs.add((ExpressionSegment) visit(each.expr(0)));
            thenExprs.add((ExpressionSegment) visit(each.expr(1)));
        }
        ExpressionSegment caseExpr = null == ctx.expr() ? null : (ExpressionSegment) visit(ctx.expr());
        ExpressionSegment elseExpr = null == ctx.caseElse() ? null : (ExpressionSegment) visit(ctx.caseElse().expr());
        return new CaseWhenExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), caseExpr, whenExprs, thenExprs, elseExpr, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitVariable(final VariableContext ctx) {
        return null == ctx.systemVariable() ? visit(ctx.userVariable()) : visit(ctx.systemVariable());
    }
    
    @Override
    public ASTNode visitUserVariable(final UserVariableContext ctx) {
        return new VariableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.textOrIdentifier().getText());
    }
    
    @Override
    public ASTNode visitSystemVariable(final SystemVariableContext ctx) {
        VariableSegment result = new VariableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.rvalueSystemVariable().getText());
        if (null != ctx.systemVariableScope) {
            result.setScope(ctx.systemVariableScope.getText());
        }
        return result;
    }
    
    @Override
    public final ASTNode visitMatchExpression(final MatchExpressionContext ctx) {
        ExpressionSegment expressionSegment = (ExpressionSegment) visit(ctx.expr());
        MatchAgainstExpression result = new MatchAgainstExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), expressionSegment, getOriginalText(ctx.matchSearchModifier()),
                getOriginalText(ctx));
        for (ColumnRefContext each : ctx.columnRefList().columnRef()) {
            result.getColumns().add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
    }
    
    @Override
    public final ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setDataTypeName(ctx.dataTypeName.getText());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.fieldLength()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.fieldLength());
            result.setDataLength(dataTypeLengthSegment);
        }
        if (null != ctx.precision()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.precision());
            result.setDataLength(dataTypeLengthSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitFieldLength(final FieldLengthContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        result.setPrecision(new BigDecimal(ctx.length.getText()).intValue());
        return result;
    }
    
    @Override
    public ASTNode visitPrecision(final PrecisionContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        List<TerminalNode> numbers = ctx.NUMBER_();
        result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        result.setScale(Integer.parseInt(numbers.get(1).getText()));
        return result;
    }
    
    @Override
    public ASTNode visitTypeDatetimePrecision(final TypeDatetimePrecisionContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        result.setPrecision(Integer.parseInt(ctx.NUMBER_().getText()));
        return result;
    }
    
    @Override
    public final ASTNode visitOrderByClause(final OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new OrderBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public final ASTNode visitOrderByItem(final OrderByItemContext ctx) {
        OrderDirection orderDirection;
        if (null != ctx.direction()) {
            orderDirection = null == ctx.direction().DESC() ? OrderDirection.ASC : OrderDirection.DESC;
        } else {
            orderDirection = OrderDirection.ASC;
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtils.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection, null);
        } else {
            ASTNode expr = visitExpr(ctx.expr());
            if (expr instanceof ColumnSegment) {
                return new ColumnOrderByItemSegment((ColumnSegment) expr, orderDirection, null);
            } else {
                return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(),
                        ctx.expr().getStop().getStopIndex(), getOriginalText(ctx.expr()), orderDirection, null, (ExpressionSegment) expr);
            }
        }
    }
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        InsertStatement result = (InsertStatement) visit(ctx.insertBody());
        if (null != ctx.onDuplicateKeyClause()) {
            result.setOnDuplicateKeyColumns((OnDuplicateKeyColumnsSegment) visit(ctx.onDuplicateKeyClause()));
        }
        result.setIgnore(null != ctx.insertSpecification().IGNORE());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.addParameterMarkers(getParameterMarkerSegments());
        if (null != ctx.returningClause()) {
            result.setReturning((ReturningSegment) visit(ctx.returningClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertBody(final InsertBodyContext ctx) {
        InsertStatement result;
        if (null != ctx.insertValuesClause()) {
            result = (InsertStatement) visit(ctx.insertValuesClause());
        } else if (null != ctx.insertSelectClause()) {
            result = (InsertStatement) visit(ctx.insertSelectClause());
            if (null != ctx.valueReference()) {
                result.setValueReference((ValueReferenceSegment) visit(ctx.valueReference()));
            }
        } else {
            result = new InsertStatement(databaseType);
            result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
            if (null != ctx.valueReference()) {
                result.setValueReference((ValueReferenceSegment) visit(ctx.valueReference()));
            }
            if (null == ctx.valueReference() && null != ctx.setAssignmentsClause().setRowAlias()) {
                result.setValueReference(createValueReferenceFromSetRowAlias(ctx.setAssignmentsClause().setRowAlias()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertSelectClause(final InsertSelectClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        result.setInsertSelect(createInsertSelectSegment(ctx));
        if (null != ctx.LP_() && !ctx.LP_().isEmpty()) {
            if (null != ctx.fields()) {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().get(0).getSymbol().getStartIndex(), ctx.RP_().get(0).getSymbol().getStopIndex(), createInsertColumns(ctx.fields())));
            } else {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().get(0).getSymbol().getStartIndex(), ctx.RP_().get(0).getSymbol().getStopIndex(), Collections.emptyList()));
            }
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        return result;
    }
    
    private ValueReferenceSegment createValueReferenceFromSetRowAlias(final SetRowAliasContext ctx) {
        AliasSegment alias = (AliasSegment) visit(ctx.alias());
        Collection<ColumnSegment> derivedColumns = null;
        if (null != ctx.derivedColumns()) {
            derivedColumns = new LinkedList<>();
            for (AliasContext each : ctx.derivedColumns().alias()) {
                AliasSegment aliasSegment = (AliasSegment) visit(each);
                ColumnSegment column = new ColumnSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), aliasSegment.getIdentifier());
                derivedColumns.add(column);
            }
        }
        return new ValueReferenceSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), alias, derivedColumns);
    }
    
    private SubquerySegment createInsertSelectSegment(final InsertSelectClauseContext ctx) {
        SelectStatement selectStatement = (SelectStatement) visit(ctx.select());
        selectStatement.addParameterMarkers(getParameterMarkerSegments());
        return new SubquerySegment(ctx.select().start.getStartIndex(), ctx.select().stop.getStopIndex(), selectStatement, getOriginalText(ctx.select()));
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        if (null != ctx.LP_()) {
            if (null != ctx.fields()) {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), createInsertColumns(ctx.fields())));
            } else {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), Collections.emptyList()));
            }
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        if (null != ctx.valueReference()) {
            ValueReferenceSegment valueRef = (ValueReferenceSegment) visit(ctx.valueReference());
            result.setValueReference(valueRef);
        }
        Collection<InsertValuesSegment> insertValuesSegments =
                null == ctx.rowConstructorList() ? createInsertValuesSegments(ctx.assignmentValues()) : createRowConstructorList(ctx.rowConstructorList());
        result.getValues().addAll(insertValuesSegments);
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
    public ASTNode visitRowAlias(final RowAliasContext ctx) {
        AliasSegment alias = (AliasSegment) visit(ctx.alias());
        Collection<ColumnSegment> derivedColumns = null;
        if (null != ctx.derivedColumns()) {
            derivedColumns = new LinkedList<>();
            for (AliasContext each : ctx.derivedColumns().alias()) {
                AliasSegment aliasSegment = (AliasSegment) visit(each);
                ColumnSegment column = new ColumnSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), aliasSegment.getIdentifier());
                derivedColumns.add(column);
            }
        }
        return new RowAliasSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), alias, derivedColumns);
    }
    
    @Override
    public ASTNode visitValueReference(final ValueReferenceContext ctx) {
        AliasSegment alias = (AliasSegment) visit(ctx.alias());
        Collection<ColumnSegment> derivedColumns = null;
        if (null != ctx.derivedColumns()) {
            derivedColumns = new LinkedList<>();
            for (AliasContext each : ctx.derivedColumns().alias()) {
                AliasSegment aliasSegment = (AliasSegment) visit(each);
                ColumnSegment column = new ColumnSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), aliasSegment.getIdentifier());
                derivedColumns.add(column);
            }
        }
        return new ValueReferenceSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), alias, derivedColumns);
    }
    
    @Override
    public ASTNode visitOnDuplicateKeyClause(final OnDuplicateKeyClauseContext ctx) {
        Collection<ColumnAssignmentSegment> columns = new LinkedList<>();
        for (AssignmentContext each : ctx.assignment()) {
            columns.add((ColumnAssignmentSegment) visit(each));
        }
        return new OnDuplicateKeyColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    @Override
    public ASTNode visitReplace(final ReplaceContext ctx) {
        // TODO :FIXME, since there is no segment for replaceValuesClause, ReplaceStatement is created by sub rule.
        InsertStatement result;
        if (null != ctx.replaceValuesClause()) {
            result = (InsertStatement) visit(ctx.replaceValuesClause());
        } else if (null != ctx.replaceSelectClause()) {
            result = (InsertStatement) visit(ctx.replaceSelectClause());
        } else {
            result = new InsertStatement(databaseType);
            result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        }
        result.setReplace(true);
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.addParameterMarkers(getParameterMarkerSegments());
        if (null != ctx.returningClause()) {
            result.setReturning((ReturningSegment) visit(ctx.returningClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitReplaceSelectClause(final ReplaceSelectClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        if (null != ctx.LP_()) {
            if (null != ctx.fields()) {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), createInsertColumns(ctx.fields())));
            } else {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), Collections.emptyList()));
            }
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        result.setInsertSelect(createReplaceSelectSegment(ctx));
        return result;
    }
    
    private SubquerySegment createReplaceSelectSegment(final ReplaceSelectClauseContext ctx) {
        SelectStatement selectStatement = (SelectStatement) visit(ctx.select());
        return new SubquerySegment(ctx.select().start.getStartIndex(), ctx.select().stop.getStopIndex(), selectStatement, getOriginalText(ctx.select()));
    }
    
    @Override
    public ASTNode visitReplaceValuesClause(final ReplaceValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        if (null != ctx.LP_()) {
            if (null != ctx.fields()) {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), createInsertColumns(ctx.fields())));
            } else {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), Collections.emptyList()));
            }
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        result.getValues().addAll(createInsertValuesSegments(ctx.assignmentValues()));
        return result;
    }
    
    private List<ColumnSegment> createInsertColumns(final FieldsContext fields) {
        List<ColumnSegment> result = new LinkedList<>();
        for (InsertIdentifierContext each : fields.insertIdentifier()) {
            result.add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement(databaseType);
        TableSegment tableSegment = (TableSegment) visit(ctx.tableReferences());
        result.setTable(tableSegment);
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSetAssignmentsClause(final SetAssignmentsClauseContext ctx) {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentContext each : ctx.assignmentList().assignment()) {
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
        ColumnSegment column = (ColumnSegment) visit(ctx.columnRef());
        ExpressionSegment value = (ExpressionSegment) visit(ctx.assignmentValue());
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(column);
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
    public ASTNode visitBlobValue(final BlobValueContext ctx) {
        return new StringLiteralValue(ctx.string_().getText());
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement(databaseType);
        if (null != ctx.multipleTablesClause()) {
            result.setTable((TableSegment) visit(ctx.multipleTablesClause()));
        } else {
            result.setTable((TableSegment) visit(ctx.singleTableClause()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        if (null != ctx.returningClause()) {
            result.setReturning((ReturningSegment) visit(ctx.returningClause()));
        }
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause(final SingleTableClauseContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitMultipleTablesClause(final MultipleTablesClauseContext ctx) {
        DeleteMultiTableSegment result = new DeleteMultiTableSegment();
        TableSegment relateTableSource = (TableSegment) visit(ctx.tableReferences());
        result.setRelationTable(relateTableSource);
        result.setActualDeleteTables(generateTablesFromTableAliasRefList(ctx.tableAliasRefList()));
        return result;
    }
    
    private List<SimpleTableSegment> generateTablesFromTableAliasRefList(final TableAliasRefListContext ctx) {
        List<SimpleTableSegment> result = new LinkedList<>();
        for (TableIdentOptWildContext each : ctx.tableIdentOptWild()) {
            result.add((SimpleTableSegment) visit(each.tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        SelectStatement result;
        if (null != ctx.queryExpression()) {
            result = (SelectStatement) visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                result.setLock((LockSegment) visit(ctx.lockClauseList()));
            }
        } else if (null != ctx.selectWithInto()) {
            result = (SelectStatement) visit(ctx.selectWithInto());
        } else {
            result = (SelectStatement) visit(ctx.getChild(0));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    private boolean isDistinct(final QuerySpecificationContext ctx) {
        for (SelectSpecificationContext each : ctx.selectSpecification()) {
            if (((BooleanLiteralValue) visit(each)).getValue()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ASTNode visitSelectSpecification(final SelectSpecificationContext ctx) {
        if (null != ctx.duplicateSpecification()) {
            return visit(ctx.duplicateSpecification());
        }
        return new BooleanLiteralValue(false);
    }
    
    @Override
    public ASTNode visitDuplicateSpecification(final DuplicateSpecificationContext ctx) {
        String text = ctx.getText();
        if ("DISTINCT".equalsIgnoreCase(text) || "DISTINCTROW".equalsIgnoreCase(text)) {
            return new BooleanLiteralValue(true);
        }
        return new BooleanLiteralValue(false);
    }
    
    @Override
    public ASTNode visitProjections(final ProjectionsContext ctx) {
        Collection<ProjectionSegment> projections = new LinkedList<>();
        if (null != ctx.unqualifiedShorthand()) {
            projections.add(new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex()));
        }
        for (ProjectionContext each : ctx.projection()) {
            projections.add((ProjectionSegment) visit(each));
        }
        ProjectionsSegment result = new ProjectionsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Override
    public ASTNode visitProjection(final ProjectionContext ctx) {
        // FIXME :The stop index of project is the stop index of projection, instead of alias.
        if (null != ctx.qualifiedShorthand()) {
            return createShorthandProjection(ctx.qualifiedShorthand());
        }
        AliasSegment alias = null == ctx.alias() ? null : (AliasSegment) visit(ctx.alias());
        ExpressionSegment exprProjection = (ExpressionSegment) visit(ctx.expr());
        if (exprProjection instanceof ColumnSegment) {
            ColumnProjectionSegment result = new ColumnProjectionSegment((ColumnSegment) exprProjection);
            result.setAlias(alias);
            return result;
        }
        if (exprProjection instanceof SubquerySegment) {
            SubquerySegment subquerySegment = (SubquerySegment) exprProjection;
            String text = ctx.start.getInputStream().getText(new Interval(subquerySegment.getStartIndex(), subquerySegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment((SubquerySegment) exprProjection, text);
            result.setAlias(alias);
            return result;
        }
        return createProjection(ctx, alias, exprProjection);
    }
    
    private ShorthandProjectionSegment createShorthandProjection(final QualifiedShorthandContext shorthand) {
        ShorthandProjectionSegment result = new ShorthandProjectionSegment(shorthand.getStart().getStartIndex(), shorthand.getStop().getStopIndex());
        IdentifierContext identifier = shorthand.identifier().get(shorthand.identifier().size() - 1);
        OwnerSegment owner = new OwnerSegment(identifier.getStart().getStartIndex(), identifier.getStop().getStopIndex(), new IdentifierValue(identifier.getText()));
        result.setOwner(owner);
        if (shorthand.identifier().size() > 1) {
            IdentifierContext databaseIdentifier = shorthand.identifier().get(0);
            owner.setOwner(new OwnerSegment(databaseIdentifier.getStart().getStartIndex(), databaseIdentifier.getStop().getStopIndex(), new IdentifierValue(databaseIdentifier.getText())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlias(final AliasContext ctx) {
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.textOrIdentifier().getText()));
    }
    
    private ASTNode createProjection(final ProjectionContext ctx, final AliasSegment alias, final ExpressionSegment projection) {
        if (projection instanceof AggregationProjectionSegment) {
            ((AggregationProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof ExpressionProjectionSegment) {
            ((ExpressionProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof FunctionSegment) {
            FunctionSegment functionSegment = (FunctionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(functionSegment.getStartIndex(), functionSegment.getStopIndex(), functionSegment.getText(), functionSegment);
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
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryExpressionSegment subqueryExpressionSegment = (SubqueryExpressionSegment) projection;
            String text = ctx.start.getInputStream().getText(new Interval(subqueryExpressionSegment.getStartIndex(), subqueryExpressionSegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(subqueryExpressionSegment.getSubquery(), text);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof BinaryOperationExpression) {
            int startIndex = projection.getStartIndex();
            int stopIndex = null == alias ? projection.getStopIndex() : alias.getStopIndex();
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, projection.getText(), projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof ParameterMarkerExpressionSegment) {
            ParameterMarkerExpressionSegment result = (ParameterMarkerExpressionSegment) projection;
            result.setAlias(alias);
            return projection;
        }
        if (projection instanceof CaseWhenExpression || projection instanceof VariableSegment || projection instanceof BetweenExpression || projection instanceof InExpression
                || projection instanceof CollateExpression || projection instanceof NotExpression || projection instanceof ExistsSubqueryExpression) {
            return createExpressionProjectionSegment(ctx, alias, projection);
        }
        ExpressionProjectionSegment result = null == alias
                ? new ExpressionProjectionSegment(projection.getStartIndex(), projection.getStopIndex(), String.valueOf(projection.getText()), projection)
                : new ExpressionProjectionSegment(projection.getStartIndex(), ctx.alias().stop.getStopIndex(), String.valueOf(projection.getText()), projection);
        result.setAlias(alias);
        return result;
    }
    
    private ExpressionProjectionSegment createExpressionProjectionSegment(final ProjectionContext ctx, final AliasSegment alias, final ExpressionSegment projection) {
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx.expr()), projection);
        result.setAlias(alias);
        return result;
    }
    
    @Override
    public ASTNode visitFromClause(final FromClauseContext ctx) {
        return visit(ctx.tableReferences());
    }
    
    @Override
    public ASTNode visitTableReferences(final TableReferencesContext ctx) {
        TableSegment result = (TableSegment) visit(ctx.tableReference(0));
        if (ctx.tableReference().size() > 1) {
            for (int i = 1; i < ctx.tableReference().size(); i++) {
                result = generateJoinTableSourceFromEscapedTableReference(ctx.tableReference(i), result);
            }
        }
        return result;
    }
    
    private JoinTableSegment generateJoinTableSourceFromEscapedTableReference(final TableReferenceContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft(tableSegment);
        result.setJoinType(JoinType.COMMA.name());
        result.setRight((TableSegment) visit(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitEscapedTableReference(final EscapedTableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = (TableSegment) visit(ctx.tableFactor());
        for (JoinedTableContext each : ctx.joinedTable()) {
            left = visitJoinedTable(each, left);
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = null == ctx.tableFactor() ? (TableSegment) visit(ctx.escapedTableReference()) : (TableSegment) visit(ctx.tableFactor());
        for (JoinedTableContext each : ctx.joinedTable()) {
            left = visitJoinedTable(each, left);
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.subquery()) {
            SelectStatement subquery = (SelectStatement) visit(ctx.subquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery, getOriginalText(ctx.subquery()));
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
            if (null != ctx.indexHintList()) {
                ctx.indexHintList().indexHint().forEach(each -> result.getIndexHintSegments().add((IndexHintSegment) visit(each)));
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
        return visit(ctx.tableReferences());
    }
    
    private JoinTableSegment visitJoinedTable(final JoinedTableContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(tableSegment);
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setJoinType(getJoinType(ctx));
        result.setNatural(null != ctx.naturalJoinType());
        TableSegment right = null == ctx.tableFactor() ? (TableSegment) visit(ctx.tableReference()) : (TableSegment) visit(ctx.tableFactor());
        result.setRight(right);
        return null == ctx.joinSpecification() ? result : visitJoinSpecification(ctx.joinSpecification(), result);
    }
    
    private String getJoinType(final JoinedTableContext ctx) {
        if (null != ctx.innerJoinType()) {
            return JoinType.INNER.name();
        }
        if (null != ctx.outerJoinType()) {
            return null == ctx.outerJoinType().LEFT() ? JoinType.RIGHT.name() : JoinType.LEFT.name();
        }
        if (null != ctx.naturalJoinType()) {
            return getNaturalJoinType(ctx.naturalJoinType());
        }
        return JoinType.COMMA.name();
    }
    
    private String getNaturalJoinType(final NaturalJoinTypeContext ctx) {
        if (null != ctx.LEFT()) {
            return JoinType.LEFT.name();
        }
        if (null != ctx.RIGHT()) {
            return JoinType.RIGHT.name();
        }
        return JoinType.INNER.name();
    }
    
    private JoinTableSegment visitJoinSpecification(final JoinSpecificationContext ctx, final JoinTableSegment result) {
        if (null != ctx.expr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.expr());
            result.setCondition(condition);
        }
        if (null != ctx.USING()) {
            result.setUsing(ctx.columnNames().columnName().stream().map(each -> (ColumnSegment) visit(each)).collect(Collectors.toList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        ASTNode segment = visit(ctx.expr());
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) segment);
    }
    
    @Override
    public ASTNode visitGroupByClause(final GroupByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new GroupBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public ASTNode visitLimitClause(final LimitClauseContext ctx) {
        if (null == ctx.limitOffset()) {
            return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, (PaginationValueSegment) visit(ctx.limitRowCount()));
        }
        PaginationValueSegment rowCount;
        PaginationValueSegment offset;
        if (null != ctx.OFFSET()) {
            rowCount = (PaginationValueSegment) visit(ctx.limitRowCount());
            offset = (PaginationValueSegment) visit(ctx.limitOffset());
        } else {
            offset = (PaginationValueSegment) visit(ctx.limitOffset());
            rowCount = (PaginationValueSegment) visit(ctx.limitRowCount());
        }
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, rowCount);
    }
    
    @Override
    public ASTNode visitLimitRowCount(final LimitRowCountContext ctx) {
        if (null != ctx.numberLiterals()) {
            return new NumberLiteralLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((NumberLiteralValue) visit(ctx.numberLiterals())).getValue().longValue());
        }
        ParameterMarkerSegment result = new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        parameterMarkerSegments.add(result);
        return result;
    }
    
    @Override
    public final ASTNode visitConstraintName(final ConstraintNameContext ctx) {
        return new ConstraintSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public ASTNode visitLimitOffset(final LimitOffsetContext ctx) {
        if (null != ctx.numberLiterals()) {
            return new NumberLiteralLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((NumberLiteralValue) visit(ctx.numberLiterals())).getValue().longValue());
        }
        ParameterMarkerSegment result = new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        parameterMarkerSegments.add(result);
        return result;
    }
    
    @Override
    public ASTNode visitCollateClause(final CollateClauseContext ctx) {
        if (null != ctx.collationName()) {
            return new LiteralExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.collationName().textOrIdentifier().getText());
        }
        ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        parameterMarkerSegments.add(segment);
        return segment;
    }
    
    @Override
    public ASTNode visitEngineRef(final EngineRefContext ctx) {
        return new EngineSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), SQLUtils.getExactlyValue(ctx.textOrIdentifier().getText()));
    }
    
    private static String getFunctionName(final SubstringFunctionContext ctx) {
        if (null == ctx.SUBSTR()) {
            return null == ctx.SUBSTRING() ? ctx.MID().getText() : ctx.SUBSTRING().getText();
        }
        return ctx.SUBSTR().getText();
    }
    
    protected String getOriginalText(final ParserRuleContext ctx) {
        return null == ctx ? "" : ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
}
