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

package org.apache.shardingsphere.traffic.api.config;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Traffic rule configuration.
 */
@Getter
public final class TrafficRuleConfiguration implements GlobalRuleConfiguration {
    
    private final Collection<TrafficStrategyConfiguration> trafficStrategies = new LinkedList<>();
    
    private final Map<String, AlgorithmConfiguration> trafficAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
}
