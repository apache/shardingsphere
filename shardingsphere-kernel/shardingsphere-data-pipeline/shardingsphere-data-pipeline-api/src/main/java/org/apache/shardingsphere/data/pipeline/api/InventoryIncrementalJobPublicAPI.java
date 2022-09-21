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

import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.sql.SQLException;

/**
 * Inventory incremental job API.
 */
public interface InventoryIncrementalJobPublicAPI extends PipelineJobPublicAPI, TypedSPI {
    
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
     * @throws SQLException when commit underlying database data
     */
    void commit(String jobId) throws SQLException;
}
