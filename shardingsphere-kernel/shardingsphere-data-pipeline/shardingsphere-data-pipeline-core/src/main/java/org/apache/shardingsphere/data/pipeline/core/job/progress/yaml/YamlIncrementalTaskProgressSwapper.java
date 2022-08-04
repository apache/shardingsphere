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

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgressItem;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PositionInitializerFactory;

/**
 * YAML IncrementalTask progress swapper.
 */
public final class YamlIncrementalTaskProgressSwapper {
    
    /**
     * Swap to YAML.
     * @param incremental incrementalTask progress
     * @return IncrementalTaskProgress
     */
    public YamlIncrementalTaskProgress swapToYaml(final IncrementalTaskProgress incremental) {
        if (incremental == null) {
            return null;
        }
        YamlIncrementalTaskProgress yamlIncrementalTaskProgress = new YamlIncrementalTaskProgress();
        yamlIncrementalTaskProgress.setDataSources(incremental.getIncrementalTaskProgressItemMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    YamlIncrementalTaskProgress.YamlIncrementalTaskProgressItem result = new YamlIncrementalTaskProgress.YamlIncrementalTaskProgressItem();
                    result.setPosition(entry.getValue().getPosition().toString());
                    result.setDelay(entry.getValue().getIncrementalTaskDelay());
                    return result;
                })));
        return yamlIncrementalTaskProgress;
    }
    
    /**
     * Swap to object.
     * @param databaseType databaseType
     * @param incremental yaml incrementalTask progress
     * @return incrementalTask progress
     */
    public IncrementalTaskProgress swapToObject(final String databaseType, final YamlIncrementalTaskProgress incremental) {
        if (incremental == null) {
            return null;
        }
        return new IncrementalTaskProgress(incremental.getDataSources().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    IncrementalTaskProgressItem result = new IncrementalTaskProgressItem();
                    result.setPosition(PositionInitializerFactory.getInstance(databaseType).init(entry.getValue().getPosition()));
                    result.setIncrementalTaskDelay(entry.getValue().getDelay());
                    return result;
                })));
    }
}
