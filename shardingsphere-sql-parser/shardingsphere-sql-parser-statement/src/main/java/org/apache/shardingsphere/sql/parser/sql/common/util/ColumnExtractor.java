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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

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
     * Get left and right value if either value of expression is column segment.
     *
     * @param expression expression segment
     * @return column segment collection
     */
    public static Collection<ColumnSegment> extractAll(final ExpressionSegment expression) {
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression boExpression = (BinaryOperationExpression) expression;
            Collection<ColumnSegment> columns = new ArrayList<>();
            if (Objects.nonNull(boExpression.getLeft()) && boExpression.getLeft() instanceof ColumnSegment) {
                columns.add((ColumnSegment) boExpression.getLeft());
            }
            if (Objects.nonNull(boExpression.getRight()) && boExpression.getRight() instanceof ColumnSegment) {
                columns.add((ColumnSegment) boExpression.getRight());
            }
            return columns;
        }
        if (expression instanceof InExpression && Objects.nonNull(((InExpression) expression).getLeft()) 
                && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((InExpression) expression).getLeft();
            return Arrays.asList(column);
        }
        if (expression instanceof BetweenExpression && Objects.nonNull(((BetweenExpression) expression).getLeft()) 
                && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((BetweenExpression) expression).getLeft();
            return Arrays.asList(column);
        }
        return Collections.emptyList();
    }
}
