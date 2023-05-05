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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;

/**
 * YAML job item incremental tasks progress swapper.
 */
@Slf4j
public final class YamlJobItemIncrementalTasksProgressSwapper {
    
    /**
     * Swap to YAML.
     *
     * @param progress progress
     * @return YAML progress
     */
    public YamlJobItemIncrementalTasksProgress swapToYaml(final JobItemIncrementalTasksProgress progress) {
        if (null == progress) {
            return new YamlJobItemIncrementalTasksProgress();
        }
        IncrementalTaskProgress incrementalTaskProgress = progress.getIncrementalTaskProgress();
        if (null == incrementalTaskProgress) {
            return new YamlJobItemIncrementalTasksProgress();
        }
        YamlJobItemIncrementalTasksProgress result = new YamlJobItemIncrementalTasksProgress();
        result.setPosition(progress.getIncrementalTaskProgress().getPosition().toString());
        result.setLastEventTimestamp(progress.getIncrementalTaskProgress().getLastEventTimestamp());
        result.setLatestActiveMillis(progress.getIncrementalTaskProgress().getLatestActiveMillis());
        return result;
    }
    
    /**
     * Swap to object.
     *
     * @param databaseType database type
     * @param yamlProgress YAML progress
     * @return progress
     */
    public JobItemIncrementalTasksProgress swapToObject(final String databaseType, final YamlJobItemIncrementalTasksProgress yamlProgress) {
        if (null == yamlProgress) {
            return new JobItemIncrementalTasksProgress(null);
        }
        final IncrementalTaskProgress taskProgress = new IncrementalTaskProgress();
        // TODO consider to remove parameter databaseType
        PositionInitializer positionInitializer = PipelineTypedSPILoader.getDatabaseTypedService(PositionInitializer.class, databaseType);
        log.error("===positionInitializer==" + positionInitializer);
        log.error("===yamlProgress==" + yamlProgress);
        log.error("===yamlProgress.getPosition()==" + yamlProgress.getPosition());
        log.error("===positionInitializer.init(yamlProgress.getPosition())==" + positionInitializer.init(yamlProgress.getPosition()));
        taskProgress.setPosition(positionInitializer.init(yamlProgress.getPosition()));
        taskProgress.setLastEventTimestamp(yamlProgress.getLastEventTimestamp());
        taskProgress.setLatestActiveMillis(yamlProgress.getLatestActiveMillis());
        return new JobItemIncrementalTasksProgress(taskProgress);
    }
}
