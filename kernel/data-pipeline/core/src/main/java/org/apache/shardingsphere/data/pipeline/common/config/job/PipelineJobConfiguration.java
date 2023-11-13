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

package org.apache.shardingsphere.data.pipeline.common.config.job;

import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.listener.PipelineElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Pipeline job configuration.
 */
public interface PipelineJobConfiguration {
    
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get job id.
     *
     * @return job id
     */
    String getJobId();
    
    /**
     * Get job sharding count.
     *
     * @return job sharding count
     */
    int getJobShardingCount();
    
    /**
     * Get source database type.
     *
     * @return source database type
     */
    DatabaseType getSourceDatabaseType();
    
    /**
     * Convert to job configuration POJO.
     * 
     * @return converted job configuration POJO
     */
    default JobConfigurationPOJO convertToJobConfigurationPOJO() {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName(getJobId());
        result.setShardingTotalCount(getJobShardingCount());
        result.setJobParameter(YamlEngine.marshal(swapToYamlJobConfiguration()));
        String createTimeFormat = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        result.getProps().setProperty("create_time", createTimeFormat);
        result.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        result.getProps().setProperty("run_count", "1");
        result.setJobListenerTypes(Collections.singletonList(PipelineElasticJobListener.class.getName()));
        return result;
    }
    
    /**
     * Swap to YAML pipeline job configuration.
     * 
     * @return swapped YAML pipeline job configuration
     */
    YamlPipelineJobConfiguration swapToYamlJobConfiguration();
}
