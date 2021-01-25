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

package org.apache.shardingsphere.replicaquery.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Replica query data source rule.
 */
@Getter
public final class ReplicaQueryDataSourceRule {
    
    private final String name;
    
    private final String primaryDataSourceName;
    
    private final List<String> replicaDataSourceNames;
    
    private final ReplicaLoadBalanceAlgorithm loadBalancer;
    
    @Getter(AccessLevel.NONE)
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    public ReplicaQueryDataSourceRule(final ReplicaQueryDataSourceRuleConfiguration config, final ReplicaLoadBalanceAlgorithm loadBalancer) {
        checkConfiguration(config);
        name = config.getName();
        primaryDataSourceName = config.getPrimaryDataSourceName();
        replicaDataSourceNames = config.getReplicaDataSourceNames();
        this.loadBalancer = loadBalancer;
    }
    
    private void checkConfiguration(final ReplicaQueryDataSourceRuleConfiguration config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getName()), "Name is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getPrimaryDataSourceName()), "Primary data source name is required.");
        Preconditions.checkArgument(null != config.getReplicaDataSourceNames() && !config.getReplicaDataSourceNames().isEmpty(), "Replica data source names are required.");
    }
    
    /**
     * Get replica data source names.
     *
     * @return available replica data source names
     */
    public List<String> getReplicaDataSourceNames() {
        return replicaDataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each)).collect(Collectors.toList());
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
     * Get data source mapper.
     *
     * @return data source mapper
     */
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(1, 1);
        Collection<String> actualDataSourceNames = new LinkedList<>();
        actualDataSourceNames.add(primaryDataSourceName);
        actualDataSourceNames.addAll(replicaDataSourceNames);
        result.put(name, actualDataSourceNames);
        return result;
    }
}
