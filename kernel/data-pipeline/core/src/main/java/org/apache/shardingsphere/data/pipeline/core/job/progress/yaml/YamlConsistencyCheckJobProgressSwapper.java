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

package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml;

import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.ConsistencyCheckJobProgress;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML data check job progress swapper.
 */
public final class YamlConsistencyCheckJobProgressSwapper implements YamlConfigurationSwapper<YamlConsistencyCheckJobProgress, ConsistencyCheckJobProgress> {
    
    @Override
    public YamlConsistencyCheckJobProgress swapToYamlConfiguration(final ConsistencyCheckJobProgress data) {
        YamlConsistencyCheckJobProgress result = new YamlConsistencyCheckJobProgress();
        result.setStatus(data.getStatus().name());
        result.setRecordsCount(data.getRecordsCount());
        result.setCheckedRecordsCount(data.getCheckedRecordsCount());
        result.setCheckBeginTimeMillis(data.getCheckBeginTimeMillis());
        result.setCheckEndTimeMillis(data.getCheckEndTimeMillis());
        result.setTableNames(data.getTableNames());
        return result;
    }
    
    @Override
    public ConsistencyCheckJobProgress swapToObject(final YamlConsistencyCheckJobProgress yamlConfig) {
        ConsistencyCheckJobProgress result = new ConsistencyCheckJobProgress();
        result.setStatus(JobStatus.valueOf(yamlConfig.getStatus()));
        result.setRecordsCount(yamlConfig.getRecordsCount());
        result.setCheckedRecordsCount(yamlConfig.getCheckedRecordsCount());
        result.setCheckBeginTimeMillis(yamlConfig.getCheckBeginTimeMillis());
        result.setCheckEndTimeMillis(yamlConfig.getCheckEndTimeMillis());
        result.setTableNames(yamlConfig.getTableNames());
        return result;
    }
}
