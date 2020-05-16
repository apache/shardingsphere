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

package org.apache.shardingsphere.masterslave.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

import java.util.List;

/**
 * Master-slave data source configuration.
 */
@Getter
public final class MasterSlaveDataSourceConfiguration implements RuleConfiguration {
    
    private final String name;
    
    private final String masterDataSourceName;
    
    private final List<String> slaveDataSourceNames;
    
    private final LoadBalanceStrategyConfiguration loadBalanceStrategyConfiguration;
    
    public MasterSlaveDataSourceConfiguration(final String name, final String masterDataSourceName, final List<String> slaveDataSourceNames) {
        this(name, masterDataSourceName, slaveDataSourceNames, null);
    }
    
    public MasterSlaveDataSourceConfiguration(final String name,
                                              final String masterDataSourceName, final List<String> slaveDataSourceNames, final LoadBalanceStrategyConfiguration loadBalanceStrategyConfiguration) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Name is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(masterDataSourceName), "MasterDataSourceName is required.");
        Preconditions.checkArgument(null != slaveDataSourceNames && !slaveDataSourceNames.isEmpty(), "SlaveDataSourceNames is required.");
        this.name = name;
        this.masterDataSourceName = masterDataSourceName;
        this.slaveDataSourceNames = slaveDataSourceNames;
        this.loadBalanceStrategyConfiguration = loadBalanceStrategyConfiguration;
    }
}
