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

package org.apache.shardingsphere.spring.boot;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.spring.boot.condition.ShardingSphereSpringBootCondition;
import org.apache.shardingsphere.spring.boot.datasource.DataSourceMapSetter;
import org.apache.shardingsphere.spring.boot.prop.SpringBootPropertiesConfiguration;
import org.apache.shardingsphere.spring.boot.schema.DatabaseNameSetter;
import org.apache.shardingsphere.spring.transaction.TransactionTypeScanner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring boot starter configuration.
 */
@Configuration
@ComponentScan("org.apache.shardingsphere.spring.boot.converter")
@EnableConfigurationProperties(SpringBootPropertiesConfiguration.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@RequiredArgsConstructor
public class ShardingSphereAutoConfiguration implements EnvironmentAware {
    
    private String databaseName;
    
    private final SpringBootPropertiesConfiguration props;
    
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    
    /**
     * Get mode configuration.
     *
     * @return mode configuration
     */
    @Bean
    public ModeConfiguration modeConfiguration() {
        return null == props.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(props.getMode());
    }
    
    /**
     * Get ShardingSphere data source bean.
     *
     * @param rules rules configuration
     * @param modeConfig mode configuration
     * @return data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    @Conditional(ShardingSphereSpringBootCondition.class)
    @Autowired(required = false)
    public DataSource shardingSphereDataSource(final ObjectProvider<List<RuleConfiguration>> rules, final ObjectProvider<ModeConfiguration> modeConfig) throws SQLException {
        Collection<RuleConfiguration> ruleConfigs = Optional.ofNullable(rules.getIfAvailable()).orElseGet(Collections::emptyList);
        return ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig.getIfAvailable(), dataSourceMap, ruleConfigs, props.getProps());
    }
    
    /**
     * Get data source bean from registry center.
     *
     * @param modeConfig mode configuration
     * @return data source bean
     * @throws SQLException SQL exception
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(final ModeConfiguration modeConfig) throws SQLException {
        return !dataSourceMap.isEmpty() ? ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, Collections.emptyList(), props.getProps())
                : ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig);
    }
    
    /**
     * Create transaction type scanner.
     *
     * @return transaction type scanner
     */
    @Bean
    public TransactionTypeScanner transactionTypeScanner() {
        return new TransactionTypeScanner();
    }
    
    @Override
    public final void setEnvironment(final Environment environment) {
        dataSourceMap.putAll(DataSourceMapSetter.getDataSourceMap(environment));
        databaseName = DatabaseNameSetter.getDatabaseName(environment);
    }
}
