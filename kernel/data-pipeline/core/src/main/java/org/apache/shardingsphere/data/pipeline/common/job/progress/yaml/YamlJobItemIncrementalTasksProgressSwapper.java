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

import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

/**
 * YAML job item incremental tasks progress swapper.
 */
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
        result.setDelay(progress.getIncrementalTaskProgress().getIncrementalTaskDelay());
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
        // TODO consider to remove parameter databaseType
        PositionInitializer positionInitializer = DatabaseTypedSPILoader.getService(PositionInitializer.class, TypedSPILoader.getService(DatabaseType.class, databaseType));
        IncrementalTaskProgress taskProgress = new IncrementalTaskProgress(positionInitializer.init(yamlProgress.getPosition()));
        taskProgress.setIncrementalTaskDelay(yamlProgress.getDelay());
        return new JobItemIncrementalTasksProgress(taskProgress);
    }
}
