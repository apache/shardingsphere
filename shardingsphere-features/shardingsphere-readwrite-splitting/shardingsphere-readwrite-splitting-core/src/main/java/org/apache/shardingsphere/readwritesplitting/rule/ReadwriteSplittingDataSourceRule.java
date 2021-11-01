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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.aware.DataSourceNameAwareFactory;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting data source rule.
 */
@Getter
public final class ReadwriteSplittingDataSourceRule {
    
    private final String name;
    
    private final String autoAwareDataSourceName;
    
    private final String writeDataSourceName;
    
    private final List<String> readDataSourceNames;
    
    private final ReplicaLoadBalanceAlgorithm loadBalancer;
    
    @Getter(AccessLevel.NONE)
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    public ReadwriteSplittingDataSourceRule(final ReadwriteSplittingDataSourceRuleConfiguration config, final ReplicaLoadBalanceAlgorithm loadBalancer) {
        checkConfiguration(config);
        name = config.getName();
        autoAwareDataSourceName = config.getAutoAwareDataSourceName();
        writeDataSourceName = config.getWriteDataSourceName();
        readDataSourceNames = config.getReadDataSourceNames();
        this.loadBalancer = loadBalancer;
    }
    
    private void checkConfiguration(final ReadwriteSplittingDataSourceRuleConfiguration config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getName()), "Name is required.");
        if (Strings.isNullOrEmpty(config.getAutoAwareDataSourceName())) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getWriteDataSourceName()), "Write data source name is required.");
            Preconditions.checkArgument(null != config.getReadDataSourceNames() && !config.getReadDataSourceNames().isEmpty(), "Read data source names are required.");
        }
    }
    
    /**
     * Get read data source names.
     *
     * @return available read data source names
     */
    public List<String> getReadDataSourceNames() {
        return readDataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each)).collect(Collectors.toList());
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
        if (Strings.isNullOrEmpty(autoAwareDataSourceName)) {
            actualDataSourceNames.add(writeDataSourceName);
            actualDataSourceNames.addAll(readDataSourceNames);
        } else {
            Optional<DataSourceNameAware> dataSourceNameAware = DataSourceNameAwareFactory.getInstance().getDataSourceNameAware();
            if (dataSourceNameAware.isPresent()) {
                actualDataSourceNames.add(dataSourceNameAware.get().getPrimaryDataSourceName(autoAwareDataSourceName));
                actualDataSourceNames.addAll(dataSourceNameAware.get().getReplicaDataSourceNames(autoAwareDataSourceName));
            }
        }
        result.put(name, actualDataSourceNames);
        return result;
    }
}
