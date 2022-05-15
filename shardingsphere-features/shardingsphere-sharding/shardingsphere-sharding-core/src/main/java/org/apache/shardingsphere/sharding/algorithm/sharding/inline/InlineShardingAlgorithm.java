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

package org.apache.shardingsphere.sharding.algorithm.sharding.inline;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.util.Expando;
import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.expr.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Inline sharding algorithm.
 */
public final class InlineShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {
    
    private static final String ALGORITHM_EXPRESSION_KEY = "algorithm-expression";
    
    private static final String ALLOW_RANGE_QUERY_KEY = "allow-range-query-with-inline-sharding";
    
    @Getter
    private Properties props;
    
    private String algorithmExpression;
    
    private boolean allowRangeQuery;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        algorithmExpression = getAlgorithmExpression(props);
        allowRangeQuery = isAllowRangeQuery(props);
    }
    
    private String getAlgorithmExpression(final Properties props) {
        String expression = props.getProperty(ALGORITHM_EXPRESSION_KEY);
        Preconditions.checkState(null != expression && !expression.isEmpty(), "Inline sharding algorithm expression cannot be null or empty.");
        return InlineExpressionParser.handlePlaceHolder(expression.trim());
    }
    
    private boolean isAllowRangeQuery(final Properties props) {
        return Boolean.parseBoolean(props.getOrDefault(ALLOW_RANGE_QUERY_KEY, Boolean.FALSE.toString()).toString());
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        Closure<?> closure = createClosure();
        closure.setProperty(shardingValue.getColumnName(), shardingValue.getValue());
        return getTargetShardingNode(closure, shardingValue.getColumnName());
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        if (allowRangeQuery) {
            return availableTargetNames;
        }
        throw new UnsupportedOperationException("Since the property of `" + ALLOW_RANGE_QUERY_KEY + "` is false, inline sharding algorithm can not tackle with range query.");
    }
    
    private Closure<?> createClosure() {
        Closure<?> result = new InlineExpressionParser(algorithmExpression).evaluateClosure().rehydrate(new Expando(), null, null);
        result.setResolveStrategy(Closure.DELEGATE_ONLY);
        return result;
    }
    
    private String getTargetShardingNode(final Closure<?> closure, final String columnName) {
        try {
            return closure.call().toString();
        } catch (final MissingMethodException | NullPointerException ex) {
            throw new ShardingSphereException("Inline sharding algorithms expression `%s` and sharding column `%s` not match.", algorithmExpression, columnName);
        }
    }
    
    @Override
    public String getType() {
        return "INLINE";
    }
}
