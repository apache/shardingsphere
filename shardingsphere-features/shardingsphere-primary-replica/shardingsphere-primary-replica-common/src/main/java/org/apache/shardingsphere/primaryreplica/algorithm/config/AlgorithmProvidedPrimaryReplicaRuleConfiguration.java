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

package org.apache.shardingsphere.primaryreplica.algorithm.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.spi.PrimaryReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.Map;

/**
 * Algorithm provided primary-replica rule configuration.
 */
@Getter
@Setter
public final class AlgorithmProvidedPrimaryReplicaRuleConfiguration implements RuleConfiguration {
    
    private Collection<PrimaryReplicaDataSourceRuleConfiguration> dataSources = new LinkedList<>();
    
    private Map<String, PrimaryReplicaLoadBalanceAlgorithm> loadBalanceAlgorithms = new LinkedHashMap<>();
    
    public AlgorithmProvidedPrimaryReplicaRuleConfiguration() {
    }
    
    public AlgorithmProvidedPrimaryReplicaRuleConfiguration(final Collection<PrimaryReplicaDataSourceRuleConfiguration> dataSources,
                                                         final Map<String, PrimaryReplicaLoadBalanceAlgorithm> loadBalanceAlgorithms) {
        this.dataSources = dataSources;
        this.loadBalanceAlgorithms = loadBalanceAlgorithms;
    }
}
