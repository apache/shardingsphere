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

package org.apache.shardingsphere.infra.util.eventbus;

import com.google.common.eventbus.EventBus;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Event bus context.
 */
@SuppressWarnings("UnstableApiUsage")
public final class EventBusContext {
    
    private final EventBus eventBus = new EventBus();
    
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    
    /**
     * Register object.
     * 
     * @param object object
     */
    public void register(final Object object) {
        eventBus.register(object);
    }
    
    /**
     * Post event.
     * 
     * @param event event
     */
    public void post(final Object event) {
        eventBus.post(event);
    }
}
