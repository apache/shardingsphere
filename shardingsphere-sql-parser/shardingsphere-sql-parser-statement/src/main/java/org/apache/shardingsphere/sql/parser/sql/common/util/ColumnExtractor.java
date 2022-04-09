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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Column extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnExtractor {
    
    /**
     * Extract column segment collection.
     *
     * @param expression expression segment
     * @return column segment collection
     */
    public static Collection<ColumnSegment> extract(final ExpressionSegment expression) {
        Collection<ColumnSegment> result = new LinkedList<>();
        if (expression instanceof BinaryOperationExpression) {
            if (((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
                result.add((ColumnSegment) ((BinaryOperationExpression) expression).getLeft());
            }
            if (((BinaryOperationExpression) expression).getRight() instanceof ColumnSegment) {
                result.add((ColumnSegment) ((BinaryOperationExpression) expression).getRight());
            }
        }
        if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            result.add((ColumnSegment) ((InExpression) expression).getLeft());
        }
        if (expression instanceof BetweenExpression && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
            result.add((ColumnSegment) ((BetweenExpression) expression).getLeft());
        }
        return result;
    }
    
    /**
     * Extract column segments.
     *
     * @param columnSegments column segments
     * @param whereSegments where segments
     */
    public static void extractColumnSegments(final Collection<ColumnSegment> columnSegments, final Collection<WhereSegment> whereSegments) {
        for (WhereSegment each : whereSegments) {
            for (AndPredicate andPredicate : ExpressionExtractUtil.getAndPredicates(each.getExpr())) {
                extractColumnSegments(columnSegments, andPredicate);
            }
        }
    }
    
    private static void extractColumnSegments(final Collection<ColumnSegment> columnSegments, final AndPredicate andPredicate) {
        for (ExpressionSegment each : andPredicate.getPredicates()) {
            columnSegments.addAll(ColumnExtractor.extract(each));
        }
    }
}
