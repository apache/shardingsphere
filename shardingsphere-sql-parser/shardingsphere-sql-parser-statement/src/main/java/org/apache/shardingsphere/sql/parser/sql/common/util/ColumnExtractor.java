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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Column extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnExtractor {
    
    /**
     * Get left value if left value of expression is column segment.
     *
     * @param expression expression segment
     * @return column segment
     */
    public static Optional<ColumnSegment> extract(final ExpressionSegment expression) {
        if (expression instanceof BinaryOperationExpression && ((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((BinaryOperationExpression) expression).getLeft();
            return Optional.of(column);
        }
        if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((InExpression) expression).getLeft();
            return Optional.of(column);
        }
        if (expression instanceof BetweenExpression && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((BetweenExpression) expression).getLeft();
            return Optional.of(column);
        }
        return Optional.empty();
    }
    
    /**
     * Extract all columns.
     *
     * @param expression expression segment
     * @return column segment
     */
    public static Collection<ColumnSegment> extractAllColumns(final ExpressionSegment expression) {
        Collection<ColumnSegment> result = new LinkedList<>();
        if (expression instanceof ColumnSegment) {
            result.add((ColumnSegment) expression);
        }
        if (expression instanceof BinaryOperationExpression) {
            result.addAll(extractAllColumns(((BinaryOperationExpression) expression).getLeft()));
            result.addAll(extractAllColumns(((BinaryOperationExpression) expression).getRight()));
        }
        if (expression instanceof InExpression) {
            result.addAll(extractAllColumns(((InExpression) expression).getLeft()));
            result.addAll(extractAllColumns(((InExpression) expression).getRight()));
        }
        if (expression instanceof BetweenExpression) {
            result.addAll(extractAllColumns(((BetweenExpression) expression).getLeft()));
            result.addAll(extractAllColumns(((BetweenExpression) expression).getBetweenExpr()));
            result.addAll(extractAllColumns(((BetweenExpression) expression).getAndExpr()));
        }
        if (expression instanceof CommonTableExpressionSegment) {
            result.addAll(((CommonTableExpressionSegment) expression).getColumns());
        }
        if (expression instanceof ExpressionProjectionSegment) {
            result.addAll(extractAllColumns(((ExpressionProjectionSegment) expression).getExpr()));
        }
        if (expression instanceof ListExpression) {
            result.addAll(((ListExpression) expression).getItems().stream().flatMap(each -> extractAllColumns(each).stream()).collect(Collectors.toList()));
        }
        if (expression instanceof NotExpression) {
            result.addAll(extractAllColumns(((NotExpression) expression).getExpression()));
        }
        return result;
    }
}
