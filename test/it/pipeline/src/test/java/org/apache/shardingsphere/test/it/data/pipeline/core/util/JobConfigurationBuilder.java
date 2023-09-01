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

package org.apache.shardingsphere.test.it.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.ConfigurationFileUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Job configuration builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationBuilder {
    
    /**
     * Create migration job configuration.
     *
     * @return created job configuration
     */
    // TODO Rename createJobConfiguration
    public static MigrationJobConfiguration createJobConfiguration() {
        return new YamlMigrationJobConfigurationSwapper().swapToObject(createYamlMigrationJobConfiguration());
    }
    
    /**
     * Create YAML migration job configuration.
     *
     * @return created job configuration
     * @throws SQLWrapperException if there's SQLException when creating table
     */
    public static YamlMigrationJobConfiguration createYamlMigrationJobConfiguration() {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setTargetDatabaseName("logic_db");
        result.setSourceDatabaseType("H2");
        result.setTargetDatabaseType("H2");
        result.setTargetTableNames(Collections.singletonList("t_order"));
        Map<String, String> targetTableSchemaMap = new LinkedHashMap<>();
        targetTableSchemaMap.put("t_order", null);
        result.setTargetTableSchemaMap(targetTableSchemaMap);
        result.setTablesFirstDataNodes("t_order:ds_0.t_order");
        result.setJobShardingDataNodes(Collections.singletonList("t_order:ds_0.t_order"));
        PipelineContextKey contextKey = PipelineContextKey.build(RandomStringUtils.randomAlphabetic(32), InstanceType.PROXY);
        result.setJobId(generateMigrationJobId(contextKey, result));
        Map<String, YamlPipelineDataSourceConfiguration> sources = new LinkedHashMap<>();
        String databaseNameSuffix = RandomStringUtils.randomAlphabetic(9);
        PipelineDataSourceConfiguration sourceDataSourceConfig = new StandardPipelineDataSourceConfiguration(
                ConfigurationFileUtils.readFile("migration_standard_jdbc_source.yaml").replace("${databaseNameSuffix}", databaseNameSuffix));
        try (
                PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(sourceDataSourceConfig);
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(PipelineContextUtils.getCreateOrderTableSchema());
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
        sources.put("ds_0", createYamlPipelineDataSourceConfiguration(sourceDataSourceConfig));
        result.setSources(sources);
        result.setTarget(createYamlPipelineDataSourceConfiguration(new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtils.readFile("migration_sharding_sphere_jdbc_target.yaml").replace("${databaseNameSuffix}", databaseNameSuffix))));
        TypedSPILoader.getService(PipelineJobAPI.class, "MIGRATION").extendYamlJobConfiguration(contextKey, result);
        return result;
    }
    
    private static String generateMigrationJobId(final PipelineContextKey contextKey, final YamlMigrationJobConfiguration yamlJobConfig) {
        MigrationJobId migrationJobId = new MigrationJobId(contextKey, yamlJobConfig.getJobShardingDataNodes());
        return new MigrationJobAPI().marshalJobId(migrationJobId);
    }
    
    private static YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration(final PipelineDataSourceConfiguration config) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(config.getType());
        result.setParameter(config.getParameter());
        return result;
    }
}
