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

package org.apache.shardingsphere.sharding.algorithm.sharding.hint;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.exception.data.NullShardingValueException;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Hint inline sharding algorithm.
 */
public final class HintInlineShardingAlgorithm implements HintShardingAlgorithm<Comparable<?>> {
    
    private static final String ALGORITHM_EXPRESSION_KEY = "algorithm-expression";
    
    private static final String DEFAULT_ALGORITHM_EXPRESSION = "${value}";
    
    private static final String HINT_INLINE_VALUE_PROPERTY_NAME = "value";
    
    private String algorithmExpression;
    
    @Override
    public void init(final Properties props) {
        algorithmExpression = getAlgorithmExpression(props);
    }
    
    private String getAlgorithmExpression(final Properties props) {
        String algorithmExpression = props.getProperty(ALGORITHM_EXPRESSION_KEY, DEFAULT_ALGORITHM_EXPRESSION);
        ShardingSpherePreconditions.checkNotNull(algorithmExpression, () -> new AlgorithmInitializationException(this, "Inline sharding algorithm expression can not be null."));
        return InlineExpressionParserFactory.newInstance(algorithmExpression.trim()).handlePlaceHolder();
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final HintShardingValue<Comparable<?>> shardingValue) {
        return shardingValue.getValues().isEmpty() ? availableTargetNames : shardingValue.getValues().stream().map(this::doSharding).collect(Collectors.toList());
    }
    
    private String doSharding(final Comparable<?> shardingValue) {
        ShardingSpherePreconditions.checkNotNull(shardingValue, NullShardingValueException::new);
        return InlineExpressionParserFactory.newInstance(algorithmExpression).evaluateWithArgs(Collections.singletonMap(HINT_INLINE_VALUE_PROPERTY_NAME, shardingValue));
    }
    
    @Override
    public String getType() {
        return "HINT_INLINE";
    }
}
