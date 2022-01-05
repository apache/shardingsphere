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

package org.apache.shardingsphere.mode.metadata.persist.service;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Compute node persist service.
 */
@RequiredArgsConstructor
public final class ComputeNodePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist instance labels.
     * 
     * @param instanceId instance id
     * @param labels collection of label
     */
    public void persistInstanceLabels(final String instanceId, final Collection<String> labels) {
        repository.persist(ComputeNode.getInstanceLabelNodePath(instanceId), YamlEngine.marshal(labels));
    }
    
    /**
     * Load instance labels.
     * 
     * @param instanceId instance id
     * @return collection of label
     */
    public Collection<String> loadInstanceLabels(final String instanceId) {
        String yamlContent = repository.get(ComputeNode.getInstanceLabelNodePath(instanceId));
        return Strings.isNullOrEmpty(yamlContent) ? new ArrayList<>() : YamlEngine.unmarshal(yamlContent, Collection.class);
    }
    
    /**
     * Load all compute node instances.
     * 
     * @return collection of compute node instance
     */
    public Collection<ComputeNodeInstance> loadAllComputeNodeInstances() {
        Collection<String> onlineComputeNodes = repository.getChildrenKeys(ComputeNode.getOnlineNodePath());
        List<ComputeNodeInstance> result = new ArrayList<>(onlineComputeNodes.size());
        onlineComputeNodes.forEach(each -> {
            ComputeNodeInstance instance = new ComputeNodeInstance();
            instance.setIp(Splitter.on("@").splitToList(each).get(0));
            instance.setPort(Splitter.on("@").splitToList(each).get(1));
            instance.setLabels(loadInstanceLabels(each));
            result.add(instance);
        });
        return result;
    }
}
