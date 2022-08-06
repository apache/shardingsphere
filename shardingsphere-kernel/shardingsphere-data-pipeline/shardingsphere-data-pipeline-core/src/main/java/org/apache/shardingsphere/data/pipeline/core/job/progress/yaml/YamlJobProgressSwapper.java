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
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;

/**
 * YAML Job progress swapper.
 */
public final class YamlJobProgressSwapper {
    
    /**
     * Swap to YAML.
     *
     * @param jobProgress job progress
     * @return YAML job progress
     */
    public YamlJobProgress swapToYaml(final JobProgress jobProgress) {
        YamlJobProgress result = new YamlJobProgress();
        result.setStatus(jobProgress.getStatus().name());
        result.setSourceDatabaseType(jobProgress.getSourceDatabaseType());
        result.setInventory(new YamlInventoryTaskProgressSwapper().swapToYaml(jobProgress.getJobInventoryTask()));
        result.setIncremental(new YamlIncrementalTaskProgressSwapper().swapToYaml(jobProgress.getJobIncrementalTask()));
        return result;
    }
    
    /**
     * Swap to object.
     *
     * @param yamlJobProgress yaml job progress
     * @return job progress
     */
    public JobProgress swapToObject(final YamlJobProgress yamlJobProgress) {
        JobProgress result = new JobProgress();
        result.setStatus(JobStatus.valueOf(yamlJobProgress.getStatus()));
        result.setSourceDatabaseType(yamlJobProgress.getSourceDatabaseType());
        result.setJobInventoryTask(new YamlInventoryTaskProgressSwapper().swapToObject(yamlJobProgress.getInventory()));
        result.setJobIncrementalTask(new YamlIncrementalTaskProgressSwapper().swapToObject(yamlJobProgress.getSourceDatabaseType(), yamlJobProgress.getIncremental()));
        return result;
    }
}
