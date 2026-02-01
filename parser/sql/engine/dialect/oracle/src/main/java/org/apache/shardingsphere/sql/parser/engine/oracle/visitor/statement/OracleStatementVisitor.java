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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AnalyticFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ApproxRankContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CaseExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CaseWhenContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CursorFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DateTimeLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DatetimeExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExtractFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FeatureFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FirstOrLastValueFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FormatFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IntervalDayToSecondExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IntervalExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IntervalYearToMonthExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.JsonObjectFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.JsonObjectKeyValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PackageNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PredictionCostFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PrivateExprOfDbContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SynonymNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ToDateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TranslateFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TrimFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ViewNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.WmConcatFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlAggFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlCdataFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlColattvalFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlElementFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlExistsFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlForestFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlIsSchemaValidFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlNamespaceStringAsIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlNamespacesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlParseFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlPiFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlQueryFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlRootFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlSerializeFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlTableColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlTableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlTableOptionsContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SequenceFunction;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalDayToSecondExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalYearToMonthExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.multiset.MultisetExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlElementFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlNamespaceStringAsIdentifierSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlNamespacesClauseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlPiFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlSerializeFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableOptionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.CursorForLoopStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureBodyEndNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.DateTimeLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.parametermarker.ParameterMarkerValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Statement visitor for Oracle.
 */
@RequiredArgsConstructor
@Getter
public abstract class OracleStatementVisitor extends OracleStatementBaseVisitor<ASTNode> {
    
    private final DatabaseType databaseType;
    
    private final Collection<ParameterMarkerSegment> globalParameterMarkerSegments = new LinkedList<>();
    
    private final Collection<ParameterMarkerSegment> statementParameterMarkerSegments = new LinkedList<>();
    
    private final List<SQLStatementSegment> sqlStatementsInPlsql = new ArrayList<>();
    
    private final List<ProcedureCallNameSegment> procedureCallNames = new ArrayList<>();
    
    private final List<ProcedureBodyEndNameSegment> procedureBodyEndNameSegments = new ArrayList<>();
    
    private final List<ExpressionSegment> dynamicSqlStatementExpressions = new ArrayList<>();
    
    private final Collection<String> variableNames = new HashSet<>();
    
    private final Map<String, SQLStatement> cursorStatements = new HashMap<>();
    
    private final List<CursorForLoopStatementSegment> cursorForLoopStatementSegments = new ArrayList<>();
    
    private final Map<Integer, Set<SQLStatement>> tempCursorForLoopStatements = new HashMap<>();
    
    private int cursorForLoopLevel;
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(getParameterMarkerIndex(ctx), ParameterMarkerType.QUESTION);
    }
    
    private int getParameterMarkerIndex(final ParameterMarkerContext ctx) {
        int startIndex = ctx.getStart().getStartIndex();
        if (startIndex <= 0) {
            return 0;
        }
        String sql = ctx.getStart().getInputStream().getText(new Interval(0, startIndex - 1));
        return countParameterMarkers(sql);
    }
    
    private int countParameterMarkers(final String sql) {
        ParameterMarkerScanState state = new ParameterMarkerScanState();
        while (state.index < sql.length()) {
            if (advanceInLineComment(sql, state)) {
                continue;
            }
            if (advanceInBlockComment(sql, state)) {
                continue;
            }
            if (advanceInOrToggleStringLiteral(sql, state)) {
                continue;
            }
            if (enterLineOrBlockComment(sql, state)) {
                continue;
            }
            if (!state.inStringLiteral && sql.charAt(state.index) == '?') {
                state.result++;
            }
            state.index++;
        }
        return state.result;
    }
    
    private boolean advanceInLineComment(final String sql, final ParameterMarkerScanState state) {
        if (!state.inLineComment) {
            return false;
        }
        char ch = sql.charAt(state.index);
        if ('\n' == ch || '\r' == ch) {
            state.inLineComment = false;
        }
        state.index++;
        return true;
    }
    
    private boolean advanceInBlockComment(final String sql, final ParameterMarkerScanState state) {
        if (!state.inBlockComment) {
            return false;
        }
        char ch = sql.charAt(state.index);
        if ('*' == ch && state.index + 1 < sql.length() && '/' == sql.charAt(state.index + 1)) {
            state.inBlockComment = false;
            state.index += 2;
        } else {
            state.index++;
        }
        return true;
    }
    
    private boolean advanceInOrToggleStringLiteral(final String sql, final ParameterMarkerScanState state) {
        if (sql.charAt(state.index) != '\'') {
            return false;
        }
        if (state.inStringLiteral && state.index + 1 < sql.length() && '\'' == sql.charAt(state.index + 1)) {
            state.index += 2;
        } else {
            state.inStringLiteral = !state.inStringLiteral;
            state.index++;
        }
        return true;
    }
    
    private boolean enterLineOrBlockComment(final String sql, final ParameterMarkerScanState state) {
        if (state.inStringLiteral) {
            return false;
        }
        char ch = sql.charAt(state.index);
        if ('-' == ch && state.index + 1 < sql.length() && '-' == sql.charAt(state.index + 1)) {
            state.inLineComment = true;
            state.index += 2;
            return true;
        }
        if ('/' == ch && state.index + 1 < sql.length() && '*' == sql.charAt(state.index + 1)) {
            state.inBlockComment = true;
            state.index += 2;
            return true;
        }
        return false;
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
        if (null != ctx.dateTimeLiterals()) {
            return visit(ctx.dateTimeLiterals());
        }
        if (null != ctx.intervalLiterals()) {
            return visit(ctx.intervalLiterals());
        }
        throw new IllegalStateException("Literals must have string, number, dateTime, hex, bit, interval, boolean or null.");
    }
    
    @Override
    public ASTNode visitDateTimeLiterals(final DateTimeLiteralsContext ctx) {
        if (null != ctx.LBE_()) {
            return new DateTimeLiteralValue(ctx.identifier().getText(), ((StringLiteralValue) visit(ctx.stringLiterals())).getValue(), true);
        }
        String dateTimeType;
        if (null != ctx.DATE()) {
            dateTimeType = ctx.DATE().getText();
        } else if (null != ctx.TIME()) {
            dateTimeType = ctx.TIME().getText();
        } else {
            dateTimeType = ctx.TIMESTAMP().getText();
        }
        return new DateTimeLiteralValue(dateTimeType, ((StringLiteralValue) visit(ctx.stringLiterals())).getValue(), false);
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
        UnreservedWordContext unreservedWord = ctx.unreservedWord();
        return null == unreservedWord ? new IdentifierValue(ctx.getText()) : visit(unreservedWord);
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
    public final ASTNode visitSynonymName(final SynonymNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        if (SequenceFunction.valueFrom(ctx.name().getText()).isPresent()) {
            return createSequenceFunction(ctx);
        }
        ColumnSegment result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        if (null != ctx.nestedItem() && !ctx.nestedItem().isEmpty()) {
            result.setNestedObjectAttributes(ctx.nestedItem().stream().map(item -> (IdentifierValue) visit(item.identifier())).collect(Collectors.toList()));
        }
        return result;
    }
    
    private FunctionSegment createSequenceFunction(final ColumnNameContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.name().getText(), getOriginalText(ctx));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitViewName(final ViewNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        IndexNameSegment indexName = new IndexNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName);
    }
    
    @Override
    public final ASTNode visitFunction(final FunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((IdentifierValue) visit(ctx.name())).getValue(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitPackageName(final PackageNameContext ctx) {
        return new PackageSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public final ASTNode visitTypeName(final TypeNameContext ctx) {
        return new TypeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public final ASTNode visitIndexTypeName(final IndexTypeNameContext ctx) {
        return new IndexTypeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
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
        if (null != ctx.datetimeExpr()) {
            return createDatetimeExpression(ctx, ctx.datetimeExpr());
        }
        if (null != ctx.multisetExpr()) {
            return createMultisetExpression(ctx);
        }
        return new NotExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.expr(0)), false);
    }
    
    private ASTNode createMultisetExpression(final ExprContext ctx) {
        ExpressionSegment left = (ColumnSegment) visitColumnName(ctx.multisetExpr().columnName(0));
        ExpressionSegment right = (ColumnSegment) visitColumnName(ctx.multisetExpr().columnName(1));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        String keyWord = null == ctx.multisetExpr().DISTINCT() ? "ALL" : "DISTINCT";
        return new MultisetExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, ctx.multisetExpr().multisetOperator().getText(), keyWord, text);
    }
    
    private ASTNode createDatetimeExpression(final ExprContext ctx, final DatetimeExprContext datetimeExpr) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        if (null == datetimeExpr.expr()) {
            return new DatetimeExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, text);
        }
        ExpressionSegment right = new ExpressionProjectionSegment(datetimeExpr.getStart().getStartIndex(),
                datetimeExpr.getStop().getStopIndex(), datetimeExpr.getText(), (ExpressionSegment) visit(datetimeExpr.expr()));
        return new DatetimeExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, text);
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
            if (null != ctx.NAN()) {
                operatorToken = ctx.NAN().getSymbol();
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
            right = new SubqueryExpressionSegment(
                    new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery())));
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
        if (null != ctx.PRIOR()) {
            return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
        }
        if (null == ctx.bitExpr(0)) {
            return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
        }
        return visit(ctx.bitExpr(0));
    }
    
    private InExpression createInSegment(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right;
        if (null == ctx.subquery()) {
            if (null == ctx.LP_() || null == ctx.RP_()) {
                StringLiteralsContext stringLiteralsContext = ctx.stringLiterals();
                right = new LiteralExpressionSegment(stringLiteralsContext.start.getStartIndex(), stringLiteralsContext.stop.getStopIndex(),
                        ((StringLiteralValue) visit(stringLiteralsContext)).getValue());
            } else {
                ListExpression listExpression = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
                for (ExprContext each : ctx.expr()) {
                    listExpression.getItems().add((ExpressionSegment) visit(each));
                }
                right = listExpression;
            }
        } else {
            right = new SubqueryExpressionSegment(
                    new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery())));
        }
        boolean not = null != ctx.NOT();
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
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
            globalParameterMarkerSegments.add(segment);
            statementParameterMarkerSegments.add(segment);
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
            ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(startIndex, stopIndex, parameterMarker.getValue(), parameterMarker.getType());
            globalParameterMarkerSegments.add(segment);
            statementParameterMarkerSegments.add(segment);
            return segment;
        }
        if (null != ctx.literals()) {
            return SQLUtils.createLiteralExpression(visit(ctx.literals()), startIndex, stopIndex, ctx.literals().start.getInputStream().getText(new Interval(startIndex, stopIndex)));
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return null == ctx.joinOperator() ? visit(ctx.columnName())
                    : new OuterJoinExpression(startIndex, stopIndex, (ColumnSegment) visitColumnName(ctx.columnName()), ctx.joinOperator().getText(), getOriginalText(ctx));
        }
        if (null != ctx.privateExprOfDb()) {
            return visit(ctx.privateExprOfDb());
        }
        if (null != ctx.LP_()) {
            if (1 == ctx.expr().size()) {
                return visit(ctx.expr(0));
            } else {
                ListExpression result = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
                for (ExprContext each : ctx.expr()) {
                    result.getItems().add((ExpressionSegment) visit(each));
                }
                return result;
            }
        }
        return visitRemainSimpleExpr(ctx, startIndex, stopIndex);
    }
    
    private ASTNode visitRemainSimpleExpr(final SimpleExprContext ctx, final int startIndex, final int stopIndex) {
        if (null != ctx.OR_()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.simpleExpr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.simpleExpr(1));
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, ctx.OR_().getText(), text);
        }
        if (null != ctx.caseExpression()) {
            return visit(ctx.caseExpression());
        }
        if (null != ctx.BINARY()) {
            return visit(ctx.simpleExpr(0));
        }
        for (SimpleExprContext each : ctx.simpleExpr()) {
            visit(each);
        }
        return new CommonExpressionSegment(startIndex, stopIndex, ctx.getText());
    }
    
    @Override
    public ASTNode visitCaseExpression(final CaseExpressionContext ctx) {
        ExpressionSegment caseExpr = null == ctx.simpleExpr() ? null : (ExpressionSegment) visit(ctx.simpleExpr());
        Collection<ExpressionSegment> whenExprs = new ArrayList<>(ctx.caseWhen().size());
        Collection<ExpressionSegment> thenExprs = new ArrayList<>(ctx.caseWhen().size());
        for (CaseWhenContext each : ctx.caseWhen()) {
            whenExprs.add((ExpressionSegment) visit(each.expr(0)));
            thenExprs.add((ExpressionSegment) visit(each.expr(1)));
        }
        ExpressionSegment elseExpr = null == ctx.caseElse() ? null : (ExpressionSegment) visit(ctx.caseElse().expr());
        return new CaseWhenExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), caseExpr, whenExprs, thenExprs, elseExpr);
    }
    
    @Override
    public ASTNode visitPrivateExprOfDb(final PrivateExprOfDbContext ctx) {
        if (null != ctx.intervalExpression()) {
            return visit(ctx.intervalExpression());
        }
        return super.visitPrivateExprOfDb(ctx);
    }
    
    @Override
    public ASTNode visitIntervalExpression(final IntervalExpressionContext ctx) {
        IntervalExpressionProjection result = new IntervalExpressionProjection(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr(0)),
                (ExpressionSegment) visit(ctx.MINUS_()), (ExpressionSegment) visit(ctx.expr(1)), getOriginalText(ctx));
        if (null != ctx.intervalDayToSecondExpression()) {
            result.setDayToSecondExpression((IntervalDayToSecondExpression) visit(ctx.intervalDayToSecondExpression()));
        } else {
            result.setYearToMonthExpression((IntervalYearToMonthExpression) visit(ctx.intervalYearToMonthExpression()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitIntervalDayToSecondExpression(final IntervalDayToSecondExpressionContext ctx) {
        IntervalDayToSecondExpression result = new IntervalDayToSecondExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ctx.DAY().getText(), ctx.TO().getText(), ctx.SECOND().getText());
        if (null != ctx.leadingFieldPrecision()) {
            result.setLeadingFieldPrecision(Integer.parseInt(ctx.leadingFieldPrecision().INTEGER_().getText()));
        }
        if (null != ctx.fractionalSecondPrecision()) {
            result.setFractionalSecondPrecision(Integer.parseInt(ctx.fractionalSecondPrecision().getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitIntervalYearToMonthExpression(final IntervalYearToMonthExpressionContext ctx) {
        IntervalYearToMonthExpression result = new IntervalYearToMonthExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ctx.YEAR().getText(), ctx.TO().getText(), ctx.MONTH().getText());
        if (null != ctx.leadingFieldPrecision()) {
            result.setLeadingFieldPrecision(Integer.parseInt(ctx.leadingFieldPrecision().INTEGER_().getText()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.analyticFunction()) {
            return visit(ctx.analyticFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        if (null != ctx.xmlFunction()) {
            return visit(ctx.xmlFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction, analyticFunction or specialFunction.");
    }
    
    @Override
    public ASTNode visitAnalyticFunction(final AnalyticFunctionContext ctx) {
        String functionName = null == ctx.analyticFunctionName() ? ctx.specifiedAnalyticFunctionName.getText() : ctx.analyticFunctionName().getText();
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), functionName, getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx.expr()));
        for (DataTypeContext each : ctx.dataType()) {
            result.getParameters().add((DataTypeSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType)
                : createAggregationFunctionSegment(ctx, aggregationType);
    }
    
    @Override
    public ASTNode visitWmConcatFunction(final WmConcatFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.WM_CONCAT().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        if (null != ctx.owner()) {
            OwnerContext owner = ctx.owner();
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    private FunctionSegment createAggregationFunctionSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), aggregationType, getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        if (null != ctx.DISTINCT()) {
            AggregationDistinctProjectionSegment result =
                    new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx), getDistinctExpression(ctx));
            result.getParameters().addAll(getExpressions(ctx.expr()));
            return result;
        }
        AggregationProjectionSegment result = new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitXmlFunction(final XmlFunctionContext ctx) {
        if (null != ctx.xmlAggFunction()) {
            return visit(ctx.xmlAggFunction());
        }
        if (null != ctx.xmlColattvalFunction()) {
            return visit(ctx.xmlColattvalFunction());
        }
        if (null != ctx.xmlExistsFunction()) {
            return visit(ctx.xmlExistsFunction());
        }
        if (null != ctx.xmlForestFunction()) {
            return visit(ctx.xmlForestFunction());
        }
        if (null != ctx.xmlParseFunction()) {
            return visit(ctx.xmlParseFunction());
        }
        if (null != ctx.xmlPiFunction()) {
            return visit(ctx.xmlPiFunction());
        }
        if (null != ctx.xmlQueryFunction()) {
            return visit(ctx.xmlQueryFunction());
        }
        if (null != ctx.xmlRootFunction()) {
            return visit(ctx.xmlRootFunction());
        }
        if (null != ctx.xmlSerializeFunction()) {
            return visit(ctx.xmlSerializeFunction());
        }
        if (null != ctx.xmlIsSchemaValidFunction()) {
            return visit(ctx.xmlIsSchemaValidFunction());
        }
        if (null != ctx.xmlTableFunction()) {
            return visit(ctx.xmlTableFunction());
        }
        if (null != ctx.xmlElementFunction()) {
            return visit(ctx.xmlElementFunction());
        }
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.specifiedFunctionName.getText(), getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx.exprList()));
        return result;
    }
    
    @Override
    public ASTNode visitXmlElementFunction(final XmlElementFunctionContext ctx) {
        XmlElementFunctionSegment result =
                new XmlElementFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLELEMENT().getText(), (IdentifierValue) visit(ctx.identifier()), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.exprWithAlias().stream().map(each -> (ExpressionSegment) visit(each.expr())).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        if (null != ctx.xmlAttributes()) {
            Collection<ExpressionSegment> xmlAttributes = ctx.xmlAttributes().exprWithAlias().stream().map(each -> (ExpressionSegment) visit(each.expr())).collect(Collectors.toList());
            result.getXmlAttributes().addAll(xmlAttributes);
        }
        return result;
    }
    
    @Override
    public ASTNode visitXmlCdataFunction(final XmlCdataFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLCDATA().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.stringLiterals()));
        return result;
    }
    
    @Override
    public ASTNode visitXmlAggFunction(final XmlAggFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.XMLAGG().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlColattvalFunction(final XmlColattvalFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLCOLATTVAL().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlExistsFunction(final XmlExistsFunctionContext ctx) {
        XmlQueryAndExistsFunctionSegment result =
                new XmlQueryAndExistsFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLEXISTS().getText(), ctx.STRING_().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.xmlPassingClause().expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlForestFunction(final XmlForestFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLFOREST().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlParseFunction(final XmlParseFunctionContext ctx) {
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitXmlPiFunction(final XmlPiFunctionContext ctx) {
        if (null != ctx.identifier()) {
            return new XmlPiFunctionSegment(ctx.start.getStopIndex(), ctx.stop.getStopIndex(), ctx.XMLPI().getText(),
                    ctx.identifier().getText(), (ExpressionSegment) visit(ctx.expr(0)), getOriginalText(ctx));
        }
        return new XmlPiFunctionSegment(ctx.start.getStopIndex(), ctx.stop.getStopIndex(), ctx.XMLPI().getText(),
                (ExpressionSegment) visit(ctx.expr(0)), (ExpressionSegment) visit(ctx.expr(1)), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlQueryFunction(final XmlQueryFunctionContext ctx) {
        XmlQueryAndExistsFunctionSegment result =
                new XmlQueryAndExistsFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLQUERY().getText(), ctx.STRING_().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.xmlPassingClause().expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlRootFunction(final XmlRootFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLROOT().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlSerializeFunction(final XmlSerializeFunctionContext ctx) {
        String dataType = null == ctx.dataType() ? null : ctx.dataType().getText();
        String encoding = null == ctx.STRING_() ? null : ctx.STRING_().getText();
        String version = null == ctx.stringLiterals() ? null : ctx.stringLiterals().getText();
        String identSize = null == ctx.INTEGER_() ? null : ctx.INTEGER_().getText();
        return new XmlSerializeFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLSERIALIZE().getText(), (ExpressionSegment) visit(ctx.expr()),
                dataType, encoding, version, identSize, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlTableFunction(final XmlTableFunctionContext ctx) {
        XmlNamespacesClauseSegment xmlNamespacesClause = null == ctx.xmlNamespacesClause() ? null : (XmlNamespacesClauseSegment) visit(ctx.xmlNamespacesClause());
        return new XmlTableFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLTABLE().getText(),
                xmlNamespacesClause, ctx.STRING_().getText(), (XmlTableOptionsSegment) visit(ctx.xmlTableOptions()), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlNamespacesClause(final XmlNamespacesClauseContext ctx) {
        // TODO : throw exception if more than one defaultString exists in a xml name space clause
        String defaultString = null == ctx.defaultString() ? null : ctx.defaultString(0).STRING_().getText();
        Collection<XmlNamespaceStringAsIdentifierSegment> xmlNamespaceStringAsIdentifierSegments = null == ctx.xmlNamespaceStringAsIdentifier() ? Collections.emptyList()
                : ctx.xmlNamespaceStringAsIdentifier().stream().map(each -> (XmlNamespaceStringAsIdentifierSegment) visit(each)).collect(Collectors.toList());
        XmlNamespacesClauseSegment result = new XmlNamespacesClauseSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), defaultString, getOriginalText(ctx));
        result.getStringAsIdentifier().addAll(xmlNamespaceStringAsIdentifierSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlNamespaceStringAsIdentifier(final XmlNamespaceStringAsIdentifierContext ctx) {
        return new XmlNamespaceStringAsIdentifierSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.STRING_().getText(), ctx.identifier().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlTableOptions(final XmlTableOptionsContext ctx) {
        XmlTableOptionsSegment result = new XmlTableOptionsSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = null == ctx.xmlPassingClause().expr() ? Collections.emptyList()
                : ctx.xmlPassingClause().expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        Collection<XmlTableColumnSegment> xmlTableColumnSegments = null == ctx.xmlTableColumn() ? Collections.emptyList()
                : ctx.xmlTableColumn().stream().map(each -> (XmlTableColumnSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        result.getXmlTableColumnSegments().addAll(xmlTableColumnSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlTableColumn(final XmlTableColumnContext ctx) {
        String dataType = null == ctx.dataType() ? null : ctx.dataType().getText();
        String path = null == ctx.STRING_() ? null : ctx.STRING_().getText();
        ExpressionSegment defaultExpr = null == ctx.expr() ? null : (ExpressionSegment) visit(ctx.expr());
        return new XmlTableColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.columnName().getText(), dataType, path, defaultExpr, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlIsSchemaValidFunction(final XmlIsSchemaValidFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.ISSCHEMAVALID().getText(), getOriginalText(ctx));
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressions(final ExprListContext exprList) {
        if (null == exprList) {
            return Collections.emptyList();
        }
        return getExpressions(exprList.exprs().expr());
    }
    
    private Collection<ExpressionSegment> getExpressions(final List<ExprContext> exprList) {
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
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    @Override
    public final ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        if (null != ctx.extractFunction()) {
            return visit(ctx.extractFunction());
        }
        if (null != ctx.formatFunction()) {
            return visit(ctx.formatFunction());
        }
        if (null != ctx.firstOrLastValueFunction()) {
            return visit(ctx.firstOrLastValueFunction());
        }
        if (null != ctx.trimFunction()) {
            return visit(ctx.trimFunction());
        }
        if (null != ctx.featureFunction()) {
            return visit(ctx.featureFunction());
        }
        if (null != ctx.setFunction()) {
            return visit(ctx.setFunction());
        }
        if (null != ctx.translateFunction()) {
            return visit(ctx.translateFunction());
        }
        if (null != ctx.cursorFunction()) {
            return visit(ctx.cursorFunction());
        }
        if (null != ctx.toDateFunction()) {
            return visit(ctx.toDateFunction());
        }
        if (null != ctx.approxRank()) {
            return visit(ctx.approxRank());
        }
        if (null != ctx.wmConcatFunction()) {
            return visit(ctx.wmConcatFunction());
        }
        if (null != ctx.predictionCostFunction()) {
            return visit(ctx.predictionCostFunction());
        }
        if (null != ctx.jsonObjectFunction()) {
            return visit(ctx.jsonObjectFunction());
        }
        throw new IllegalStateException(
                "SpecialFunctionContext must have castFunction, charFunction, extractFunction, formatFunction, firstOrLastValueFunction, "
                        + "trimFunction, toDateFunction, approxCount, predictionCostFunction, jsonObjectFunction or featureFunction.");
    }
    
    @Override
    public ASTNode visitJsonObjectFunction(final JsonObjectFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.JSON_OBJECT().getText(), getOriginalText(ctx));
        for (JsonObjectKeyValueContext each : ctx.jsonObjectContent().jsonObjectKeyValue()) {
            result.getParameters().addAll(getExpressions(each.expr()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitPredictionCostFunction(final PredictionCostFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.PREDICTION_COST().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitApproxRank(final ApproxRankContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.APPROX_RANK().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitCursorFunction(final CursorFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CURSOR().toString(), ctx.getText());
        result.getParameters()
                .add(new SubqueryExpressionSegment(
                        new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery()))));
        return result;
    }
    
    @Override
    public ASTNode visitToDateFunction(final ToDateFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TO_DATE().getText(), getOriginalText(ctx));
        if (null != ctx.STRING_()) {
            ctx.STRING_().forEach(each -> result.getParameters().add(new LiteralExpressionSegment(each.getSymbol().getStartIndex(), each.getSymbol().getStopIndex(), each.getSymbol().getText())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitTranslateFunction(final TranslateFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TRANSLATE().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        TerminalNode charSet = null == ctx.NCHAR_CS() ? ctx.CHAR_CS() : ctx.NCHAR_CS();
        result.getParameters().add(new LiteralExpressionSegment(charSet.getSymbol().getStartIndex(), charSet.getSymbol().getStopIndex(), charSet.getText()));
        return result;
    }
    
    @Override
    public final ASTNode visitSetFunction(final SetFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.SET().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        FunctionSegment result;
        if (null != ctx.CAST()) {
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CAST().getText(), getOriginalText(ctx));
        } else {
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.XMLCAST().getText(), getOriginalText(ctx));
        }
        if (null != ctx.MULTISET()) {
            result.getParameters().add(new SubqueryExpressionSegment(new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(),
                    (SelectStatement) visit(ctx.subquery()), getOriginalText(ctx.subquery()))));
        } else {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        }
        result.getParameters().add((DataTypeSegment) visit(ctx.dataType()));
        return result;
    }
    
    @Override
    public final ASTNode visitCharFunction(final CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result;
        if (null != ctx.CHR()) {
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHR().getText(), getOriginalText(ctx));
        } else {
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHAR().getText(), getOriginalText(ctx));
        }
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitExtractFunction(final ExtractFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.EXTRACT().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitFormatFunction(final FormatFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.FORMAT().getText(), getOriginalText(ctx));
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitFirstOrLastValueFunction(final FirstOrLastValueFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                null == ctx.FIRST_VALUE() ? ctx.LAST_VALUE().getText() : ctx.FIRST_VALUE().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitTrimFunction(final TrimFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TRIM().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitFeatureFunction(final FeatureFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.featureFunctionName().getText(), getOriginalText(ctx));
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.regularFunctionName().getText(), getOriginalText(ctx));
        if (null != ctx.owner()) {
            OwnerContext owner = ctx.owner();
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        result.getParameters().addAll(getExpressions(ctx.expr()));
        return result;
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        Collection<String> dataTypeNames = new LinkedList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            dataTypeNames.add(ctx.getChild(i).getText());
        }
        return new KeywordValue(String.join(" ", dataTypeNames));
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
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
        OrderDirection orderDirection = null == ctx.DESC() ? OrderDirection.ASC : OrderDirection.DESC;
        NullsOrderType nullsOrderType = generateNullsOrderType(ctx);
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            return new ColumnOrderByItemSegment(column, orderDirection, nullsOrderType);
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtils.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection, nullsOrderType);
        }
        return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(), ctx.expr().getStop().getStopIndex(),
                getOriginalText(ctx.expr()), orderDirection, nullsOrderType, (ExpressionSegment) visit(ctx.expr()));
    }
    
    private NullsOrderType generateNullsOrderType(final OrderByItemContext ctx) {
        if (null == ctx.FIRST() && null == ctx.LAST()) {
            return null;
        }
        return null == ctx.FIRST() ? NullsOrderType.LAST : NullsOrderType.FIRST;
    }
    
    @Override
    public final ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        if (null != ctx.dataTypeName()) {
            result.setDataTypeName(((KeywordValue) visit(ctx.dataTypeName())).getValue());
        }
        if (null != ctx.specialDatatype()) {
            result.setDataTypeName(((KeywordValue) visit(ctx.specialDatatype().dataTypeName())).getValue());
        }
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
        List<TerminalNode> numbers = ctx.INTEGER_();
        if (1 == numbers.size()) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        }
        if (2 == numbers.size()) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
            result.setScale(Integer.parseInt(numbers.get(1).getText()));
        }
        if (null != ctx.type) {
            result.setType(ctx.type.getText());
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
    
    /**
     * Pop all statement parameter marker segments.
     *
     * @return all statement parameter marker segments
     */
    protected Collection<ParameterMarkerSegment> popAllStatementParameterMarkerSegments() {
        Collection<ParameterMarkerSegment> result = new LinkedList<>(statementParameterMarkerSegments);
        statementParameterMarkerSegments.clear();
        return result;
    }
    
    /**
     * Increase cursor for loop level.
     */
    protected void increaseCursorForLoopLevel() {
        ++cursorForLoopLevel;
    }
    
    /**
     * Decrease cursor for loop level.
     */
    protected void decreaseCursorForLoopLevel() {
        --cursorForLoopLevel;
    }
    
    private static final class ParameterMarkerScanState {
        
        private int index;
        
        private int result;
        
        private boolean inStringLiteral;
        
        private boolean inLineComment;
        
        private boolean inBlockComment;
    }
}
