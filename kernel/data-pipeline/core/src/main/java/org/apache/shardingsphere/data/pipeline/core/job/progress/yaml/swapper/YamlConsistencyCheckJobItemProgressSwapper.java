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

package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.yaml.YamlTableCheckRangePositionSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlConsistencyCheckJobItemProgress;

import java.util.stream.Collectors;

/**
 * YAML data check job item progress swapper.
 */
public final class YamlConsistencyCheckJobItemProgressSwapper implements YamlPipelineJobItemProgressSwapper<YamlConsistencyCheckJobItemProgress, ConsistencyCheckJobItemProgress> {
    
    private final YamlTableCheckRangePositionSwapper tableCheckPositionSwapper = new YamlTableCheckRangePositionSwapper();
    
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
        result.setTableCheckRangePositions(data.getTableCheckRangePositions().stream().map(tableCheckPositionSwapper::swapToYamlConfiguration).collect(Collectors.toList()));
        result.setSourceDatabaseType(data.getSourceDatabaseType());
        return result;
    }
    
    @Override
    public ConsistencyCheckJobItemProgress swapToObject(final YamlConsistencyCheckJobItemProgress yamlConfig) {
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress(yamlConfig.getTableNames(), yamlConfig.getIgnoredTableNames(), yamlConfig.getCheckedRecordsCount(),
                yamlConfig.getRecordsCount(), yamlConfig.getCheckBeginTimeMillis(), yamlConfig.getCheckEndTimeMillis(), yamlConfig.getSourceDatabaseType());
        if (null != yamlConfig.getTableCheckRangePositions()) {
            result.getTableCheckRangePositions().addAll(yamlConfig.getTableCheckRangePositions().stream().map(tableCheckPositionSwapper::swapToObject).collect(Collectors.toList()));
        }
        result.setStatus(JobStatus.valueOf(yamlConfig.getStatus()));
        return result;
    }
    
    @Override
    public Class<YamlConsistencyCheckJobItemProgress> getYamlProgressClass() {
        return YamlConsistencyCheckJobItemProgress.class;
    }
}
