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

package org.apache.shardingsphere.infra.expression;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.List;

/**
 * Expression segment util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpressionSegmentUtil {
    /**
     * Extract all ParameterMarkerExpressionSegment from ExpressionSegment list.
     *
     * @param expressions ExpressionSegment list
     * @return ParameterMarkerExpressionSegment list
     */
    public static List<ParameterMarkerExpressionSegment> extractParameterMarkerExpressionSegment(final List<ExpressionSegment> expressions) {
        List<ParameterMarkerExpressionSegment> extractList = Lists.newArrayList();
        for (ExpressionSegment each : expressions) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                extractList.add((ParameterMarkerExpressionSegment) each);
            } else if (each instanceof BinaryOperationExpression) {
                if (((BinaryOperationExpression) each).getRight() instanceof ParameterMarkerExpressionSegment) {
                    extractList.add((ParameterMarkerExpressionSegment) ((BinaryOperationExpression) each).getRight());
                }
            }
        }
        return extractList;
    }
}
