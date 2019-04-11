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

package org.apache.shardingsphere.shardingjdbc.spring.boot;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.util.InlineExpressionParser;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.spring.boot.common.SpringBootPropertiesConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.encrypt.SpringBootEncryptRuleConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.util.DataSourceUtil;
import org.apache.shardingsphere.shardingjdbc.spring.boot.util.PropertyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring boot sharding and master-slave configuration.
 *
 * @author caohao
 * @author panjuan
 */
@Configuration
@EnableConfigurationProperties({
        SpringBootShardingRuleConfigurationProperties.class, 
        SpringBootMasterSlaveRuleConfigurationProperties.class, SpringBootEncryptRuleConfigurationProperties.class, SpringBootPropertiesConfigurationProperties.class})
@ConditionalOnProperty(prefix = "spring.shardingsphere", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SpringBootConfiguration implements EnvironmentAware {
    
    private final SpringBootShardingRuleConfigurationProperties shardingProperties;
    
    private final SpringBootMasterSlaveRuleConfigurationProperties masterSlaveProperties;
    
    private final SpringBootEncryptRuleConfigurationProperties encryptProperties;
    
    private final SpringBootPropertiesConfigurationProperties propMapProperties;
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    private final ShardingRuleConfigurationYamlSwapper shardingSwapper = new ShardingRuleConfigurationYamlSwapper();
    
    private final MasterSlaveRuleConfigurationYamlSwapper masterSlaveSwapper = new MasterSlaveRuleConfigurationYamlSwapper();
    
    private final EncryptRuleConfigurationYamlSwapper encryptSwapper = new EncryptRuleConfigurationYamlSwapper();
    
    /**
     * Get data source bean.
     *
     * @return data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    public DataSource dataSource() throws SQLException {
        if (null != masterSlaveProperties.getMasterDataSourceName()) {
            return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveSwapper.swap(masterSlaveProperties), propMapProperties.getProps());
        }
        if (!encryptProperties.getEncryptors().isEmpty()) {
            return EncryptDataSourceFactory.createDataSource(dataSourceMap.values().iterator().next(), encryptSwapper.swap(encryptProperties));
        }
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingSwapper.swap(shardingProperties), propMapProperties.getProps());
    }
    
    @Override
    public final void setEnvironment(final Environment environment) {
        String prefix = "spring.shardingsphere.datasource.";
        for (String each : getDataSourceNames(environment, prefix)) {
            try {
                dataSourceMap.put(each, getDataSource(environment, prefix, each));
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingException("Can't find datasource type!", ex);
            }
        }
    }
    
    private List<String> getDataSourceNames(final Environment environment, final String prefix) {
        StandardEnvironment standardEnv = (StandardEnvironment) environment;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        return null == standardEnv.getProperty(prefix + "name") 
                ? new InlineExpressionParser(standardEnv.getProperty(prefix + "names")).splitAndEvaluate() : Collections.singletonList(standardEnv.getProperty(prefix + "name"));
    }
    
    @SuppressWarnings("unchecked")
    private DataSource getDataSource(final Environment environment, final String prefix, final String dataSourceName) throws ReflectiveOperationException {
        Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dataSourceName.trim(), Map.class);
        Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
        return DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
    }
}
