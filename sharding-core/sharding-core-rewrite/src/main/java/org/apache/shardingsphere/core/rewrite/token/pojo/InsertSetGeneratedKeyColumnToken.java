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

import lombok.Getter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

/**
 * Insert set generated key column token.
 *
 * @author panjuan
 */
@Getter
public final class InsertSetGeneratedKeyColumnToken extends SQLToken implements Attachable {
    
    private final String columnName;
    
    private final ExpressionSegment value;
    
    public InsertSetGeneratedKeyColumnToken(final int startIndex, final String columnName, final ExpressionSegment value) {
        super(startIndex);
        this.columnName = columnName;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.format(", %s = %s", columnName, getLiteralOfValue());
    }
    
    private String getLiteralOfValue() {
        if (value instanceof ParameterMarkerExpressionSegment) {
            return "?";
        }
        if (value instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) value).getLiterals();
            return literals instanceof String ? String.format("'%s'", literals) : literals.toString();
        }
        return ((ComplexExpressionSegment) value).getText();
    }
}
