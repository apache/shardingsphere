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

package org.apache.shardingsphere.core.rule;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * Sharding data source names.
 * 
 * <p>Will convert actual data source names to master-slave data source name.</p>
 * 
 * @author zhangliang
 */
public final class ShardingDataSourceNames {
    
    private final ShardingRuleConfiguration shardingRuleConfig;
    
    @Getter
    private final Collection<String> dataSourceNames;
    
    public ShardingDataSourceNames(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> rawDataSourceNames) {
        Preconditions.checkArgument(null != shardingRuleConfig, "can not construct ShardingDataSourceNames with null ShardingRuleConfig");
        this.shardingRuleConfig = shardingRuleConfig;
        dataSourceNames = getAllDataSourceNames(rawDataSourceNames);
    }
    
    private Collection<String> getAllDataSourceNames(final Collection<String> dataSourceNames) {
        Collection<String> result = new LinkedHashSet<>(dataSourceNames);
        for (MasterSlaveRuleConfiguration each : shardingRuleConfig.getMasterSlaveRuleConfigs()) {
            result.remove(each.getMasterDataSourceName());
            result.removeAll(each.getSlaveDataSourceNames());
            result.add(each.getName());
        }
        return result;
    }
    
    /**
     * Get default data source name.
     *
     * @return default data source name
     */
    public String getDefaultDataSourceName() {
        return 1 == dataSourceNames.size() ? dataSourceNames.iterator().next() : shardingRuleConfig.getDefaultDataSourceName();
    }
    
    /**
     * Get raw master data source name.
     *
     * @param dataSourceName data source name
     * @return raw master data source name
     */
    public String getRawMasterDataSourceName(final String dataSourceName) {
        for (MasterSlaveRuleConfiguration each : shardingRuleConfig.getMasterSlaveRuleConfigs()) {
            if (each.getName().equals(dataSourceName)) {
                return each.getMasterDataSourceName();
            }
        }
        return dataSourceName;
    }
    
    /**
     * Get random data source name.
     *
     * @return random data source name
     */
    public String getRandomDataSourceName() {
        return getRandomDataSourceName(dataSourceNames);
    }
    
    /**
     * Get random data source name.
     *
     * @param dataSourceNames available data source names
     * @return random data source name
     */
    public String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        return Lists.newArrayList(dataSourceNames).get(new Random().nextInt(dataSourceNames.size()));
    }
}
