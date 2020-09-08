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

package org.apache.shardingsphere.masterslave.spring.boot.algorithm;

import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.spring.boot.registry.AbstractAlgorithmProvidedBeanRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

/**
 * Primary replica algorithm provided bean registry.
 */
public final class PrimaryReplicaAlgorithmProvidedBeanRegistry extends AbstractAlgorithmProvidedBeanRegistry {
    
    private static final String MASTER_SLAVE_ALGORITHMS = "spring.shardingsphere.rules.master-slave.load-balancers.";
    
    /**
     * Instantiates a new primary-replica algorithm provided bean registry.
     *
     * @param environment environment
     */
    public PrimaryReplicaAlgorithmProvidedBeanRegistry(final Environment environment) {
        super(environment);
    }
    
    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        registerBean(MASTER_SLAVE_ALGORITHMS, PrimaryReplicaLoadBalanceAlgorithm.class, registry);
    }
}
