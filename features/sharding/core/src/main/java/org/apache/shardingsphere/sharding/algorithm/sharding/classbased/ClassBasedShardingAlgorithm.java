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

package org.apache.shardingsphere.sharding.algorithm.sharding.classbased;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * Class based sharding algorithm.
 */
@SuppressWarnings("rawtypes")
public final class ClassBasedShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ComplexKeysShardingAlgorithm<Comparable<?>>, HintShardingAlgorithm<Comparable<?>> {
    
    private static final String STRATEGY_KEY = "strategy";
    
    private static final String ALGORITHM_CLASS_NAME_KEY = "algorithmClassName";
    
    private ClassBasedShardingAlgorithmStrategyType strategy;
    
    private String algorithmClassName;
    
    private StandardShardingAlgorithm standardShardingAlgorithm;
    
    private ComplexKeysShardingAlgorithm complexKeysShardingAlgorithm;
    
    private HintShardingAlgorithm hintShardingAlgorithm;
    
    @Override
    public void init(final Properties props) {
        strategy = getStrategy(props);
        algorithmClassName = getAlgorithmClassName(props);
        initAlgorithmInstance(props);
    }
    
    private ClassBasedShardingAlgorithmStrategyType getStrategy(final Properties props) {
        String strategy = props.getProperty(STRATEGY_KEY);
        ShardingSpherePreconditions.checkNotEmpty(strategy,
                () -> new AlgorithmInitializationException(this, "Properties `%s` can not be null or empty when uses class based sharding strategy", STRATEGY_KEY));
        String shardingAlgorithmStrategyType = strategy.toUpperCase().trim();
        ShardingSpherePreconditions.checkState(
                Arrays.stream(ClassBasedShardingAlgorithmStrategyType.values()).anyMatch(each -> each.name().equals(shardingAlgorithmStrategyType)),
                () -> new AlgorithmInitializationException(this, "Unsupported sharding strategy `%s`", strategy));
        return ClassBasedShardingAlgorithmStrategyType.valueOf(shardingAlgorithmStrategyType);
    }
    
    private String getAlgorithmClassName(final Properties props) {
        String result = props.getProperty(ALGORITHM_CLASS_NAME_KEY);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new AlgorithmInitializationException(this, "Sharding algorithm class name can not be null or empty"));
        return result;
    }
    
    private void initAlgorithmInstance(final Properties props) {
        switch (strategy) {
            case STANDARD:
                standardShardingAlgorithm = ClassBasedShardingAlgorithmFactory.newInstance(algorithmClassName, StandardShardingAlgorithm.class, props);
                break;
            case COMPLEX:
                complexKeysShardingAlgorithm = ClassBasedShardingAlgorithmFactory.newInstance(algorithmClassName, ComplexKeysShardingAlgorithm.class, props);
                break;
            case HINT:
                hintShardingAlgorithm = ClassBasedShardingAlgorithmFactory.newInstance(algorithmClassName, HintShardingAlgorithm.class, props);
                break;
            default:
                break;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        return standardShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return standardShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final ComplexKeysShardingValue<Comparable<?>> shardingValue) {
        return complexKeysShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final HintShardingValue<Comparable<?>> shardingValue) {
        return hintShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }
    
    @Override
    public String getType() {
        return "CLASS_BASED";
    }
}
