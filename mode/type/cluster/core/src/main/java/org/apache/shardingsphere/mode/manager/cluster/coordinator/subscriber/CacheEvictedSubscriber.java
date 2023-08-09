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

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;

/**
 * Cache evicted subscriber.
 */
public final class CacheEvictedSubscriber {
    
    public CacheEvictedSubscriber(final EventBusContext eventBusContext) {
        eventBusContext.register(this);
    }
    
    /**
     * Callback of any {@link GovernanceEvent}.
     *
     * @param ignored unused
     */
    @SuppressWarnings({"UnstableApiUsage", "unused"})
    @Subscribe
    public void onGovernanceEvent(final GovernanceEvent ignored) {
        OrderedServicesCache.clearCache();
    }
}
