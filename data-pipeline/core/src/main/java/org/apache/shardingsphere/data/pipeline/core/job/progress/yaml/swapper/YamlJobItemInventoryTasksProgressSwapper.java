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

import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlJobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.task.progress.InventoryTaskProgress;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * YAML job item inventory tasks progress swapper.
 */
public final class YamlJobItemInventoryTasksProgressSwapper {
    
    /**
     * Swap to YAML.
     *
     * @param progress progress
     * @return YAML progress
     */
    public YamlJobItemInventoryTasksProgress swapToYaml(final JobItemInventoryTasksProgress progress) {
        YamlJobItemInventoryTasksProgress result = new YamlJobItemInventoryTasksProgress();
        if (null != progress) {
            result.setFinished(getFinished(progress));
            result.setUnfinished(getUnfinished(progress));
        }
        return result;
    }
    
    private String[] getFinished(final JobItemInventoryTasksProgress progress) {
        return progress.getProgresses().entrySet().stream()
                .filter(entry -> entry.getValue().getPosition() instanceof IngestFinishedPosition)
                .map(Entry::getKey).toArray(String[]::new);
    }
    
    private Map<String, String> getUnfinished(final JobItemInventoryTasksProgress progress) {
        return progress.getProgresses().entrySet().stream()
                .filter(entry -> !(entry.getValue().getPosition() instanceof IngestFinishedPosition))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition().toString()));
    }
    
    /**
     * Swap to object.
     *
     * @param yamlProgress YAML progress
     * @return progress
     */
    public JobItemInventoryTasksProgress swapToObject(final YamlJobItemInventoryTasksProgress yamlProgress) {
        if (null == yamlProgress) {
            return new JobItemInventoryTasksProgress(Collections.emptyMap());
        }
        Map<String, InventoryTaskProgress> taskProgressMap = new LinkedHashMap<>(yamlProgress.getFinished().length + yamlProgress.getUnfinished().size(), 1F);
        taskProgressMap.putAll(Arrays.stream(yamlProgress.getFinished()).collect(Collectors.toMap(key -> key, value -> new InventoryTaskProgress(new IngestFinishedPosition()))));
        taskProgressMap.putAll(yamlProgress.getUnfinished().entrySet().stream().collect(Collectors.toMap(Entry::getKey, getInventoryTaskProgressFunction())));
        return new JobItemInventoryTasksProgress(taskProgressMap);
    }
    
    private Function<Entry<String, String>, InventoryTaskProgress> getInventoryTaskProgressFunction() {
        return entry -> new InventoryTaskProgress(
                Strings.isNullOrEmpty(entry.getValue()) ? new IngestPlaceholderPosition() : UniqueKeyIngestPosition.decode(entry.getValue()));
    }
}
