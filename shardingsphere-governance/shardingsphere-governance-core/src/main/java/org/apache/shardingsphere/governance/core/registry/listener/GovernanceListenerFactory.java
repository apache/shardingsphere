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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Collection;

/**
 * Governance listener factory.
 */
public interface GovernanceListenerFactory {
    
    /**
     * Create governance listener.
     * 
     * @param registryCenterRepository governance repository
     * @param schemaNames schema names
     * @return governance listener
     */
    GovernanceListener create(RegistryCenterRepository registryCenterRepository, Collection<String> schemaNames);
    
    /**
     * Get watch types.
     * 
     * @return watch types
     */
    Collection<Type> getWatchTypes();
}
