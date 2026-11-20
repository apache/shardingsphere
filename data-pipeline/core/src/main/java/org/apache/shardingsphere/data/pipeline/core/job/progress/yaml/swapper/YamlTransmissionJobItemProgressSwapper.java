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

import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlTransmissionJobItemProgress;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * YAML transmission job item progress swapper.
 */
public final class YamlTransmissionJobItemProgressSwapper implements YamlPipelineJobItemProgressSwapper<YamlTransmissionJobItemProgress, TransmissionJobItemProgress> {
    
    private final YamlJobItemInventoryTasksProgressSwapper inventoryTasksProgressSwapper = new YamlJobItemInventoryTasksProgressSwapper();
    
    private final YamlJobItemIncrementalTasksProgressSwapper incrementalTasksProgressSwapper = new YamlJobItemIncrementalTasksProgressSwapper();
    
    @Override
    public YamlTransmissionJobItemProgress swapToYamlConfiguration(final TransmissionJobItemProgress progress) {
        YamlTransmissionJobItemProgress result = new YamlTransmissionJobItemProgress();
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
    public TransmissionJobItemProgress swapToObject(final YamlTransmissionJobItemProgress yamlProgress) {
        TransmissionJobItemProgress result = new TransmissionJobItemProgress();
        result.setStatus(JobStatus.valueOf(yamlProgress.getStatus()));
        result.setSourceDatabaseType(TypedSPILoader.getService(DatabaseType.class, yamlProgress.getSourceDatabaseType()));
        result.setDataSourceName(yamlProgress.getDataSourceName());
        result.setInventory(inventoryTasksProgressSwapper.swapToObject(yamlProgress.getInventory()));
        result.setIncremental(incrementalTasksProgressSwapper.swapToObject(yamlProgress.getSourceDatabaseType(), yamlProgress.getIncremental()));
        result.setProcessedRecordsCount(yamlProgress.getProcessedRecordsCount());
        result.setInventoryRecordsCount(yamlProgress.getInventoryRecordsCount());
        return result;
    }
    
    @Override
    public Class<YamlTransmissionJobItemProgress> getYamlProgressClass() {
        return YamlTransmissionJobItemProgress.class;
    }
}
