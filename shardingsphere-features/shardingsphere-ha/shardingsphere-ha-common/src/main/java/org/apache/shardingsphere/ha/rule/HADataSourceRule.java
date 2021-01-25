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

package org.apache.shardingsphere.ha.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.spi.HAType;
import org.apache.shardingsphere.ha.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HA data source rule.
 */
@Getter
@Slf4j
public final class HADataSourceRule {
    
    private final String name;
    
    private final List<String> dataSourceNames;
    
    private final ReplicaLoadBalanceAlgorithm loadBalancer;
    
    private final boolean replicaQuery;
    
    private final HAType haType;
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    private String primaryDataSourceName;
    
    public HADataSourceRule(final HADataSourceRuleConfiguration config, final ReplicaLoadBalanceAlgorithm loadBalancer, final HAType haType) {
        checkConfiguration(config);
        name = config.getName();
        dataSourceNames = config.getDataSourceNames();
        this.loadBalancer = loadBalancer;
        replicaQuery = config.isReplicaQuery();
        this.haType = haType;
    }
    
    private void checkConfiguration(final HADataSourceRuleConfiguration config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getName()), "Name is required.");
        Preconditions.checkArgument(null != config.getDataSourceNames() && !config.getDataSourceNames().isEmpty(), "Replica data source names are required.");
    }
    
    /**
     * Get replica data source names.
     *
     * @return available replica data source names
     */
    public List<String> getDataSourceNames() {
        return dataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each)).collect(Collectors.toList());
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
     * Update primary data source name.
     *
     * @param dataSourceName data source name
     */
    public void updatePrimaryDataSourceName(final String dataSourceName) {
        primaryDataSourceName = dataSourceName;
    }
    
    /**
     * Get data source mapper.
     *
     * @return data source mapper
     */
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(1, 1);
        Collection<String> actualDataSourceNames = new LinkedList<>(dataSourceNames);
        result.put(name, actualDataSourceNames);
        return result;
    }
}
