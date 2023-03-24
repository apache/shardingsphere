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

package org.apache.shardingsphere.readwritesplitting.rule;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.strategy.ReadwriteSplittingStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.ReadwriteSplittingStrategyFactory;
import org.apache.shardingsphere.readwritesplitting.strategy.type.DynamicReadwriteSplittingStrategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Readwrite-splitting data source rule.
 */
@Getter
public final class ReadwriteSplittingDataSourceRule {
    
    private final String name;
    
    private final ReadQueryLoadBalanceAlgorithm loadBalancer;
    
    private final ReadwriteSplittingStrategy readwriteSplittingStrategy;
    
    @Getter(AccessLevel.NONE)
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    public ReadwriteSplittingDataSourceRule(final ReadwriteSplittingDataSourceRuleConfiguration config, final ReadQueryLoadBalanceAlgorithm loadBalancer,
                                            final Collection<ShardingSphereRule> builtRules) {
        name = config.getName();
        this.loadBalancer = loadBalancer;
        readwriteSplittingStrategy = ReadwriteSplittingStrategyFactory.newInstance(config, builtRules);
    }
    
    /**
     * Get write data source name.
     *
     * @return write data source name
     */
    public String getWriteDataSource() {
        return readwriteSplittingStrategy.getWriteDataSource();
    }
    
    /**
     * Update disabled data source names.
     *
     * @param dataSourceName data source name
     * @param isDisabled is disabled
     */
    public void updateDisabledDataSourceNames(final String dataSourceName, final boolean isDisabled) {
        if (isDisabled) {
            disabledDataSourceNames.add(dataSourceName);
        } else {
            disabledDataSourceNames.remove(dataSourceName);
        }
    }
    
    /**
     * Get enabled replica data sources.
     *
     * @return enabled replica data sources
     */
    public List<String> getEnabledReplicaDataSources() {
        List<String> result = readwriteSplittingStrategy.getReadDataSources();
        if (readwriteSplittingStrategy instanceof DynamicReadwriteSplittingStrategy) {
            return result;
        }
        if (!disabledDataSourceNames.isEmpty()) {
            result = new LinkedList<>(result);
            result.removeIf(disabledDataSourceNames::contains);
        }
        return result;
    }
}
