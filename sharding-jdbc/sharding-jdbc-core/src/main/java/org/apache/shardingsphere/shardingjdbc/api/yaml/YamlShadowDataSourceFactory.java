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

package org.apache.shardingsphere.shardingjdbc.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlRootMasterSlaveConfiguration;
import org.apache.shardingsphere.core.yaml.config.shadow.YamlRootShadowConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.ShadowDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Shadow data source factory for YAML.
 *
 * @author xiayan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlShadowDataSourceFactory {

    /**
     * Create shadow data source.
     *
     * @param yamlFile YAML file for encrypt rule configuration with data sources
     * @return shadow data source
     */
    @SneakyThrows
    public static DataSource createDataSource(final File yamlFile) {
        YamlRootShadowConfiguration config = YamlEngine.unmarshal(yamlFile, YamlRootShadowConfiguration.class);
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration(config.getShadowRule().getColumn());
        return ShadowDataSourceFactory.createDataSource(createActualDataSource(config), createShadowDataSource(config), shadowRuleConfiguration, config.getProps());
    }

    /**
     * Create shadow data source.
     *
     * @param yamlBytes YAML bytes for encrypt rule configuration with data sources
     * @return shadow data source
     */
    @SneakyThrows
    public static DataSource createDataSource(final byte[] yamlBytes) {
        YamlRootShadowConfiguration config = YamlEngine.unmarshal(yamlBytes, YamlRootShadowConfiguration.class);
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration(config.getShadowRule().getColumn());
        return ShadowDataSourceFactory.createDataSource(createActualDataSource(config), createShadowDataSource(config), shadowRuleConfiguration, config.getProps());
    }

    /**
     * Create shadow data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile YAML file for shadow rule configuration without data sources
     * @return shadow data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlRootShadowConfiguration config = YamlEngine.unmarshal(yamlFile, YamlRootShadowConfiguration.class);
        return ShadowDataSourceFactory.createDataSource(createActualDataSource(dataSourceMap, config), createActualDataSource(config.getDataSources(), config),
                new ShadowRuleConfigurationYamlSwapper().swap(config.getShadowRule()), config.getProps());
    }

    @SneakyThrows
    private static DataSource createActualDataSource(final YamlRootShadowConfiguration config) {
        Properties props = config.getProps();
        if (config.isEncrypt()) {
            return EncryptDataSourceFactory.createDataSource(config.getDataSource(), new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), props);
        } else if (config.isSharding()) {
            return ShardingDataSourceFactory.createDataSource(config.getDataSources(), new ShardingRuleConfigurationYamlSwapper().swap(config.getShardingRule()), props);
        } else if (config.isMasterSlave()) {
            return MasterSlaveDataSourceFactory.createDataSource(config.getDataSources(), new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), props);
        } else if (null != config.getDataSource()) {
            return config.getDataSource();
        } else {
            throw new UnsupportedOperationException("unsupported datasource");
        }
    }

    @SneakyThrows
    private static DataSource createActualDataSource(final Map<String, DataSource> dataSourceMap, final YamlRootShadowConfiguration config) {
        Properties props = config.getProps();
        if (config.isEncrypt()) {
            return EncryptDataSourceFactory.createDataSource(dataSourceMap.values().iterator().next(), new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), props);
        } else if (config.isSharding()) {
            return ShardingDataSourceFactory.createDataSource(dataSourceMap, new ShardingRuleConfigurationYamlSwapper().swap(config.getShardingRule()), props);
        } else if (config.isMasterSlave()) {
            return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), props);
        } else if (null != config.getDataSource()) {
            return config.getDataSource();
        } else {
            throw new UnsupportedOperationException("unsupported datasource");
        }
    }

    @SneakyThrows
    private static DataSource createShadowDataSource(final YamlRootShadowConfiguration config) {
        Properties props = config.getShadowRule().getProps();
        if (config.isEncrypt()) {
            return EncryptDataSourceFactory.createDataSource(config.getShadowRule().getDataSource(),
                    new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), props);
        } else if (config.isSharding()) {
            return ShardingDataSourceFactory.createDataSource(config.getShadowRule().getDataSources(),
                    new ShardingRuleConfigurationYamlSwapper().swap(config.getShardingRule()), props);
        } else if (config.isMasterSlave()) {
            return MasterSlaveDataSourceFactory.createDataSource(config.getShadowRule().getDataSources(),
                    new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), props);
        } else if (null != config.getDataSource()) {
            return config.getShadowRule().getDataSource();
        } else {
            throw new UnsupportedOperationException("unsupported datasource");
        }
    }

    @SneakyThrows
    private static DataSource createShadowDataSource(final Map<String, DataSource> dataSourceMap, final YamlRootShadowConfiguration config) {
        Properties props = config.getShadowRule().getProps();
        if (config.isEncrypt()) {
            return EncryptDataSourceFactory.createDataSource(dataSourceMap.values().iterator().next(),
                    new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), props);
        } else if (config.isSharding()) {
            return ShardingDataSourceFactory.createDataSource(dataSourceMap,
                    new ShardingRuleConfigurationYamlSwapper().swap(config.getShardingRule()), props);
        } else if (config.isMasterSlave()) {
            return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap,
                    new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), props);
        } else if (null != config.getDataSource()) {
            return config.getShadowRule().getDataSource();
        } else {
            throw new UnsupportedOperationException("unsupported datasource");
        }
    }
}
