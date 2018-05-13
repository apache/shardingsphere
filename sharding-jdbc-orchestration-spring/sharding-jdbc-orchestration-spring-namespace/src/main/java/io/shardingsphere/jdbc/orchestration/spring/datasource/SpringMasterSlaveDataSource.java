/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.spring.datasource;

import io.shardingsphere.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Master-slave datasource for spring namespace.
 *
 * @author caohao
 */
public class SpringMasterSlaveDataSource extends MasterSlaveDataSource {
    
    public SpringMasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final String name, 
                                       final String masterDataSourceName, final Collection<String> slaveDataSourceNames, final MasterSlaveLoadBalanceAlgorithm strategy) throws SQLException {
        super(dataSourceMap, getMasterSlaveRuleConfiguration(name, masterDataSourceName, slaveDataSourceNames, strategy), Collections.<String, Object>emptyMap());
    }
    
    public SpringMasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final String name, 
                                       final String masterDataSourceName, final Collection<String> slaveDataSourceNames, final MasterSlaveLoadBalanceAlgorithmType strategyType) throws SQLException {
        super(dataSourceMap, 
                getMasterSlaveRuleConfiguration(name, masterDataSourceName, slaveDataSourceNames, null == strategyType ? null : strategyType.getAlgorithm()), Collections.<String, Object>emptyMap());
    }
    
    private static MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration(
            final String name, final String masterDataSourceName, final Collection<String> slaveDataSourceNames, final MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm) {
        return new MasterSlaveRuleConfiguration(name, masterDataSourceName, slaveDataSourceNames, loadBalanceAlgorithm);
    }
}
