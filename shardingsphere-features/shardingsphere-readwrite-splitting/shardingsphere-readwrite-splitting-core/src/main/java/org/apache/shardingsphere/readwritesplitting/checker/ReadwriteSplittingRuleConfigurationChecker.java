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

package org.apache.shardingsphere.readwritesplitting.checker;

import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Readwrite-splitting rule configuration checker.
 */
public final class ReadwriteSplittingRuleConfigurationChecker extends AbstractReadwriteSplittingRuleConfigurationChecker<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    protected Collection<ReadwriteSplittingDataSourceRuleConfiguration> getDataSources(final ReadwriteSplittingRuleConfiguration config) {
        return config.getDataSources();
    }
    
    @Override
    protected Map<String, ReadQueryLoadBalanceAlgorithm> getLoadBalancer(final ReadwriteSplittingRuleConfiguration config) {
        Map<String, ReadQueryLoadBalanceAlgorithm> result = new LinkedHashMap<>(config.getLoadBalancers().size(), 1);
        config.getLoadBalancers().forEach((key, value) -> result.put(key, ReadQueryLoadBalanceAlgorithmFactory.newInstance(value)));
        return result;
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
