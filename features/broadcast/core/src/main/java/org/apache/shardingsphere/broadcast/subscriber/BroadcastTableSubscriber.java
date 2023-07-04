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

package org.apache.shardingsphere.broadcast.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.broadcast.event.table.AlterBroadcastTableEvent;
import org.apache.shardingsphere.broadcast.event.table.DropBroadcastTableEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;

/**
 * Broadcast table subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class BroadcastTableSubscriber implements RuleChangedSubscriber<AlterBroadcastTableEvent, DropBroadcastTableEvent> {
    
    private BroadcastTableSubscribeEngine engine;
    
    @Override
    public void setContextManager(final ContextManager contextManager) {
        engine = new BroadcastTableSubscribeEngine(contextManager);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterBroadcastTableEvent event) {
        engine.renew(event);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropBroadcastTableEvent event) {
        engine.renew(event);
    }
}
