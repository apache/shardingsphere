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

package org.apache.shardingsphere.ui.servcie;


import org.apache.shardingsphere.ui.common.domain.CenterConfig;
import org.apache.shardingsphere.ui.common.domain.CenterConfigs;
import org.apache.shardingsphere.ui.common.dto.CenterConfigDTO;

import java.util.Optional;

/**
 * Center config service.
 */
public interface CenterConfigService {
    
    /**
     * Load center config.
     *
     * @param name center config name
     * @param orchestrationType orchestration type
     * @return center config
     */
    CenterConfig load(String name, String orchestrationType);
    
    /**
     * Load the activated center config.
     *
     * @param orchestrationType orchestration type
     * @return activated center config
     */
    Optional<CenterConfig> loadActivated(String orchestrationType);
    
    /**
     * Add center config.
     *
     * @param config center config
     */
    void add(CenterConfig config);
    
    /**
     * Delete center config.
     *
     * @param name center config name
     * @param orchestrationType orchestration type
     */
    void delete(String name, String orchestrationType);
    
    /**
     * Set activated center config.
     *
     * @param name center config name
     * @param orchestrationType orchestration type
     */
    void setActivated(String name, String orchestrationType);
    
    /**
     * Load all center configs.
     *
     * @return all center configs.
     */
    CenterConfigs loadAll();
    
    /**
     * Load all center configs.
     *
     * @param orchestrationType orchestration type
     * @return all center configs.
     */
    CenterConfigs loadAll(String orchestrationType);
    
    /**
     * update config center
     *
     * @param config center config
     */
    void update(CenterConfigDTO config);
}
