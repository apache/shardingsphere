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

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;

public final class ExpressionUtil {
    
    /**
     * Get left value if left value of expression is ColumnSegment.
     *
     * @param expression ExpressionSegment.
     * @return ColumnSegment.
     */
    public static ColumnSegment getColumnFromExpression(final ExpressionSegment expression) {
        ColumnSegment column = null;
        if (expression instanceof BinaryOperationExpression && ((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
            column = (ColumnSegment) ((BinaryOperationExpression) expression).getLeft();
        } else if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            column = (ColumnSegment) ((InExpression) expression).getLeft();
        } else if (expression instanceof BetweenExpression && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
            column = (ColumnSegment) ((BetweenExpression) expression).getLeft();
        }
        return column;
    }
}
