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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber.ProcessListChangedSubscriber;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.subsciber.EventSubscriberRegistry;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscriber;

/**
 * Cluster event subscriber registry.
 */
public final class ClusterEventSubscriberRegistry extends EventSubscriberRegistry {
    
    public ClusterEventSubscriberRegistry(final ContextManager contextManager, final ClusterPersistRepository repository) {
        super(contextManager,
                new RuleItemChangedSubscriber(contextManager),
                new ConfigurationChangedSubscriber(contextManager),
                new ConfigurationChangedSubscriber(contextManager),
                new ResourceMetaDataChangedSubscriber(contextManager),
                new StateChangedSubscriber(contextManager),
                new DatabaseChangedSubscriber(contextManager),
                new ProcessListChangedSubscriber(contextManager, repository),
                new CacheEvictedSubscriber());
    }
}
