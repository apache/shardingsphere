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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.NodePath;

import java.util.Collection;

/**
 * Global data changed event handler.
 */
@SingletonSPI
public interface GlobalDataChangedEventHandler extends ShardingSphereSPI {
    
    /**
     * Get subscribed node path.
     *
     * @return subscribed node path
     */
    NodePath getSubscribedNodePath();
    
    /**
     * Get subscribed types.
     *
     * @return subscribed types
     */
    Collection<Type> getSubscribedTypes();
    
    /**
     * Handle data changed event.
     *
     * @param contextManager context manager
     * @param event data changed event
     */
    void handle(ContextManager contextManager, DataChangedEvent event);
}
