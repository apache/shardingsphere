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

import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.group.ReadwriteSplittingGroup;
import org.apache.shardingsphere.readwritesplitting.group.type.StaticReadwriteSplittingGroup;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;

import java.util.Collection;
import java.util.HashSet;

/**
 * Readwrite-splitting data source group rule.
 */
@Getter
public final class ReadwriteSplittingDataSourceGroupRule {
    
    private final String name;
    
    private final TransactionalReadQueryStrategy transactionalReadQueryStrategy;
    
    private final LoadBalanceAlgorithm loadBalancer;
    
    private final ReadwriteSplittingGroup readwriteSplittingGroup;
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    public ReadwriteSplittingDataSourceGroupRule(final ReadwriteSplittingDataSourceGroupRuleConfiguration config,
                                                 final TransactionalReadQueryStrategy transactionalReadQueryStrategy, final LoadBalanceAlgorithm loadBalancer) {
        name = config.getName();
        this.transactionalReadQueryStrategy = transactionalReadQueryStrategy;
        this.loadBalancer = loadBalancer;
        readwriteSplittingGroup = createStaticReadwriteSplittingGroup(config);
    }
    
    private StaticReadwriteSplittingGroup createStaticReadwriteSplittingGroup(final ReadwriteSplittingDataSourceGroupRuleConfiguration config) {
        return new StaticReadwriteSplittingGroup(config.getWriteDataSourceName(), config.getReadDataSourceNames());
    }
    
    /**
     * Get write data source name.
     *
     * @return write data source name
     */
    public String getWriteDataSource() {
        return readwriteSplittingGroup.getWriteDataSource();
    }
    
    /**
     * Enable data source.
     *
     * @param dataSourceName data source name to be enabled.
     */
    public void enableDataSource(final String dataSourceName) {
        disabledDataSourceNames.remove(dataSourceName);
    }
    
    /**
     * Disable data source.
     *
     * @param dataSourceName data source name to be disabled.
     */
    public void disableDataSource(final String dataSourceName) {
        disabledDataSourceNames.add(dataSourceName);
    }
}
