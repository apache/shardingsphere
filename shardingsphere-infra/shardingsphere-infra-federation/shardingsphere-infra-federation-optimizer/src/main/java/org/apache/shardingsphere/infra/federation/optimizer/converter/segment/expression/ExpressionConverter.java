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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.BetweenExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.BinaryOperationExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.ExistsSubqueryExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.FunctionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.InExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.ListExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.LiteralExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.SubqueryExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.projection.impl.AggregationProjectionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;

import java.util.Optional;

/**
 * Expression converter.
 */
public final class ExpressionConverter implements SQLSegmentConverter<ExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ExpressionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return new LiteralExpressionConverter().convert((LiteralExpressionSegment) segment);
        } else if (segment instanceof CommonExpressionSegment) {
            // TODO
            throw new UnsupportedOperationException("unsupported CommonExpressionSegment");
        } else if (segment instanceof ListExpression) {
            return new ListExpressionConverter().convert((ListExpression) segment).map(optional -> optional);
        } else if (segment instanceof BinaryOperationExpression) {
            return new BinaryOperationExpressionConverter().convert((BinaryOperationExpression) segment).map(optional -> optional);
        } else if (segment instanceof ColumnSegment) {
            return new ColumnConverter().convert((ColumnSegment) segment).map(optional -> optional);
        } else if (segment instanceof ExistsSubqueryExpression) {
            return new ExistsSubqueryExpressionConverter().convert((ExistsSubqueryExpression) segment).map(optional -> optional);
        } else if (segment instanceof SubqueryExpressionSegment) {
            return new SubqueryExpressionConverter().convert((SubqueryExpressionSegment) segment);
        } else if (segment instanceof InExpression) {
            return new InExpressionConverter().convert((InExpression) segment).map(optional -> optional);
        } else if (segment instanceof BetweenExpression) {
            return new BetweenExpressionConverter().convert((BetweenExpression) segment).map(optional -> optional);
        } else if (segment instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerExpressionConverter().convert((ParameterMarkerExpressionSegment) segment).map(optional -> optional);
        } else if (segment instanceof FunctionSegment) {
            return new FunctionConverter().convert((FunctionSegment) segment).map(optional -> optional);
        } else if (segment instanceof AggregationProjectionSegment) {
            return new AggregationProjectionConverter().convert((AggregationProjectionSegment) segment).map(optional -> optional);
        }
        throw new UnsupportedOperationException("unsupported TableSegment type: " + segment.getClass());
    }
}
