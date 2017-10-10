/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Master-slave rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public class MasterSlaveRuleConfiguration {
    
    private String name;
    
    private String masterDataSourceName;
    
    private Collection<String> slaveDataSourceNames = new LinkedList<>();
    
    private MasterSlaveLoadBalanceAlgorithmType loadBalanceAlgorithmType;
    
    private String loadBalanceAlgorithmClassName;
    
    /**
     * Build master-slave rule.
     *
     * @param dataSourceMap data source map
     * @return sharding rule
     */
    public MasterSlaveRule build(final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkNotNull(name, "name cannot be null.");
        Preconditions.checkNotNull(masterDataSourceName, "masterDataSourceName cannot be null.");
        Preconditions.checkNotNull(slaveDataSourceNames, "slaveDataSourceNames cannot be null.");
        Preconditions.checkArgument(!slaveDataSourceNames.isEmpty(), "slaveDataSourceNames cannot be empty.");
        Map<String, DataSource> slaveDataSources = new HashMap<>(slaveDataSourceNames.size(), 1);
        for (String each : slaveDataSourceNames) {
            slaveDataSources.put(each, dataSourceMap.get(each));
        }
        return new MasterSlaveRule(name, masterDataSourceName, dataSourceMap.get(masterDataSourceName), slaveDataSources, getLoadBalanceAlgorithm());
    }
    
    private MasterSlaveLoadBalanceAlgorithm getLoadBalanceAlgorithm() {
        MasterSlaveLoadBalanceAlgorithm result;
        if (null != loadBalanceAlgorithmType) {
            result = loadBalanceAlgorithmType.getAlgorithm();
        } else {
            result = Strings.isNullOrEmpty(loadBalanceAlgorithmClassName) ? null : newInstance(loadBalanceAlgorithmClassName);
        }
        return result;
    }
    
    public MasterSlaveLoadBalanceAlgorithm newInstance(final String masterSlaveLoadBalanceAlgorithmClassName) {
        try {
            Class<?> result = Class.forName(masterSlaveLoadBalanceAlgorithmClassName);
            if (!MasterSlaveLoadBalanceAlgorithm.class.isAssignableFrom(result)) {
                throw new ShardingJdbcException("Class %s should be implement %s", masterSlaveLoadBalanceAlgorithmClassName, MasterSlaveLoadBalanceAlgorithm.class.getName());
            }
            return (MasterSlaveLoadBalanceAlgorithm) result.newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingJdbcException("Class %s should have public privilege and no argument constructor", masterSlaveLoadBalanceAlgorithmClassName);
        }
    }
}
