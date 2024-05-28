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
import org.apache.shardingsphere.mode.service.manager.ConfigurationManager;
import org.apache.shardingsphere.mode.service.manager.ResourceMetaDataManager;
import org.apache.shardingsphere.mode.service.manager.ShardingSphereDatabaseManager;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Meta data context manager..
 */
@Getter
public class MetaDataContextManager {
    
    private final ShardingSphereDatabaseManager databaseManager;
    
    private final ConfigurationManager configurationManager;
    
    private final ResourceMetaDataManager resourceMetaDataManager;
    
    public MetaDataContextManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                  final PersistServiceFacade persistServiceFacade) {
        databaseManager = new ShardingSphereDatabaseManager(metaDataContexts);
        configurationManager = new ConfigurationManager(metaDataContexts, computeNodeInstanceContext, persistServiceFacade);
        resourceMetaDataManager = new ResourceMetaDataManager(metaDataContexts, persistServiceFacade);
    }
}
