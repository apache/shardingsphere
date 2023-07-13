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

package org.apache.shardingsphere.driver.data.pipeline.datasource.creator;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.datasource.creator.PipelineDataSourceCreator;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.props.JDBCRepositoryPropertyKey;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;
import org.apache.shardingsphere.single.api.constant.SingleTableConstants;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ShardingSphere pipeline data source creator.
 */
public final class ShardingSpherePipelineDataSourceCreator implements PipelineDataSourceCreator {
    
    private static final AtomicInteger STANDALONE_DATABASE_ID = new AtomicInteger(1);
    
    @Override
    public DataSource createPipelineDataSource(final Object dataSourceConfig) throws SQLException {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(YamlEngine.marshal(dataSourceConfig), YamlRootConfiguration.class);
        disableSystemSchemaMetadata(rootConfig);
        enableStreamingQuery(rootConfig);
        updateSingleRuleConfiguration(rootConfig);
        Optional<YamlShardingRuleConfiguration> yamlShardingRuleConfig = ShardingRuleConfigurationConverter.findYamlShardingRuleConfiguration(rootConfig.getRules());
        if (yamlShardingRuleConfig.isPresent()) {
            enableRangeQueryForInline(yamlShardingRuleConfig.get());
            removeAuditStrategy(yamlShardingRuleConfig.get());
        }
        rootConfig.setDatabaseName(rootConfig.getDatabaseName());
        rootConfig.setSchemaName(rootConfig.getSchemaName());
        rootConfig.setMode(createStandaloneModeConfiguration());
        return YamlShardingSphereDataSourceFactory.createDataSourceWithoutCache(rootConfig);
    }
    
    private void updateSingleRuleConfiguration(final YamlRootConfiguration rootConfig) {
        rootConfig.getRules().removeIf(YamlSingleRuleConfiguration.class::isInstance);
        YamlSingleRuleConfiguration singleRuleConfig = new YamlSingleRuleConfiguration();
        singleRuleConfig.setTables(Collections.singletonList(SingleTableConstants.ALL_TABLES));
        rootConfig.getRules().add(singleRuleConfig);
    }
    
    private void disableSystemSchemaMetadata(final YamlRootConfiguration rootConfig) {
        rootConfig.getProps().put(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ENABLED.getKey(), String.valueOf(Boolean.FALSE));
    }
    
    // TODO Another way is improving ExecuteQueryCallback.executeSQL to enable streaming query, then remove it
    private void enableStreamingQuery(final YamlRootConfiguration rootConfig) {
        // Set a large enough value to enable ConnectionMode.MEMORY_STRICTLY, make sure streaming query work.
        rootConfig.getProps().put(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), 100000);
    }
    
    private void enableRangeQueryForInline(final YamlShardingRuleConfiguration yamlShardingRuleConfig) {
        for (YamlAlgorithmConfiguration each : yamlShardingRuleConfig.getShardingAlgorithms().values()) {
            if ("INLINE".equalsIgnoreCase(each.getType())) {
                each.getProps().put("allow-range-query-with-inline-sharding", Boolean.TRUE.toString());
            }
        }
    }
    
    private void removeAuditStrategy(final YamlShardingRuleConfiguration yamlShardingRuleConfig) {
        yamlShardingRuleConfig.setDefaultAuditStrategy(null);
        yamlShardingRuleConfig.setAuditors(null);
        if (null != yamlShardingRuleConfig.getTables()) {
            yamlShardingRuleConfig.getTables().forEach((key, value) -> value.setAuditStrategy(null));
        }
        if (null != yamlShardingRuleConfig.getAutoTables()) {
            yamlShardingRuleConfig.getAutoTables().forEach((key, value) -> value.setAuditStrategy(null));
        }
    }
    
    private YamlModeConfiguration createStandaloneModeConfiguration() {
        YamlModeConfiguration result = new YamlModeConfiguration();
        result.setType("Standalone");
        YamlPersistRepositoryConfiguration repository = new YamlPersistRepositoryConfiguration();
        result.setRepository(repository);
        repository.setType("JDBC");
        Properties props = new Properties();
        repository.setProps(props);
        props.setProperty(JDBCRepositoryPropertyKey.JDBC_URL.getKey(),
                String.format("jdbc:h2:mem:pipeline_db_%d;DB_CLOSE_DELAY=0;DATABASE_TO_UPPER=false;MODE=MYSQL", STANDALONE_DATABASE_ID.getAndIncrement()));
        return result;
    }
    
    @Override
    public String getType() {
        return ShardingSpherePipelineDataSourceConfiguration.TYPE;
    }
}
