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

import java.util.Collections;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobIncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PositionInitializerFactory;

/**
 * YAML IncrementalTask progress swapper.
 */
public final class YamlIncrementalTaskProgressSwapper {
    
    /**
     * Swap to YAML.
     *
     * @param jobIncrementalTask incrementalTask progress
     * @return IncrementalTaskProgress
     */
    public YamlIncrementalTaskProgress swapToYaml(final JobIncrementalTaskProgress jobIncrementalTask) {
        if (null == jobIncrementalTask) {
            return null;
        }
        return jobIncrementalTask.getIncrementalTaskProgressMap()
                .entrySet()
                .stream()
                .map(entry -> {
                    YamlIncrementalTaskProgress yamlIncrementalTaskProgress = new YamlIncrementalTaskProgress();
                    yamlIncrementalTaskProgress.setDataSourceName(entry.getKey());
                    yamlIncrementalTaskProgress.setPosition(entry.getValue().getPosition().toString());
                    yamlIncrementalTaskProgress.setDelay(entry.getValue().getIncrementalTaskDelay());
                    return yamlIncrementalTaskProgress;
                }).findAny().orElse(null);
    }
    
    /**
     * Swap to object.
     *
     * @param databaseType databaseType
     * @param incremental yaml incrementalTask progress
     * @return incrementalTask progress
     */
    public JobIncrementalTaskProgress swapToObject(final String databaseType, final YamlIncrementalTaskProgress incremental) {
        if (null == incremental) {
            return null;
        }
        IncrementalTaskProgress result = new IncrementalTaskProgress();
        result.setPosition(PositionInitializerFactory.getInstance(databaseType).init(incremental.getPosition()));
        result.setIncrementalTaskDelay(incremental.getDelay());
        return new JobIncrementalTaskProgress(Collections.singletonMap(incremental.getDataSourceName(), result));
    }
}
