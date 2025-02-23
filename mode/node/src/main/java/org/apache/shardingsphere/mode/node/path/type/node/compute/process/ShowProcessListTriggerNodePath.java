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

package org.apache.shardingsphere.mode.node.path.type.node.compute.process;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;

/**
 * Show process list trigger node path.
 */
@NodePathEntity("/nodes/compute_nodes/show_process_list_trigger/${instanceProcess}")
@RequiredArgsConstructor
@Getter
public final class ShowProcessListTriggerNodePath implements NodePath {
    
    private final InstanceProcessNodeValue instanceProcess;
    
    public ShowProcessListTriggerNodePath() {
        this(new InstanceProcessNodeValue(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER));
    }
    
    /**
     * Create instance ID search criteria.
     *
     * @return created search criteria
     */
    public static NodePathSearchCriteria createInstanceIdSearchCriteria() {
        return new NodePathSearchCriteria(new ShowProcessListTriggerNodePath(), false, false, 1);
    }
    
    /**
     * Create process ID search criteria.
     *
     * @return created search criteria
     */
    public static NodePathSearchCriteria createProcessIdSearchCriteria() {
        return new NodePathSearchCriteria(new ShowProcessListTriggerNodePath(), false, false, 2);
    }
}
