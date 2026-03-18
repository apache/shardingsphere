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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.List;
import java.util.StringJoiner;

/**
 * Insert value.
 */
@Getter
public class InsertValue {
    
    private final List<ExpressionSegment> values;
    
    public InsertValue(final List<ExpressionSegment> values) {
        ShardingSpherePreconditions.checkNotEmpty(values, () -> new UnsupportedSQLOperationException("Insert values can not be empty"));
        this.values = values;
    }
    
    @Override
    public final String toString() {
        StringJoiner result = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < values.size(); i++) {
            result.add(getValue(i));
        }
        return result.toString();
    }
    
    private String getValue(final int index) {
        ExpressionSegment expressionSegment = values.get(index);
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            ParameterMarkerExpressionSegment segment = (ParameterMarkerExpressionSegment) expressionSegment;
            return ParameterMarkerType.QUESTION == segment.getParameterMarkerType() ? "?" : "$" + (segment.getParameterMarkerIndex() + 1);
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) expressionSegment).getLiterals();
            return getLiteralValue((LiteralExpressionSegment) expressionSegment, literals);
        }
        return expressionSegment.getText();
    }
    
    private String getLiteralValue(final LiteralExpressionSegment expressionSegment, final Object literals) {
        if (null == literals) {
            return "NULL";
        }
        return literals instanceof String ? "'" + expressionSegment.getLiterals() + "'" : String.valueOf(literals);
    }
}
