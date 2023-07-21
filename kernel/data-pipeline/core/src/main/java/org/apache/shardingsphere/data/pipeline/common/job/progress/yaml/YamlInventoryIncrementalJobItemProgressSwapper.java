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
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML inventory incremental job item progress swapper.
 */
public final class YamlInventoryIncrementalJobItemProgressSwapper implements YamlConfigurationSwapper<YamlInventoryIncrementalJobItemProgress, InventoryIncrementalJobItemProgress> {
    
    private final YamlJobItemInventoryTasksProgressSwapper inventoryTasksProgressSwapper = new YamlJobItemInventoryTasksProgressSwapper();
    
    private final YamlJobItemIncrementalTasksProgressSwapper incrementalTasksProgressSwapper = new YamlJobItemIncrementalTasksProgressSwapper();
    
    @Override
    public YamlInventoryIncrementalJobItemProgress swapToYamlConfiguration(final InventoryIncrementalJobItemProgress progress) {
        YamlInventoryIncrementalJobItemProgress result = new YamlInventoryIncrementalJobItemProgress();
        result.setStatus(progress.getStatus().name());
        result.setSourceDatabaseType(progress.getSourceDatabaseType().getType());
        result.setDataSourceName(progress.getDataSourceName());
        result.setInventory(inventoryTasksProgressSwapper.swapToYaml(progress.getInventory()));
        result.setIncremental(incrementalTasksProgressSwapper.swapToYaml(progress.getIncremental()));
        result.setProcessedRecordsCount(progress.getProcessedRecordsCount());
        result.setInventoryRecordsCount(progress.getInventoryRecordsCount());
        return result;
    }
    
    @Override
    public InventoryIncrementalJobItemProgress swapToObject(final YamlInventoryIncrementalJobItemProgress yamlProgress) {
        InventoryIncrementalJobItemProgress result = new InventoryIncrementalJobItemProgress();
        result.setStatus(JobStatus.valueOf(yamlProgress.getStatus()));
        result.setSourceDatabaseType(TypedSPILoader.getService(DatabaseType.class, yamlProgress.getSourceDatabaseType()));
        result.setDataSourceName(yamlProgress.getDataSourceName());
        result.setInventory(inventoryTasksProgressSwapper.swapToObject(yamlProgress.getInventory()));
        result.setIncremental(incrementalTasksProgressSwapper.swapToObject(yamlProgress.getSourceDatabaseType(), yamlProgress.getIncremental()));
        result.setProcessedRecordsCount(yamlProgress.getProcessedRecordsCount());
        result.setInventoryRecordsCount(yamlProgress.getInventoryRecordsCount());
        return result;
    }
}
