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

package org.apache.shardingsphere.readwritesplitting.spring.boot.algorithm;

import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.spring.boot.registry.AbstractAlgorithmProvidedBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

/**
 * Readwrite-splitting algorithm provided bean registry.
 */
public final class ReadwriteSplittingAlgorithmProvidedBeanRegistry extends AbstractAlgorithmProvidedBeanRegistry<ReadQueryLoadBalanceAlgorithm> {
    
    private static final String ALGORITHMS = "spring.shardingsphere.rules.readwrite-splitting.load-balancers.";
    
    public ReadwriteSplittingAlgorithmProvidedBeanRegistry(final Environment env) {
        super(env);
    }
    
    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) {
        registerBean(ALGORITHMS, ReadQueryLoadBalanceAlgorithm.class, registry);
    }
}
