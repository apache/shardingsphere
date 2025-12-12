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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExtractArgExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.BetweenExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.BinaryOperationExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.CaseWhenExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.CollateExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.DataTypeExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ExistsSubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ExtractArgExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.FunctionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.InExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.IntervalExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ListExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.LiteralExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.MatchExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.NotExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.QuantifySubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.RowExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.SubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.TypeCastExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.UnaryOperationExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.VariableSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.AggregationProjectionConverter;

import java.util.Optional;

/**
 * Expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionConverter {
    
    /**
     * Convert expression segment to SQL node.
     *
     * @param segment expression segment
     * @return SQL node
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    public static Optional<SqlNode> convert(final ExpressionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return LiteralExpressionConverter.convert((LiteralExpressionSegment) segment);
        }
        if (segment instanceof CommonExpressionSegment) {
            // TODO
            throw new UnsupportedSQLOperationException("unsupported CommonExpressionSegment");
        }
        if (segment instanceof ListExpression) {
            return ListExpressionConverter.convert((ListExpression) segment);
        }
        if (segment instanceof BinaryOperationExpression) {
            return Optional.of(BinaryOperationExpressionConverter.convert((BinaryOperationExpression) segment));
        }
        if (segment instanceof ColumnSegment) {
            return Optional.of(ColumnConverter.convert((ColumnSegment) segment));
        }
        if (segment instanceof ExistsSubqueryExpression) {
            return Optional.of(ExistsSubqueryExpressionConverter.convert((ExistsSubqueryExpression) segment));
        }
        if (segment instanceof SubqueryExpressionSegment) {
            return Optional.of(SubqueryExpressionConverter.convert((SubqueryExpressionSegment) segment));
        }
        if (segment instanceof InExpression) {
            return Optional.of(InExpressionConverter.convert((InExpression) segment));
        }
        if (segment instanceof BetweenExpression) {
            return Optional.of(BetweenExpressionConverter.convert((BetweenExpression) segment));
        }
        if (segment instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(ParameterMarkerExpressionConverter.convert((ParameterMarkerExpressionSegment) segment));
        }
        if (segment instanceof FunctionSegment) {
            return FunctionConverter.convert((FunctionSegment) segment);
        }
        if (segment instanceof AggregationProjectionSegment) {
            return AggregationProjectionConverter.convert((AggregationProjectionSegment) segment);
        }
        if (segment instanceof DataTypeSegment) {
            return Optional.of(DataTypeExpressionConverter.convert((DataTypeSegment) segment));
        }
        if (segment instanceof CaseWhenExpression) {
            return Optional.of(CaseWhenExpressionConverter.convert((CaseWhenExpression) segment));
        }
        if (segment instanceof NotExpression) {
            return Optional.of(NotExpressionConverter.convert((NotExpression) segment));
        }
        if (segment instanceof TypeCastExpression) {
            return TypeCastExpressionConverter.convert((TypeCastExpression) segment);
        }
        if (segment instanceof ExtractArgExpression) {
            return Optional.of(ExtractArgExpressionConverter.convert((ExtractArgExpression) segment));
        }
        if (segment instanceof MatchAgainstExpression) {
            return Optional.of(MatchExpressionConverter.convert((MatchAgainstExpression) segment));
        }
        if (segment instanceof CollateExpression) {
            return Optional.of(CollateExpressionConverter.convert((CollateExpression) segment));
        }
        if (segment instanceof RowExpression) {
            return Optional.of(RowExpressionConverter.convert((RowExpression) segment));
        }
        if (segment instanceof VariableSegment) {
            return VariableSegmentConverter.convert((VariableSegment) segment);
        }
        if (segment instanceof UnaryOperationExpression) {
            return Optional.of(UnaryOperationExpressionConverter.convert((UnaryOperationExpression) segment));
        }
        if (segment instanceof IntervalExpression) {
            return Optional.of(IntervalExpressionConverter.convert((IntervalExpression) segment));
        }
        if (segment instanceof QuantifySubqueryExpression) {
            return Optional.of(QuantifySubqueryExpressionConverter.convert((QuantifySubqueryExpression) segment));
        }
        throw new UnsupportedSQLOperationException("unsupported TableSegment type: " + segment.getClass());
    }
}
