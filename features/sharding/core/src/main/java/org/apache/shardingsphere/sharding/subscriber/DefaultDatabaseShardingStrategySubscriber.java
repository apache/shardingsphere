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

package org.apache.shardingsphere.sharding.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.DropDefaultDatabaseShardingStrategyEvent;

/**
 * Default database sharding strategy subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DefaultDatabaseShardingStrategySubscriber implements RuleChangedSubscriber<AlterDefaultDatabaseShardingStrategyEvent, DropDefaultDatabaseShardingStrategyEvent> {
    
    private DefaultDatabaseShardingStrategySubscribeEngine engine;
    
    @Override
    public void setContextManager(final ContextManager contextManager) {
        engine = new DefaultDatabaseShardingStrategySubscribeEngine(contextManager);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterDefaultDatabaseShardingStrategyEvent event) {
        engine.renew(event);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropDefaultDatabaseShardingStrategyEvent event) {
        engine.renew(event);
    }
}
