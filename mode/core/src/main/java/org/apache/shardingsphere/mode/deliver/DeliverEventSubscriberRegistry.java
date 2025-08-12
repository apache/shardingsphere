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

package org.apache.shardingsphere.mode.deliver;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.event.EventBusContext;

import java.util.Collection;

/**
 * Cluster event subscriber registry.
 */
@RequiredArgsConstructor
public final class DeliverEventSubscriberRegistry {
    
    private final EventBusContext eventBusContext;
    
    /**
     * Register deliver event subscribers.
     *
     * @param subscribers to be registered subscribers
     */
    public void register(final Collection<DeliverEventSubscriber> subscribers) {
        subscribers.forEach(eventBusContext::register);
    }
}
