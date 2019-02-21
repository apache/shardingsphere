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

package org.apache.shardingsphere.core.parsing.parser.context.condition;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Condition.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class Condition {
    
    private final Column column;
    
    private final ShardingOperator operator;
    
    @Setter
    private String compareOperator;
    
    private final Map<Integer, Comparable<?>> positionValueMap = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    protected Condition() {
        column = null;
        operator = null;
    }
    
    public Condition(final Column column, final SQLExpression sqlExpression) {
        this(column, ShardingOperator.EQUAL);
        init(sqlExpression, 0);
    }
    
    public Condition(final Column column, final String compareOperator, final SQLExpression sqlExpression) {
        this.column = column;
        this.compareOperator = compareOperator;
        if (Symbol.EQ.getLiterals().equals(compareOperator)) {
            operator = ShardingOperator.EQUAL;
        } else {
            operator = null;
        }
        init(sqlExpression, 0);
    }
    
    public Condition(final Column column, final SQLExpression beginSQLExpression, final SQLExpression endSQLExpression) {
        this(column, ShardingOperator.BETWEEN);
        init(beginSQLExpression, 0);
        init(endSQLExpression, 1);
    }
    
    public Condition(final Column column, final List<SQLExpression> sqlExpressions) {
        this(column, ShardingOperator.IN);
        int count = 0;
        for (SQLExpression each : sqlExpressions) {
            init(each, count);
            count++;
        }
    }
    
    private void init(final SQLExpression sqlExpression, final int position) {
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            positionIndexMap.put(position, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else if (sqlExpression instanceof SQLTextExpression) {
            positionValueMap.put(position, ((SQLTextExpression) sqlExpression).getText());
        } else if (sqlExpression instanceof SQLNumberExpression) {
            positionValueMap.put(position, (Comparable) ((SQLNumberExpression) sqlExpression).getNumber());
        }
    }
    
    /**
     * Get condition values.
     *
     * @param parameters parameters
     * @return condition values
     */
    public List<Comparable<?>> getConditionValues(final List<?> parameters) {
        List<Comparable<?>> result = new LinkedList<>(positionValueMap.values());
        for (Entry<Integer, Integer> entry : positionIndexMap.entrySet()) {
            Object parameter = parameters.get(entry.getValue());
            if (!(parameter instanceof Comparable<?>)) {
                throw new ShardingException("Parameter `%s` should extends Comparable for sharding value.", parameter);
            }
            if (entry.getKey() < result.size()) {
                result.add(entry.getKey(), (Comparable<?>) parameter);
            } else {
                result.add((Comparable<?>) parameter);
            }
        }
        return result;
    }
}
