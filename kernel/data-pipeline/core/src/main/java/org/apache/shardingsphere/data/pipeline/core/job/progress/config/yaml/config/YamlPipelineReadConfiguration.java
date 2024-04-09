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

package org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;

/**
 * YAML pipeline read configuration.
 */
@Getter
@Setter
public final class YamlPipelineReadConfiguration implements YamlConfiguration {
    
    private static final Integer DEFAULT_WORKER_THREAD = 20;
    
    private static final Integer DEFAULT_BATCH_SIZE = 1000;
    
    private static final Integer DEFAULT_SHARDING_SIZE = 10000000;
    
    private Integer workerThread;
    
    private Integer batchSize;
    
    private Integer shardingSize;
    
    private YamlAlgorithmConfiguration rateLimiter;
    
    /**
     * Build with default value.
     *
     * @return read configuration
     */
    public static YamlPipelineReadConfiguration buildWithDefaultValue() {
        YamlPipelineReadConfiguration result = new YamlPipelineReadConfiguration();
        result.workerThread = DEFAULT_WORKER_THREAD;
        result.batchSize = DEFAULT_BATCH_SIZE;
        result.shardingSize = DEFAULT_SHARDING_SIZE;
        return result;
    }
    
    /**
     * Fill in null fields with default value.
     */
    public void fillInNullFieldsWithDefaultValue() {
        if (null == workerThread) {
            workerThread = DEFAULT_WORKER_THREAD;
        }
        if (null == batchSize) {
            batchSize = DEFAULT_BATCH_SIZE;
        }
        if (null == shardingSize) {
            shardingSize = DEFAULT_SHARDING_SIZE;
        }
    }
}
