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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.IntervalDayToSecondExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.IntervalYearToMonthExpression;

/**
 * Between expression.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class IntervalExpressionProjection implements ExpressionSegment, ProjectionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final ExpressionSegment left;
    
    private final ExpressionSegment minus;
    
    private final ExpressionSegment right;
    
    @Setter
    private IntervalDayToSecondExpression dayToSecondExpression;
    
    @Setter
    private IntervalYearToMonthExpression yearToMonthExpression;
    
    @Override
    public String getText() {
        return minus.getText();
    }
    
    @Override
    public String getColumnLabel() {
        return getText();
    }
}
