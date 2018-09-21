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

package io.shardingsphere.jdbc.orchestration.spring.boot;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.DataSourceUtil;
import io.shardingsphere.jdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import io.shardingsphere.jdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import io.shardingsphere.jdbc.orchestration.config.OrchestrationType;
import io.shardingsphere.jdbc.orchestration.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationProperties;
import io.shardingsphere.jdbc.orchestration.spring.boot.orchestration.SpringBootOrchestrationConfigurationProperties;
import io.shardingsphere.jdbc.orchestration.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import io.shardingsphere.jdbc.orchestration.spring.boot.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Orchestration spring boot sharding and master-slave configuration.
 *
 * @author caohao
 */
@Configuration
@EnableConfigurationProperties({SpringBootShardingRuleConfigurationProperties.class, SpringBootMasterSlaveRuleConfigurationProperties.class, SpringBootOrchestrationConfigurationProperties.class})
public class OrchestrationSpringBootConfiguration implements EnvironmentAware {
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    @Autowired
    private SpringBootShardingRuleConfigurationProperties shardingProperties;
    
    @Autowired
    private SpringBootMasterSlaveRuleConfigurationProperties masterSlaveProperties;
    
    @Autowired
    private SpringBootOrchestrationConfigurationProperties orchestrationProperties;
    
    /**
     * Get data source bean.
     * 
     * @return data source bean
     * @throws SQLException SQL Exception
     */
    @Bean
    public DataSource dataSource() throws SQLException {
        OrchestrationType type = orchestrationProperties.getType();
        Preconditions.checkState(null != type, "Missing the type of datasource configuration in orchestration configuration");
        if (OrchestrationType.SHARDING == type) {
            ShardingDataSource shardingDataSource = new ShardingDataSource(
                    dataSourceMap, new ShardingRule(shardingProperties.getShardingRuleConfiguration(), dataSourceMap.keySet()), shardingProperties.getConfigMap(), shardingProperties.getProps());
            return new OrchestrationShardingDataSource(shardingDataSource, orchestrationProperties.getOrchestrationConfiguration());
        }
        MasterSlaveDataSource masterSlaveDataSource = new MasterSlaveDataSource(
                dataSourceMap, masterSlaveProperties.getMasterSlaveRuleConfiguration(), masterSlaveProperties.getConfigMap(), masterSlaveProperties.getProps());
        return new OrchestrationMasterSlaveDataSource(masterSlaveDataSource, orchestrationProperties.getOrchestrationConfiguration());
    }
    
    @Override
    public final void setEnvironment(final Environment environment) {
        setDataSourceMap(environment);
    }
    
    @SuppressWarnings("unchecked")
    private void setDataSourceMap(final Environment environment) {
        String prefix = "sharding.jdbc.datasource.";
        String dataSources = environment.getProperty(prefix + "names");
        if (StringUtils.isEmpty(dataSources)) {
            return;
        }
        dataSources = dataSources.trim();
        for (String each : dataSources.split(",")) {
            try {
                Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + each, Map.class);
                Preconditions.checkState(!dataSourceProps.isEmpty(), String.format("Wrong datasource [%s] properties!", each));
                DataSource dataSource = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
                dataSourceMap.put(each, dataSource);
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingException("Can't find datasource type!", ex);
            }
        }
    }
}
