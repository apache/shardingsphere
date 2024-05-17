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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExtractArgExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.BetweenExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.BinaryOperationExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.CaseWhenExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.CollateExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ExistsSubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ExtractArgExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.FunctionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.InExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ListExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.LiteralExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.MatchExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.NotExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.RowExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.SubqueryExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.TypeCastExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.UnaryOperationExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.VariableSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.projection.impl.AggregationProjectionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.projection.impl.DataTypeConverter;

import java.util.Optional;

/**
 * Expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionConverter {
    
    /**
     * Convert expression segment to sql node.
     * 
     * @param segment expression segment
     * @return sql node
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
            return BinaryOperationExpressionConverter.convert((BinaryOperationExpression) segment);
        }
        if (segment instanceof ColumnSegment) {
            return ColumnConverter.convert((ColumnSegment) segment);
        }
        if (segment instanceof ExistsSubqueryExpression) {
            return ExistsSubqueryExpressionConverter.convert((ExistsSubqueryExpression) segment);
        }
        if (segment instanceof SubqueryExpressionSegment) {
            return SubqueryExpressionConverter.convert((SubqueryExpressionSegment) segment);
        }
        if (segment instanceof InExpression) {
            return InExpressionConverter.convert((InExpression) segment);
        }
        if (segment instanceof BetweenExpression) {
            return BetweenExpressionConverter.convert((BetweenExpression) segment);
        }
        if (segment instanceof ParameterMarkerExpressionSegment) {
            return ParameterMarkerExpressionConverter.convert((ParameterMarkerExpressionSegment) segment);
        }
        if (segment instanceof FunctionSegment) {
            return FunctionConverter.convert((FunctionSegment) segment);
        }
        if (segment instanceof AggregationProjectionSegment) {
            return AggregationProjectionConverter.convert((AggregationProjectionSegment) segment);
        }
        if (segment instanceof DataTypeSegment) {
            return DataTypeConverter.convert((DataTypeSegment) segment);
        }
        if (segment instanceof CaseWhenExpression) {
            return CaseWhenExpressionConverter.convert((CaseWhenExpression) segment);
        }
        if (segment instanceof NotExpression) {
            return NotExpressionConverter.convert((NotExpression) segment);
        }
        if (segment instanceof TypeCastExpression) {
            return TypeCastExpressionConverter.convert((TypeCastExpression) segment);
        }
        if (segment instanceof ExtractArgExpression) {
            return ExtractArgExpressionConverter.convert((ExtractArgExpression) segment);
        }
        if (segment instanceof MatchAgainstExpression) {
            return MatchExpressionConverter.convert((MatchAgainstExpression) segment);
        }
        if (segment instanceof CollateExpression) {
            return CollateExpressionConverter.convert((CollateExpression) segment);
        }
        if (segment instanceof RowExpression) {
            return RowExpressionConverter.convert((RowExpression) segment);
        }
        if (segment instanceof VariableSegment) {
            return VariableSegmentConverter.convert((VariableSegment) segment);
        }
        if (segment instanceof UnaryOperationExpression) {
            return UnaryOperationExpressionConverter.convert((UnaryOperationExpression) segment);
        }
        throw new UnsupportedSQLOperationException("unsupported TableSegment type: " + segment.getClass());
    }
}
