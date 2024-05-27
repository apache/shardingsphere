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

package org.apache.shardingsphere.mode.service;

import lombok.Getter;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.service.manager.ConfigurationManagerService;
import org.apache.shardingsphere.mode.service.manager.ResourceMetaDataManagerService;
import org.apache.shardingsphere.mode.service.manager.ShardingSphereDatabaseManagerService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Context manager service facade.
 */
@Getter
public class ManagerServiceFacade {
    
    private final ShardingSphereDatabaseManagerService databaseManagerService;
    
    private final ConfigurationManagerService configurationManagerService;
    
    private final ResourceMetaDataManagerService resourceMetaDataManagerService;
    
    public ManagerServiceFacade(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                final PersistServiceFacade persistServiceFacade) {
        databaseManagerService = new ShardingSphereDatabaseManagerService(metaDataContexts);
        configurationManagerService = new ConfigurationManagerService(metaDataContexts, computeNodeInstanceContext, persistServiceFacade);
        resourceMetaDataManagerService = new ResourceMetaDataManagerService(metaDataContexts, persistServiceFacade);
    }
}
