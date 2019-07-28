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

import java.util.List;

/**
 * Insert set query and plain columns token.
 *
 * @author panjuan
 */
@Getter
public final class InsertSetQueryAndPlainColumnsToken extends SQLToken implements Attachable {
    
    private final List<String> columnNames;
    
    private final List<ExpressionSegment> columnValues;
    
    public InsertSetQueryAndPlainColumnsToken(final int startIndex, final List<String> columnNames, final List<ExpressionSegment> columnValues) {
        super(startIndex);
        this.columnNames = columnNames;
        this.columnValues = columnValues;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            result.append(String.format(", %s = %s", columnNames.get(i), getColumnValue(i)));
        }
        return result.toString();
    }
    
    private String getColumnValue(final int index) {
        ExpressionSegment columnValue = columnValues.get(index);
        if (columnValue instanceof ParameterMarkerExpressionSegment) {
            return "?";
        } else if (columnValue instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) columnValue).getLiterals();
            return literals instanceof String ? String.format("'%s'", literals) : literals.toString();
        }
        return ((ComplexExpressionSegment) columnValue).getText();
    }
}
