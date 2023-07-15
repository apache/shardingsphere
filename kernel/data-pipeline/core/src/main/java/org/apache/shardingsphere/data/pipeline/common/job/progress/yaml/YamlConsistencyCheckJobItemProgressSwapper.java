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

package org.apache.shardingsphere.data.pipeline.common.job.progress.yaml;

import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML data check job item progress swapper.
 */
public final class YamlConsistencyCheckJobItemProgressSwapper implements YamlConfigurationSwapper<YamlConsistencyCheckJobItemProgress, ConsistencyCheckJobItemProgress> {
    
    @Override
    public YamlConsistencyCheckJobItemProgress swapToYamlConfiguration(final ConsistencyCheckJobItemProgress data) {
        YamlConsistencyCheckJobItemProgress result = new YamlConsistencyCheckJobItemProgress();
        result.setStatus(data.getStatus().name());
        result.setTableNames(data.getTableNames());
        result.setIgnoredTableNames(data.getIgnoredTableNames());
        result.setCheckedRecordsCount(data.getCheckedRecordsCount());
        result.setRecordsCount(data.getRecordsCount());
        result.setCheckBeginTimeMillis(data.getCheckBeginTimeMillis());
        result.setCheckEndTimeMillis(data.getCheckEndTimeMillis());
        result.setTableCheckPositions(data.getTableCheckPositions());
        return result;
    }
    
    @Override
    public ConsistencyCheckJobItemProgress swapToObject(final YamlConsistencyCheckJobItemProgress yamlConfig) {
        Map<String, Object> tableCheckPositions = new LinkedHashMap<>();
        if (null != yamlConfig.getTableCheckPositions()) {
            tableCheckPositions.putAll(yamlConfig.getTableCheckPositions());
        }
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress(yamlConfig.getTableNames(), yamlConfig.getIgnoredTableNames(), yamlConfig.getCheckedRecordsCount(),
                yamlConfig.getRecordsCount(), yamlConfig.getCheckBeginTimeMillis(), yamlConfig.getCheckEndTimeMillis(), tableCheckPositions);
        result.setStatus(JobStatus.valueOf(yamlConfig.getStatus()));
        return result;
    }
}
