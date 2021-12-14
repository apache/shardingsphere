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

package org.apache.shardingsphere.dbdiscovery.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database discovery data source rule.
 */
@Getter
public final class DatabaseDiscoveryDataSourceRule {
    
    private final String name;
    
    private final List<String> dataSourceNames;
    
    private final Properties heartbeatProps;
    
    private final DatabaseDiscoveryType databaseDiscoveryType;
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    private String primaryDataSourceName;
    
    public DatabaseDiscoveryDataSourceRule(final DatabaseDiscoveryDataSourceRuleConfiguration config, final Properties props, final DatabaseDiscoveryType databaseDiscoveryType) {
        checkConfiguration(config);
        name = config.getName();
        dataSourceNames = config.getDataSourceNames();
        this.heartbeatProps = props;
        this.databaseDiscoveryType = databaseDiscoveryType;
    }
    
    private void checkConfiguration(final DatabaseDiscoveryDataSourceRuleConfiguration config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getName()), "Name is required.");
        Preconditions.checkArgument(null != config.getDataSourceNames() && !config.getDataSourceNames().isEmpty(), "Data source names are required.");
    }
    
    /**
     * Get data source names.
     *
     * @return available data source names
     */
    public List<String> getDataSourceNames() {
        return dataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each)).collect(Collectors.toList());
    }
    
    /**
     * Get replica data source names.
     *
     * @return available replica data source names
     */
    public List<String> getReplicaDataSourceNames() {
        return dataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each) && !primaryDataSourceName.equals(each)).collect(Collectors.toList());
    }
    
    /**
     * Disable data source.
     *
     * @param dataSourceName data source name to be disabled
     */
    public void disableDataSource(final String dataSourceName) {
        disabledDataSourceNames.add(dataSourceName);
    }
    
    /**
     * Enable data source.
     *
     * @param dataSourceName data source name to be enabled
     */
    public void enableDataSource(final String dataSourceName) {
        disabledDataSourceNames.remove(dataSourceName);
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
        Map<String, Collection<String>> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, Collections.singletonList(each));
        }
        return result;
    }
}
