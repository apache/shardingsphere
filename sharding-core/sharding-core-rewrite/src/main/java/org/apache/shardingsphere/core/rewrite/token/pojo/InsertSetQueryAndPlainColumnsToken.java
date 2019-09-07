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

package org.apache.shardingsphere.core.rewrite.token.pojo;

import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.List;

/**
 * Insert set query and plain columns token.
 *
 * @author panjuan
 */
public final class InsertSetQueryAndPlainColumnsToken extends SQLToken implements Attachable {
    
    private final List<String> columnNames;
    
    private final List<ExpressionSegment> values;
    
    public InsertSetQueryAndPlainColumnsToken(final int startIndex, final List<String> columnNames, final List<ExpressionSegment> values) {
        super(startIndex);
        this.columnNames = columnNames;
        this.values = values;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            result.append(String.format(", %s = %s", columnNames.get(i), getValue(i)));
        }
        return result.toString();
    }
    
    private String getValue(final int index) {
        ExpressionSegment expressionSegment = values.get(index);
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return "?";
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) expressionSegment).getLiterals();
            return literals instanceof String ? String.format("'%s'", literals) : literals.toString();
        }
        return ((ComplexExpressionSegment) expressionSegment).getText();
    }
}
