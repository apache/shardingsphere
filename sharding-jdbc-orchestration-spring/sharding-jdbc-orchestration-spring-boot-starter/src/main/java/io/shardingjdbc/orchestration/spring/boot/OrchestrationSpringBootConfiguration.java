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

package io.shardingjdbc.orchestration.spring.boot;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingjdbc.core.constant.ShardingPropertiesConstant;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.util.DataSourceUtil;
import io.shardingjdbc.orchestration.api.OrchestrationMasterSlaveDataSourceFactory;
import io.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingjdbc.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;
import io.shardingjdbc.orchestration.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationProperties;
import io.shardingjdbc.orchestration.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration spring boot sharding and master-slave configuration.
 *
 * @author caohao
 */
@Configuration
@EnableConfigurationProperties({SpringBootShardingRuleConfigurationProperties.class, SpringBootMasterSlaveRuleConfigurationProperties.class})
public class OrchestrationSpringBootConfiguration implements EnvironmentAware {
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    private final Properties props = new Properties();
    
    @Autowired
    private SpringBootShardingRuleConfigurationProperties shardingProperties;
    
    @Autowired
    private SpringBootMasterSlaveRuleConfigurationProperties masterSlaveProperties;
    
    private OrchestrationConfiguration orchestrationConfig;
    
    @Bean
    public DataSource dataSource() throws SQLException {
        return null == masterSlaveProperties.getMasterDataSourceName() 
                ? OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, 
                        shardingProperties.getShardingRuleConfiguration(), shardingProperties.getConfigMap(), shardingProperties.getProps(), orchestrationConfig)
                : OrchestrationMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, 
                        masterSlaveProperties.getMasterSlaveRuleConfiguration(), masterSlaveProperties.getConfigMap(), orchestrationConfig);
    }
    
    @Override
    public void setEnvironment(final Environment environment) {
        setDataSourceMap(environment);
        setShardingProperties(environment);
        setOrchestrationConfiguration(environment);
    }
    
    private void setDataSourceMap(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.datasource.");
        String dataSources = propertyResolver.getProperty("names");
        for (String each : dataSources.split(",")) {
            try {
                Map<String, Object> dataSourceProps = propertyResolver.getSubProperties(each + ".");
                Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
                DataSource dataSource = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
                dataSourceMap.put(each, dataSource);
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingJdbcException("Can't find datasource type!", ex);
            }
        }
    }
    
    private void setShardingProperties(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.config.sharding.props.");
        String showSQL = propertyResolver.getProperty(ShardingPropertiesConstant.SQL_SHOW.getKey());
        if (!Strings.isNullOrEmpty(showSQL)) {
            props.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), showSQL);
        }
        String executorSize = propertyResolver.getProperty(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey());
        if (!Strings.isNullOrEmpty(executorSize)) {
            props.setProperty(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey(), executorSize);
        }
    }
    
    private void setOrchestrationConfiguration(final Environment environment) {
        RelaxedPropertyResolver regCenterPropertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.config.orchestration.regcenter.");
        String type = regCenterPropertyResolver.getProperty("type");
        RegistryCenterConfiguration regCenterConfig;
        if ("zookeeper".equalsIgnoreCase(type)) {
            regCenterConfig = getZookeeperConfiguration(regCenterPropertyResolver);
        } else if ("etcd".equalsIgnoreCase(type)) {
            regCenterConfig = getEtcdConfiguration(regCenterPropertyResolver);
        } else {
            throw new ShardingJdbcException("Can't find registry center type: %s!", type);
        }
        RelaxedPropertyResolver orchestrationPropertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.config.orchestration.");
        String name = orchestrationPropertyResolver.containsProperty("name") ? orchestrationPropertyResolver.getProperty("name") : null;
        boolean overwrite = orchestrationPropertyResolver.containsProperty("overwrite") ? Boolean.valueOf(orchestrationPropertyResolver.getProperty("overwrite")) : false;
        orchestrationConfig = new OrchestrationConfiguration(name, regCenterConfig, overwrite);
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final RelaxedPropertyResolver propertyResolver) {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        if (propertyResolver.containsProperty("serverLists")) {
            result.setServerLists(propertyResolver.getProperty("serverLists"));
        }
        if (propertyResolver.containsProperty("namespace")) {
            result.setNamespace(propertyResolver.getProperty("namespace"));
        }
        if (propertyResolver.containsProperty("baseSleepTimeMilliseconds")) {
            result.setBaseSleepTimeMilliseconds(Integer.parseInt(propertyResolver.getProperty("baseSleepTimeMilliseconds")));
        }
        if (propertyResolver.containsProperty("maxSleepTimeMilliseconds")) {
            result.setMaxSleepTimeMilliseconds(Integer.parseInt(propertyResolver.getProperty("maxSleepTimeMilliseconds")));
        }
        if (propertyResolver.containsProperty("maxRetries")) {
            result.setMaxRetries(Integer.parseInt(propertyResolver.getProperty("maxRetries")));
        }
        if (propertyResolver.containsProperty("sessionTimeoutMilliseconds")) {
            result.setSessionTimeoutMilliseconds(Integer.parseInt(propertyResolver.getProperty("sessionTimeoutMilliseconds")));
        }
        if (propertyResolver.containsProperty("connectionTimeoutMilliseconds")) {
            result.setConnectionTimeoutMilliseconds(Integer.parseInt(propertyResolver.getProperty("connectionTimeoutMilliseconds")));
        }
        if (propertyResolver.containsProperty("digest")) {
            result.setDigest(propertyResolver.getProperty("digest"));
        }
        return result;
    }
    
    private EtcdConfiguration getEtcdConfiguration(final RelaxedPropertyResolver propertyResolver) {
        EtcdConfiguration result = new EtcdConfiguration();
        if (propertyResolver.containsProperty("serverLists")) {
            result.setServerLists(propertyResolver.getProperty("serverLists"));
        }
        if (propertyResolver.containsProperty("namespace")) {
            result.setNamespace(propertyResolver.getProperty("namespace"));
        }
        if (propertyResolver.containsProperty("timeToLiveMilliseconds")) {
            result.setTimeToLiveMilliseconds(Integer.parseInt(propertyResolver.getProperty("timeToLiveMilliseconds")));
        }
        if (propertyResolver.containsProperty("timeoutMilliseconds")) {
            result.setTimeoutMilliseconds(Integer.parseInt(propertyResolver.getProperty("timeoutMilliseconds")));
        }
        if (propertyResolver.containsProperty("retryIntervalMilliseconds")) {
            result.setRetryIntervalMilliseconds(Integer.parseInt(propertyResolver.getProperty("retryIntervalMilliseconds")));
        }
        if (propertyResolver.containsProperty("maxRetries")) {
            result.setMaxRetries(Integer.parseInt(propertyResolver.getProperty("maxRetries")));
        }
        return result;
    }
}
