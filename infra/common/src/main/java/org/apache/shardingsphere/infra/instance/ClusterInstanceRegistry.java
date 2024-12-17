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

package org.apache.shardingsphere.infra.instance;

import lombok.Getter;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cluster instance registry.
 */
@Getter
public final class ClusterInstanceRegistry {
    
    private final Collection<ComputeNodeInstance> allClusterInstances = new CopyOnWriteArrayList<>();
    
    /**
     * Find compute node instance.
     *
     * @param instanceId instance ID
     * @return compute node instance
     */
    public Optional<ComputeNodeInstance> find(final String instanceId) {
        return allClusterInstances.stream().filter(each -> instanceId.equals(each.getMetaData().getId())).findFirst();
    }
    
    /**
     * Add compute node instance.
     *
     * @param instance compute node instance
     */
    public void add(final ComputeNodeInstance instance) {
        allClusterInstances.removeIf(each -> each.getMetaData().getId().equalsIgnoreCase(instance.getMetaData().getId()));
        allClusterInstances.add(instance);
    }
    
    /**
     * Delete compute node instance.
     *
     * @param instance compute node instance
     */
    public void delete(final ComputeNodeInstance instance) {
        allClusterInstances.removeIf(each -> each.getMetaData().getId().equalsIgnoreCase(instance.getMetaData().getId()));
    }
}
