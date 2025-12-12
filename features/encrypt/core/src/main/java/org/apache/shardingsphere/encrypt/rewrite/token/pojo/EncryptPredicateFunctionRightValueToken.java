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

package org.apache.shardingsphere.encrypt.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Predicate in right value token for encrypt.
 */
public final class EncryptPredicateFunctionRightValueToken extends SQLToken implements Substitutable {
    
    private static final String COMMA_SEPARATOR = ", ";
    
    @Getter
    private final int stopIndex;
    
    private final String functionName;
    
    private final Collection<ExpressionSegment> parameters;
    
    private final Map<Integer, Object> indexValues;
    
    private final Collection<Integer> paramMarkerIndexes;
    
    public EncryptPredicateFunctionRightValueToken(final int startIndex, final int stopIndex, final String functionName, final Collection<ExpressionSegment> parameters,
                                                   final Map<Integer, Object> indexValues, final Collection<Integer> paramMarkerIndexes) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.functionName = functionName;
        this.parameters = parameters;
        this.indexValues = indexValues;
        this.paramMarkerIndexes = paramMarkerIndexes;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        AtomicInteger parameterIndex = new AtomicInteger();
        appendFunctionSegment(functionName, parameters, result, parameterIndex);
        return result.toString();
    }
    
    private void appendFunctionSegment(final String functionName, final Collection<ExpressionSegment> parameters, final StringBuilder builder, final AtomicInteger parameterIndex) {
        builder.append(functionName).append("(");
        for (ExpressionSegment each : parameters) {
            if (each instanceof FunctionSegment) {
                appendFunctionSegment(((FunctionSegment) each).getFunctionName(), ((FunctionSegment) each).getParameters(), builder, parameterIndex);
                builder.append(COMMA_SEPARATOR);
            } else {
                appendRewrittenParameters(builder, parameterIndex.getAndIncrement());
            }
        }
        if (builder.toString().endsWith(COMMA_SEPARATOR)) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(')');
    }
    
    private void appendRewrittenParameters(final StringBuilder builder, final int parameterIndex) {
        if (paramMarkerIndexes.contains(parameterIndex)) {
            builder.append('?');
        } else {
            if (indexValues.get(parameterIndex) instanceof String) {
                builder.append('\'').append(indexValues.get(parameterIndex)).append('\'');
            } else {
                builder.append(indexValues.get(parameterIndex));
            }
        }
        builder.append(COMMA_SEPARATOR);
    }
}
