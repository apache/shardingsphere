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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.event.dispatch.assisted.DropDatabaseListenerAssistedEvent;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CacheEvictedSubscriberTest {
    
    @Test
    void assertClearCache() {
        EventBusContext eventBusContext = new EventBusContext();
        eventBusContext.register(new CacheEvictedSubscriber());
        OrderedServicesCache.cacheServices(getClass(), Collections.emptyList(), Collections.emptyMap());
        eventBusContext.post(new DropDatabaseListenerAssistedEvent("db"));
        assertFalse(OrderedServicesCache.findCachedServices(getClass(), Collections.emptyList()).isPresent());
    }
}
