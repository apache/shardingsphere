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

package org.apache.shardingsphere.replica.rule;

import org.apache.shardingsphere.infra.rule.DataSourceRoutedRule;
import org.apache.shardingsphere.replica.api.config.ReplicaDataSourceConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Replica rule.
 */
public final class ReplicaRule implements DataSourceRoutedRule {
    
    private final Map<String, Collection<String>> dataSourceRules;
    
    public ReplicaRule(final ReplicaRuleConfiguration configuration) {
        dataSourceRules = new HashMap<>(configuration.getDataSources().size(), 1);
        for (ReplicaDataSourceConfiguration each : configuration.getDataSources()) {
            dataSourceRules.put(each.getName(), each.getReplicaSourceNames());
        }
    }
    
    /**
     * Get single replica data sources.
     *
     * @return replica data source rule
     */
    public Collection<String> getSingleReplicaDataSources() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find replica data sources.
     * 
     * @param dataSourceName data source name
     * @return replica data source names
     */
    public Optional<Collection<String>> findReplicaDataSources(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    /**
     * Find logic data source name.
     * 
     * @param replicaDataSourceName replica data source name
     * @return logic data source name
     */
    public Optional<String> findLogicDataSource(final String replicaDataSourceName) {
        for (Entry<String, Collection<String>> entry : dataSourceRules.entrySet()) {
            if (entry.getValue().contains(replicaDataSourceName)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        return dataSourceRules;
    }
}
