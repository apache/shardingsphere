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
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Inventory incremental job public API.
 */
@SingletonSPI
public interface InventoryIncrementalJobPublicAPI extends PipelineJobPublicAPI, TypedSPI {
    
    /**
     * Alter process configuration.
     *
     * @param processConfig process configuration
     */
    void alterProcessConfiguration(PipelineProcessConfiguration processConfig);
    
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
     */
    void commit(String jobId);
    
    /**
     * Get job infos.
     *
     * @param jobId job id
     * @return job item infos
     */
    List<InventoryIncrementalJobItemInfo> getJobItemInfos(String jobId);
    
    /**
     * List all data consistency check algorithms from SPI.
     *
     * @return data consistency check algorithms
     */
    Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms();
}
