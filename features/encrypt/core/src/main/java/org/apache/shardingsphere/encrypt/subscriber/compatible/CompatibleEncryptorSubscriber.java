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

package org.apache.shardingsphere.encrypt.subscriber.compatible;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.encrypt.event.compatible.encryptor.AlterCompatibleEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.compatible.encryptor.DropCompatibleEncryptorEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;

/**
 * Compatible encryptor subscriber.
 * @deprecated compatible support will remove in next version.
 */
@Deprecated
@SuppressWarnings("UnstableApiUsage")
public final class CompatibleEncryptorSubscriber implements RuleChangedSubscriber<AlterCompatibleEncryptorEvent, DropCompatibleEncryptorEvent> {
    
    private CompatibleEncryptorSubscribeEngine engine;
    
    @Override
    public void setContextManager(final ContextManager contextManager) {
        engine = new CompatibleEncryptorSubscribeEngine(contextManager);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterCompatibleEncryptorEvent event) {
        engine.renew(event);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropCompatibleEncryptorEvent event) {
        engine.renew(event);
    }
}
