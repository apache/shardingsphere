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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.NewRegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber.NewProcessListChangedSubscriber;

/**
 * TODO replace the old implementation after meta data refactor completed
 * New context manager subscriber facade.
 */
public final class NewContextManagerSubscriberFacade {
    
    public NewContextManagerSubscriberFacade(final NewRegistryCenter registryCenter, final ContextManager contextManager) {
        new NewConfigurationChangedSubscriber(contextManager);
        new NewResourceMetaDataChangedSubscriber(contextManager);
        new DatabaseChangedSubscriber(contextManager);
        new NewStateChangedSubscriber(registryCenter, contextManager);
        new NewProcessListChangedSubscriber(registryCenter, contextManager);
        new CacheEvictedSubscriber(contextManager.getInstanceContext().getEventBusContext());
    }
}
