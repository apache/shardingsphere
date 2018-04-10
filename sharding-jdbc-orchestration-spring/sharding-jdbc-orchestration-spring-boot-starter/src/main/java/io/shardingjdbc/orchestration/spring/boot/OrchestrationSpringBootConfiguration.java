/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.spring.boot;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;

import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.util.DataSourceUtil;
import io.shardingjdbc.orchestration.api.OrchestrationMasterSlaveDataSourceFactory;
import io.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationProperties;
import io.shardingjdbc.orchestration.spring.boot.orchestration.SpringBootOrchestrationConfigurationProperties;
import io.shardingjdbc.orchestration.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import io.shardingjdbc.orchestration.spring.boot.util.PropertyUtil;

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
        String type = orchestrationProperties.getType();
        Preconditions.checkState(null != type, "Missing the type of datasource configuration in orchestration configuration");
        return OrchestrationConfiguration.SHARDING.equals(type)
                ? OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap,
                shardingProperties.getShardingRuleConfiguration(), shardingProperties.getConfigMap(), shardingProperties.getProps(), orchestrationProperties.getOrchestrationConfiguration())
                : OrchestrationMasterSlaveDataSourceFactory.createDataSource(dataSourceMap,
                masterSlaveProperties.getMasterSlaveRuleConfiguration(), masterSlaveProperties.getConfigMap(), orchestrationProperties.getOrchestrationConfiguration());
    }
    
    @Override
    public void setEnvironment(final Environment environment) {
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
                throw new ShardingJdbcException("Can't find datasource type!", ex);
            }
        }
    }
}
