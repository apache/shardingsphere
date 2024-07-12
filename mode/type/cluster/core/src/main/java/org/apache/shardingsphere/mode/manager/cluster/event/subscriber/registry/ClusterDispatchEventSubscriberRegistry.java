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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.registry;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.event.subsciber.EventSubscriberRegistry;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.CacheEvictedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.ComputeNodeOnlineSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.DatabaseChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.GlobalRuleConfigurationEventSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.ListenerAssistedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.ProcessListChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.PropertiesEventSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.QualifiedDataSourceSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.ResourceMetaDataChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.RuleItemChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.StateChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch.StorageUnitEventSubscriber;

import java.util.Arrays;
import java.util.Collection;

/**
 * Cluster dispatch event subscriber registry.
 */
public class ClusterDispatchEventSubscriberRegistry implements EventSubscriberRegistry {
    
    private final EventBusContext eventBusContext;
    
    private final Collection<EventSubscriber> subscribers;
    
    public ClusterDispatchEventSubscriberRegistry(final ContextManager contextManager) {
        eventBusContext = contextManager.getComputeNodeInstanceContext().getEventBusContext();
        subscribers = Arrays.asList(new RuleItemChangedSubscriber(contextManager),
                new ResourceMetaDataChangedSubscriber(contextManager),
                new ListenerAssistedSubscriber(contextManager),
                new StateChangedSubscriber(contextManager),
                new DatabaseChangedSubscriber(contextManager),
                new ProcessListChangedSubscriber(contextManager),
                new CacheEvictedSubscriber(),
                new ComputeNodeOnlineSubscriber(contextManager),
                new QualifiedDataSourceSubscriber(contextManager),
                new StorageUnitEventSubscriber(contextManager),
                new GlobalRuleConfigurationEventSubscriber(contextManager),
                new PropertiesEventSubscriber(contextManager));
    }
    
    @Override
    public void register() {
        // TODO use call subscruber instead of event bus
        subscribers.forEach(eventBusContext::register);
    }
}
