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

package org.apache.shardingsphere.data.pipeline.common.registrycenter.repository;

import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

/**
 * Governance repository API.
 */
public interface GovernanceRepositoryAPI {
    
    /**
     * Get job configuration governance repository.
     * 
     * @return job configuration governance repository
     */
    PipelineJobConfigurationGovernanceRepository getJobConfigurationGovernanceRepository();
    
    /**
     * Get job offset governance repository.
     * 
     * @return job offset governance repository
     */
    PipelineJobOffsetGovernanceRepository getJobOffsetGovernanceRepository();
    
    /**
     * Get job item process governance repository.
     *
     * @return job item process governance repository
     */
    PipelineJobItemProcessGovernanceRepository getJobItemProcessGovernanceRepository();
    
    /**
     * Get job item error message governance repository.
     * 
     * @return job item error message governance repository
     */
    PipelineJobItemErrorMessageGovernanceRepository getJobItemErrorMessageGovernanceRepository();
    
    /**
     * Get job check governance repository.
     * 
     * @return job check governance repository
     */
    PipelineJobCheckGovernanceRepository getJobCheckGovernanceRepository();
    
    /**
     * Get job governance repository.
     * 
     * @return job governance repository
     */
    PipelineJobGovernanceRepository getJobGovernanceRepository();
    
    /**
     * Get meta data data source governance repository.
     *
     * @return meta data data source governance repository
     */
    PipelineMetaDataDataSourceGovernanceRepository getMetaDataDataSourceGovernanceRepository();
    
    /**
     * Get meta data process configuration governance repository.
     * 
     * @return meta data process configuration governance repository
     */
    PipelineMetaDataProcessConfigurationGovernanceRepository getMetaDataProcessConfigurationGovernanceRepository();
    
    /**
     * Watch pipeLine root path.
     *
     * @param listener data changed event listener
     */
    void watchPipeLineRootPath(DataChangedEventListener listener);
}
