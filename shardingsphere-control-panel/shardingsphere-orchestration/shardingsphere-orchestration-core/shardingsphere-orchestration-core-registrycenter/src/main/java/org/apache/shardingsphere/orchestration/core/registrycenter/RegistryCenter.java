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

package org.apache.shardingsphere.orchestration.core.registrycenter;

import org.apache.shardingsphere.orchestration.center.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.core.registrycenter.instance.OrchestrationInstance;

/**
 * RegistryCenter hold and persist instance state.
 */
public final class RegistryCenter {
    
    private final RegistryCenterNode node;
    
    private final RegistryCenterRepository repository;
    
    private final OrchestrationInstance instance;
    
    public RegistryCenter(final String name, final RegistryCenterRepository registryCenterRepository) {
        this.node = new RegistryCenterNode(name);
        this.repository = registryCenterRepository;
        this.instance = OrchestrationInstance.getInstance();
    }
    
    /**
     * Persist instance online.
     */
    public void persistInstanceOnline() {
        repository.persistEphemeral(node.getInstancesNodeFullPath(instance.getInstanceId()), "");
    }
    
    /**
     * Initialize data sources node.
     */
    public void persistDataSourcesNode() {
        repository.persist(node.getDataSourcesNodeFullRootPath(), "");
    }
    
    /**
     * Persist instance data.
     * @param instanceData instance data
     */
    public void persistInstanceData(final String instanceData) {
        repository.persist(node.getInstancesNodeFullPath(instance.getInstanceId()), instanceData);
    }
    
    /**
     * Load instance data.
     * @return instance data
     */
    public String loadInstanceData() {
        return repository.get(node.getInstancesNodeFullPath(instance.getInstanceId()));
    }
}
