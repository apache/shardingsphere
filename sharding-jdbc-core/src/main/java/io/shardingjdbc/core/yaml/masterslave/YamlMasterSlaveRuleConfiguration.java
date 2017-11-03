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

package io.shardingjdbc.core.yaml.masterslave;

import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Master-slave rule configuration for yaml.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlMasterSlaveRuleConfiguration {
    
    private String name;
    
    private String masterDataSourceName;
    
    private Collection<String> slaveDataSourceNames = new ArrayList<>();
    
    private MasterSlaveLoadBalanceAlgorithmType loadBalanceAlgorithmType;
    
    private String loadBalanceAlgorithmClassName;
    
    /**
     * Get master-slave rule configuration from yaml.
     *
     * @return master-slave rule configuration from yaml
     */
    public MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() throws SQLException {
        MasterSlaveRuleConfiguration result = new MasterSlaveRuleConfiguration();
        result.setName(name);
        result.setMasterDataSourceName(masterDataSourceName);
        result.setSlaveDataSourceNames(slaveDataSourceNames);
        result.setLoadBalanceAlgorithmType(loadBalanceAlgorithmType);
        result.setLoadBalanceAlgorithmClassName(loadBalanceAlgorithmClassName);
        return result;
    }
}
