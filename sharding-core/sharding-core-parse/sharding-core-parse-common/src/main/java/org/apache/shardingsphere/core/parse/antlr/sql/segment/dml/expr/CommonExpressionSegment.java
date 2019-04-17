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

package org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;

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
    
    private Object literals;
    
    @Override
    public SQLExpression getSQLExpression(final String sql) {
        if (-1 != placeholderIndex) {
            return new SQLPlaceholderExpression(placeholderIndex);
        }
        if (literals instanceof Number) {
            return new SQLNumberExpression((Number) literals);
        }
        if (literals instanceof String) {
            return new SQLTextExpression(literals.toString());
        }
        String value = sql.substring(startIndex, stopIndex + 1);
        if (DefaultKeyword.NULL.name().equalsIgnoreCase(value)) {
            return new SQLNumberExpression(null);
        }
        return new SQLTextExpression(value);
    } 
}
