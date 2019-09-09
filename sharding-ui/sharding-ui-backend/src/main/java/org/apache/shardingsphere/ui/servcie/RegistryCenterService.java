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

import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.internal.registry.state.node.StateNode;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;

/**
 * Registry center service.
 *
 * @author chenqingyang
 */
public interface RegistryCenterService {
    
    /**
     * Get activated registry center.
     *
     * @return registry center
     */
    RegistryCenter getActivatedRegistryCenter();
    
    /**
     * Get activated configuration node.
     *
     * @return configuration node
     */
    ConfigurationNode getActivateConfigurationNode();
    
    /**
     * Get activated state node.
     *
     * @return state node
     */
    StateNode getActivatedStateNode();
}
