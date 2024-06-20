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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.metadata.manager.ConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.ResourceMetaDataManager;
import org.apache.shardingsphere.mode.metadata.manager.ResourceSwitchManager;
import org.apache.shardingsphere.mode.metadata.manager.RuleItemManager;
import org.apache.shardingsphere.mode.metadata.manager.ShardingSphereDatabaseManager;
import org.apache.shardingsphere.mode.service.PersistServiceFacade;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Meta data context manager..
 */
@Getter
public class MetaDataContextManager {
    
    private final ShardingSphereDatabaseManager databaseManager;
    
    private final ConfigurationManager configurationManager;
    
    private final ResourceMetaDataManager resourceMetaDataManager;
    
    private final RuleItemManager ruleItemManager;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    public MetaDataContextManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                  final PersistServiceFacade persistServiceFacade) {
        resourceSwitchManager = new ResourceSwitchManager();
        databaseManager = new ShardingSphereDatabaseManager(metaDataContexts);
        configurationManager = new ConfigurationManager(metaDataContexts, computeNodeInstanceContext, persistServiceFacade, resourceSwitchManager);
        resourceMetaDataManager = new ResourceMetaDataManager(metaDataContexts, persistServiceFacade);
        ruleItemManager = new RuleItemManager(metaDataContexts, persistServiceFacade, configurationManager);
    }
}
