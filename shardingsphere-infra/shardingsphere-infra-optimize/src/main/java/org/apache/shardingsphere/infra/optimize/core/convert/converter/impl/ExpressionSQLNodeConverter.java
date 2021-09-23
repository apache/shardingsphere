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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SQLNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;

import java.util.Optional;

/**
 * Expression converter entry.
 */
public final class ExpressionSQLNodeConverter implements SQLNodeConverter<ExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ExpressionSegment expression) {
        if (null == expression) {
            return Optional.empty();
        }
        if (expression instanceof LiteralExpressionSegment) {
            return new LiteralExpressionSQLNodeConverter().convert((LiteralExpressionSegment) expression);
        } else if (expression instanceof CommonExpressionSegment) {
            // TODO 
            throw new UnsupportedOperationException("unsupported CommonExpressionSegment");
        } else if (expression instanceof ListExpression) {
            return new ListExpressionSQLNodeConverter().convert((ListExpression) expression);
        } else if (expression instanceof BinaryOperationExpression) {
            return new BinaryOperationExpressionSQLNodeConverter().convert((BinaryOperationExpression) expression);
        } else if (expression instanceof ColumnSegment) {
            return new ColumnSQLNodeConverter().convert((ColumnSegment) expression);
        }
        throw new UnsupportedOperationException("unsupported TableSegment type: " + expression.getClass());
    }
}
