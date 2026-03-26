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
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Insert value.
 */
@Getter
public class InsertValue {
    
    private final List<ExpressionSegment> values;
    
    private final Map<Integer, SQLToken> substitutedSQLTokens = new HashMap<>();
    
    private final Map<Integer, Collection<SQLToken>> addedSQLTokens = new HashMap<>();
    
    private final Map<Integer, Collection<AddedItem>> orderedAddedItems = new HashMap<>();
    
    private int addedValuesCount;
    
    private int addedParameterMarkerExpressionCount;
    
    public InsertValue(final List<ExpressionSegment> values) {
        ShardingSpherePreconditions.checkNotEmpty(values, () -> new UnsupportedSQLOperationException("Insert values can not be empty"));
        this.values = values;
    }
    
    /**
     * Put substituted SQL token.
     *
     * @param index index
     * @param sqlToken SQL token
     */
    public void putSubstitutedSQLToken(final int index, final SQLToken sqlToken) {
        substitutedSQLTokens.put(index, sqlToken);
    }
    
    /**
     * Add added SQL token.
     *
     * @param index index
     * @param sqlToken SQL token
     * @param parameterMarkerCount parameter marker count
     */
    public void addAddedSQLToken(final int index, final SQLToken sqlToken, final int parameterMarkerCount) {
        addedValuesCount += 1;
        addedParameterMarkerExpressionCount += parameterMarkerCount;
        addedSQLTokens.computeIfAbsent(index, unused -> new LinkedList<>()).add(sqlToken);
        orderedAddedItems.computeIfAbsent(index, unused -> new LinkedList<>()).add(AddedItem.valueOf(sqlToken));
    }
    
    @Override
    public final String toString() {
        StringJoiner result = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < values.size(); i++) {
            result.add(getValue(i));
            getAddedItems(i, result);
        }
        return result.toString();
    }
    
    private void getAddedItems(final int index, final StringJoiner joiner) {
        Collection<AddedItem> currentAddedItems = orderedAddedItems.get(index);
        if (null != currentAddedItems) {
            for (AddedItem each : currentAddedItems) {
                joiner.add(each.isExpressionSegment() ? doGetValue(each.getExpressionSegment()) : each.getSqlToken().toString());
            }
        }
    }
    
    /**
     * Get value.
     *
     * @param index index
     * @return value
     */
    public String getValue(final int index) {
        SQLToken substitutedSQLToken = substitutedSQLTokens.get(index);
        if (null != substitutedSQLToken) {
            return substitutedSQLToken.toString();
        }
        return doGetValue(values.get(index));
    }
    
    private String doGetValue(final ExpressionSegment expressionSegment) {
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
    
    @Getter
    private static final class AddedItem {
        
        private final ExpressionSegment expressionSegment;
        
        private final SQLToken sqlToken;
        
        private AddedItem(final ExpressionSegment expressionSegment, final SQLToken sqlToken) {
            this.expressionSegment = expressionSegment;
            this.sqlToken = sqlToken;
        }
        
        private static AddedItem valueOf(final SQLToken sqlToken) {
            return new AddedItem(null, sqlToken);
        }
        
        private boolean isExpressionSegment() {
            return null != expressionSegment;
        }
    }
}
