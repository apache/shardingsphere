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
        setRegistryCenterConfiguration(environment);
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
    
    private void setRegistryCenterConfiguration(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.config.orchestration.");
        String type = propertyResolver.getProperty("regcenter.type");
        RegistryCenterConfiguration regCenterConfig;
        if ("zookeeper".equalsIgnoreCase(type)) {
            regCenterConfig = getZookeeperConfiguration(propertyResolver);
        } else if ("etcd".equalsIgnoreCase(type)) {
            regCenterConfig = getEtcdConfiguration(propertyResolver);
        } else {
            throw new ShardingJdbcException("Can't find registry center type: %s!", type);
        }
        String name = propertyResolver.containsProperty("name") ? propertyResolver.getProperty("name") : null;
        boolean overwrite = propertyResolver.containsProperty("overwrite") ? Boolean.valueOf(propertyResolver.getProperty("overwrite")) : false;
        orchestrationConfig = new OrchestrationConfiguration(name, regCenterConfig, overwrite);
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final RelaxedPropertyResolver propertyResolver) {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        if (propertyResolver.containsProperty("regcenter.serverLists")) {
            result.setServerLists(propertyResolver.getProperty("regcenter.serverLists"));
        }
        if (propertyResolver.containsProperty("regcenter.namespace")) {
            result.setNamespace(propertyResolver.getProperty("regcenter.namespace"));
        }
        if (propertyResolver.containsProperty("regcenter.baseSleepTimeMilliseconds")) {
            result.setBaseSleepTimeMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.baseSleepTimeMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.maxSleepTimeMilliseconds")) {
            result.setMaxSleepTimeMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.maxSleepTimeMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.maxRetries")) {
            result.setMaxRetries(Integer.parseInt(propertyResolver.getProperty("regcenter.maxRetries")));
        }
        if (propertyResolver.containsProperty("regcenter.sessionTimeoutMilliseconds")) {
            result.setSessionTimeoutMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.sessionTimeoutMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.connectionTimeoutMilliseconds")) {
            result.setConnectionTimeoutMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.connectionTimeoutMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.digest")) {
            result.setDigest(propertyResolver.getProperty("regcenter.digest"));
        }
        return result;
    }
    
    private EtcdConfiguration getEtcdConfiguration(final RelaxedPropertyResolver propertyResolver) {
        EtcdConfiguration result = new EtcdConfiguration();
        if (propertyResolver.containsProperty("regcenter.serverLists")) {
            result.setServerLists(propertyResolver.getProperty("regcenter.serverLists"));
        }
        if (propertyResolver.containsProperty("regcenter.namespace")) {
            result.setNamespace(propertyResolver.getProperty("regcenter.namespace"));
        }
        if (propertyResolver.containsProperty("regcenter.timeToLiveMilliseconds")) {
            result.setTimeToLiveMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.timeToLiveMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.timeoutMilliseconds")) {
            result.setTimeoutMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.timeoutMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.retryIntervalMilliseconds")) {
            result.setRetryIntervalMilliseconds(Integer.parseInt(propertyResolver.getProperty("regcenter.retryIntervalMilliseconds")));
        }
        if (propertyResolver.containsProperty("regcenter.maxRetries")) {
            result.setMaxRetries(Integer.parseInt(propertyResolver.getProperty("regcenter.maxRetries")));
        }
        return result;
    }
}
