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

package org.apache.shardingsphere.orchestration.internal.registry.fixture;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.orchestration.center.api.DistributedLockManagement;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public final class ForthTestRegistryCenter implements DistributedLockManagement {
    
    private final Map<String, String> keys = new HashMap<>();
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    private ReentrantLock lock = new ReentrantLock();
    
    @Override
    public void init(final InstanceConfiguration config) {
    }
    
    @Override
    public String get(final String key) {
        return keys.get(key);
    }
    
    @Override
    public void persist(final String key, final String value) {
        keys.put(key, value);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "ForthTestRegistryCenter";
    }
    
    @Override
    public void initLock(final String key) {
        lock = new ReentrantLock();
    }
    
    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }
    
    @Override
    public void tryRelease() {
        lock.unlock();
    }
}
