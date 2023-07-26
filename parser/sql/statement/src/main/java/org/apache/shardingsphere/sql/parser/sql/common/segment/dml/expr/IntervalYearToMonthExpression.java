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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Interval year to month expression.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class IntervalYearToMonthExpression implements ExpressionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String year;
    
    private final String to;
    
    private final String month;
    
    @Setter
    private Integer leadingFieldPrecision;
    
    @Override
    public String getText() {
        return null;
    }
}
