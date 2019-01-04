/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.servcie;

import io.shardingsphere.shardingui.common.dto.InstanceDTO;
import io.shardingsphere.shardingui.common.dto.SlaveDataSourceDTO;

import java.util.Collection;

/**
 * Orchestration operation service.
 *
 * @author chenqingyang
 */
public interface OrchestrationService {
    
    /**
     * Get all instances.
     *
     * @return all instances
     */
    Collection<InstanceDTO> getALLInstance();
    
    /**
     * Update instance status.
     *
     * @param instanceId instance id
     * @param enabled enabled
     */
    void updateInstanceStatus(String instanceId, boolean enabled);
    
    /**
     * Get all slave data source.
     *
     * @return all slaver data source dto
     */
    Collection<SlaveDataSourceDTO> getAllSlaveDataSource();
    
    /**
     * update slave data source status.
     *
     * @param schemaNames schema name
     * @param slaveDataSourceName slave data source name
     * @param enabled enabled
     */
    void updateSlaveDataSourceStatus(String schemaNames, String slaveDataSourceName, boolean enabled);
}
