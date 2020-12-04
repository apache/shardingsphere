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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

/**
 * Class based sharding algorithm.
 */
public class ClassBasedShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ComplexKeysShardingAlgorithm<Comparable<?>>, HintShardingAlgorithm<Comparable<?>> {

    private static final String STRATEGY_KEY = "strategy";

    private static final String ALGORITHM_CLASS_NAME_KEY = "algorithmClassName";

    private StandardShardingAlgorithm standardShardingAlgorithm;

    private ComplexKeysShardingAlgorithm complexKeysShardingAlgorithm;

    private HintShardingAlgorithm hintShardingAlgorithm;

    @Getter
    private ClassBasedShardingAlgorithmStrategy strategy;

    @Getter
    private String algorithmClassName;

    @Getter
    @Setter
    private Properties props = new Properties();

    @Override
    public void init() {
        String strategyStr = props.getProperty(STRATEGY_KEY);
        Preconditions.checkNotNull(strategyStr, "The props`%s` cannot be null when uses class based sharding strategy.", STRATEGY_KEY);

        strategy = ClassBasedShardingAlgorithmStrategy.valueFrom(strategyStr.trim());
        Preconditions.checkArgument(Objects.nonNull(strategy), "Unknown class based sharding strategy %s.", strategy);

        algorithmClassName = props.getProperty(ALGORITHM_CLASS_NAME_KEY);
        Preconditions.checkNotNull(algorithmClassName, "The props `%s` cannot be null when uses class based sharding strategy.", ALGORITHM_CLASS_NAME_KEY);

        createAlgorithmInstance(strategy, algorithmClassName);
    }

    private void createAlgorithmInstance(final ClassBasedShardingAlgorithmStrategy classBasedShardingAlgorithmStrategy, final String algorithmClassName) {
        if (ClassBasedShardingAlgorithmStrategy.STANDARD == classBasedShardingAlgorithmStrategy) {
            standardShardingAlgorithm = ClassBasedShardingAlgorithmFactory.newInstance(algorithmClassName, StandardShardingAlgorithm.class);
            return;
        }
        if (ClassBasedShardingAlgorithmStrategy.COMPLEX == classBasedShardingAlgorithmStrategy) {
            complexKeysShardingAlgorithm = ClassBasedShardingAlgorithmFactory.newInstance(algorithmClassName, ComplexKeysShardingAlgorithm.class);
            return;
        }
        if (ClassBasedShardingAlgorithmStrategy.HINT == classBasedShardingAlgorithmStrategy) {
            hintShardingAlgorithm = ClassBasedShardingAlgorithmFactory.newInstance(algorithmClassName, HintShardingAlgorithm.class);
            return;
        }
    }

    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        return standardShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return standardShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final ComplexKeysShardingValue<Comparable<?>> shardingValue) {
        return complexKeysShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final HintShardingValue<Comparable<?>> shardingValue) {
        return hintShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
    }

    @Override
    public String getType() {
        return "CLASS_BASED";
    }
}
