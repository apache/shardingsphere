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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber;

import lombok.Getter;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type.CacheEvictedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type.MetaDataChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type.RuleItemChangedSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type.StorageUnitEventSubscriber;

import java.util.Arrays;
import java.util.Collection;

/**
 * Cluster dispatch event subscriber registry.
 */
@Getter
public final class ClusterDispatchEventSubscriberRegistry {
    
    private final Collection<EventSubscriber> subscribers;
    
    public ClusterDispatchEventSubscriberRegistry(final ContextManager contextManager) {
        subscribers = Arrays.asList(new RuleItemChangedSubscriber(contextManager),
                new MetaDataChangedSubscriber(contextManager),
                new CacheEvictedSubscriber(),
                new StorageUnitEventSubscriber(contextManager));
    }
}
