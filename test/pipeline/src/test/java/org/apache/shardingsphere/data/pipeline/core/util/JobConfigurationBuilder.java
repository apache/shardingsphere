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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.yaml.metadata.YamlPipelineColumnMetaData;

/**
 * Job configuration builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationBuilder {
    
    /**
     * Create job configuration.
     *
     * @return created job configuration
     */
    public static MigrationJobConfiguration createJobConfiguration() {
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setTargetDatabaseName("logic_db");
        result.setSourceResourceName("standard_0");
        result.setSourceTableName("t_order");
        result.setTargetTableName("t_order");
        result.setJobId(generateJobId(result));
        result.setSource(createYamlPipelineDataSourceConfiguration(new StandardPipelineDataSourceConfiguration(ConfigurationFileUtil.readFile("migration_standard_jdbc_source.yaml"))));
        result.setTarget(createYamlPipelineDataSourceConfiguration(new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("migration_sharding_sphere_jdbc_target.yaml"))));
        result.setUniqueKeyColumn(new YamlPipelineColumnMetaData(1, "order_id", 4, "", false, true, true));
        PipelineAPIFactory.getPipelineJobAPI(JobType.MIGRATION).extendYamlJobConfiguration(result);
        return new YamlMigrationJobConfigurationSwapper().swapToObject(result);
    }
    
    private static String generateJobId(final YamlMigrationJobConfiguration yamlJobConfig) {
        String sourceTableName = RandomStringUtils.randomAlphabetic(32);
        MigrationJobId migrationJobId = new MigrationJobId(yamlJobConfig.getSourceResourceName(), yamlJobConfig.getSourceSchemaName(), sourceTableName,
                yamlJobConfig.getTargetDatabaseName(), yamlJobConfig.getTargetTableName());
        return MigrationJobAPIFactory.getInstance().marshalJobId(migrationJobId);
    }
    
    private static YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration(final PipelineDataSourceConfiguration config) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(config.getType());
        result.setParameter(config.getParameter());
        return result;
    }
}
