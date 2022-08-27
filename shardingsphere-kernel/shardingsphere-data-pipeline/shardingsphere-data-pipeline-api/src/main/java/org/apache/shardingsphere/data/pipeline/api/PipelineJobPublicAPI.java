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

package org.apache.shardingsphere.data.pipeline.api;

import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.sql.SQLException;
import java.util.List;

/**
 * Pipeline job public API.
 */
public interface PipelineJobPublicAPI extends TypedSPI {
    
    /**
     * Create process configuration.
     *
     * @param processConfig process configuration
     */
    void createProcessConfiguration(PipelineProcessConfiguration processConfig);
    
    /**
     * Alter process configuration.
     *
     * @param processConfig process configuration
     */
    void alterProcessConfiguration(PipelineProcessConfiguration processConfig);
    
    /**
     * Drop process configuration.
     *
     * @param confPath configuration path. e.g. <code>/</code>, <code>/READ</code>, <code>/READ/RATE_LIMITER</code>
     */
    void dropProcessConfiguration(String confPath);
    
    /**
     * Show process configuration.
     *
     * @return process configuration, non-null
     */
    PipelineProcessConfiguration showProcessConfiguration();
    
    /**
     * Start disabled job.
     *
     * @param jobId job id
     */
    void startDisabledJob(String jobId);
    
    /**
     * Stop pipeline job.
     *
     * @param jobId job id
     */
    void stop(String jobId);
    
    /**
     * Rollback pipeline job.
     *
     * @param jobId job id
     * @throws SQLException when rollback underlying database data
     */
    void rollback(String jobId) throws SQLException;
    
    /**
     * Commit pipeline job.
     *
     * @param jobId job id
     */
    void commit(String jobId);
    
    /**
     * Get pipeline job info.
     *
     * @return jobInfos
     */
    List<? extends PipelineJobInfo> list();
}
