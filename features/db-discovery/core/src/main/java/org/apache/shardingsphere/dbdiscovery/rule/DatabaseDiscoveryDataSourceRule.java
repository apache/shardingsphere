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
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database discovery data source rule.
 */
@Getter
public final class DatabaseDiscoveryDataSourceRule {
    
    private final String groupName;
    
    private final List<String> dataSourceNames;
    
    private final Properties heartbeatProps;
    
    private final DatabaseDiscoveryProviderAlgorithm databaseDiscoveryProviderAlgorithm;
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    private volatile String primaryDataSourceName;
    
    public DatabaseDiscoveryDataSourceRule(final DatabaseDiscoveryDataSourceRuleConfiguration config,
                                           final Properties props, final DatabaseDiscoveryProviderAlgorithm databaseDiscoveryProviderAlgorithm) {
        checkConfiguration(config);
        groupName = config.getGroupName();
        dataSourceNames = config.getDataSourceNames();
        this.heartbeatProps = props;
        this.databaseDiscoveryProviderAlgorithm = databaseDiscoveryProviderAlgorithm;
    }
    
    private void checkConfiguration(final DatabaseDiscoveryDataSourceRuleConfiguration config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getGroupName()), "Group name is required.");
        Preconditions.checkArgument(null != config.getDataSourceNames() && !config.getDataSourceNames().isEmpty(), "Data source names are required.");
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
     * Change primary data source name.
     *
     * @param primaryDataSourceName to be changed primary data source name
     */
    public void changePrimaryDataSourceName(final String primaryDataSourceName) {
        this.primaryDataSourceName = primaryDataSourceName;
    }
    
    /**
     *  Get data source.
     *
     * @param dataSourceMap data source map
     * @return data source
     */
    public Map<String, DataSource> getDataSourceGroup(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (dataSourceNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Get data source mapper.
     *
     * @return data source mapper
     */
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(1, 1);
        Collection<String> actualDataSourceNames = new LinkedList<>(dataSourceNames);
        result.put(groupName, actualDataSourceNames);
        return result;
    }
}
