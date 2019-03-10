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

package org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;

/**
 * Common expression segment.
 * 
 * @author duhongjun
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class CommonExpressionSegment implements ExpressionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private int placeholderIndex = -1;
    
    private Number value;
    
    private boolean text;
    
    @Override
    public Optional<SQLExpression> convertToSQLExpression(final String sql) {
        if (-1 != placeholderIndex) {
            return Optional.<SQLExpression>of(new SQLPlaceholderExpression(placeholderIndex));
        }
        if (null != value) {
            return Optional.<SQLExpression>of(new SQLNumberExpression(value));
        }
        if (text) {
            return Optional.<SQLExpression>of(new SQLTextExpression(sql.substring(startIndex + 1, stopIndex)));
        }
        return Optional.<SQLExpression>of(new SQLTextExpression(sql.substring(startIndex, stopIndex + 1)));
    } 
}
