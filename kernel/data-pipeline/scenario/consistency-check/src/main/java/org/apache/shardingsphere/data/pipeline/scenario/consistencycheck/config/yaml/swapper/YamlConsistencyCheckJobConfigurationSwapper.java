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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.config.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

/**
 * YAML consistency check job configuration swapper.
 */
public final class YamlConsistencyCheckJobConfigurationSwapper implements YamlPipelineJobConfigurationSwapper<YamlConsistencyCheckJobConfiguration, ConsistencyCheckJobConfiguration> {
    
    @Override
    public YamlConsistencyCheckJobConfiguration swapToYamlConfiguration(final ConsistencyCheckJobConfiguration data) {
        YamlConsistencyCheckJobConfiguration result = new YamlConsistencyCheckJobConfiguration();
        result.setJobId(data.getJobId());
        result.setParentJobId(data.getParentJobId());
        result.setAlgorithmTypeName(data.getAlgorithmTypeName());
        result.setAlgorithmProps(data.getAlgorithmProps());
        result.setSourceDatabaseType(null == data.getSourceDatabaseType() ? null : data.getSourceDatabaseType().getType());
        return result;
    }
    
    @Override
    public ConsistencyCheckJobConfiguration swapToObject(final YamlConsistencyCheckJobConfiguration yamlConfig) {
        DatabaseType databaseType = null == yamlConfig.getSourceDatabaseType() ? null : TypedSPILoader.getService(DatabaseType.class, yamlConfig.getSourceDatabaseType());
        return new ConsistencyCheckJobConfiguration(yamlConfig.getJobId(), yamlConfig.getParentJobId(), yamlConfig.getAlgorithmTypeName(), yamlConfig.getAlgorithmProps(), databaseType);
    }
    
    @Override
    public ConsistencyCheckJobConfiguration swapToObject(final String jobParam) {
        return null == jobParam ? null : swapToObject(YamlEngine.unmarshal(jobParam, YamlConsistencyCheckJobConfiguration.class, true));
    }
}
