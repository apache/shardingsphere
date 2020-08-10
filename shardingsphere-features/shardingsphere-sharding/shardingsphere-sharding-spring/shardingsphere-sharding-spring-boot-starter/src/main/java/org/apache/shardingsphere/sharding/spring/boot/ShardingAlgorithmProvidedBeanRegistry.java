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

package org.apache.shardingsphere.sharding.spring.boot;

import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.spring.boot.registry.AbstractAlgorithmProvidedBeanRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

/**
 * Sharding algorithm provided bean registry.
 */
public final class ShardingAlgorithmProvidedBeanRegistry extends AbstractAlgorithmProvidedBeanRegistry {
    
    private static final String SHARDING_ALGORITHMS = "spring.shardingsphere.rules.sharding.sharding-algorithms.";
    
    private static final String KEY_GENERATORS = "spring.shardingsphere.rules.sharding.key-generators.";
    
    /**
     * Instantiates a new Sharding algorithm provided bean registry.
     *
     * @param environment environment
     */
    public ShardingAlgorithmProvidedBeanRegistry(final Environment environment) {
        super(environment);
    }
    
    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        registerBean(SHARDING_ALGORITHMS, ShardingAlgorithm.class, registry);
        registerBean(KEY_GENERATORS, KeyGenerateAlgorithm.class, registry);
    }
}
