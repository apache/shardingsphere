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

package org.apache.shardingsphere.ha.spring.boot.algorithm;

import org.apache.shardingsphere.ha.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.spring.boot.registry.AbstractAlgorithmProvidedBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

/**
 * HA algorithm provided bean registry.
 */
public final class HAAlgorithmProvidedBeanRegistry extends AbstractAlgorithmProvidedBeanRegistry<ReplicaLoadBalanceAlgorithm> {
    
    private static final String ALGORITHMS = "spring.shardingsphere.rules.ha.load-balancers.";
    
    /**
     * Instantiates a new HA algorithm provided bean registry.
     *
     * @param environment environment
     */
    public HAAlgorithmProvidedBeanRegistry(final Environment environment) {
        super(environment);
    }
    
    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) {
        registerBean(ALGORITHMS, ReplicaLoadBalanceAlgorithm.class, registry);
    }
}
