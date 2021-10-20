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

package org.apache.shardingsphere.infra.optimize.converter.segment.expression;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.BetweenExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.BinaryOperationExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ExistsSubqueryExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.InExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ListExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.LiteralExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.SubqueryExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;

import java.util.Optional;

/**
 * Expression converter.
 */
public final class ExpressionConverter implements SQLSegmentConverter<ExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final ExpressionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return new LiteralExpressionConverter().convertToSQLNode((LiteralExpressionSegment) segment);
        } else if (segment instanceof CommonExpressionSegment) {
            // TODO 
            throw new UnsupportedOperationException("unsupported CommonExpressionSegment");
        } else if (segment instanceof ListExpression) {
            return new ListExpressionConverter().convertToSQLNode((ListExpression) segment);
        } else if (segment instanceof BinaryOperationExpression) {
            return new BinaryOperationExpressionConverter().convertToSQLNode((BinaryOperationExpression) segment);
        } else if (segment instanceof ColumnSegment) {
            return new ColumnConverter().convertToSQLNode((ColumnSegment) segment);
        } else if (segment instanceof ExistsSubqueryExpression) {
            return new ExistsSubqueryExpressionConverter().convertToSQLNode((ExistsSubqueryExpression) segment);
        } else if (segment instanceof SubqueryExpressionSegment) {
            return new SubqueryExpressionConverter().convertToSQLNode((SubqueryExpressionSegment) segment);
        } else if (segment instanceof InExpression) {
            return new InExpressionConverter().convertToSQLNode((InExpression) segment);
        } else if (segment instanceof BetweenExpression) {
            return new BetweenExpressionConverter().convertToSQLNode((BetweenExpression) segment);
        }
        throw new UnsupportedOperationException("unsupported TableSegment type: " + segment.getClass());
    }
    
    @Override
    public Optional<ExpressionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        return Optional.empty();
    }
}
