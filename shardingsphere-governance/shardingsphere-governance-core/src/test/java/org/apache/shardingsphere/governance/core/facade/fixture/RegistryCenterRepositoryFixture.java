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

package org.apache.shardingsphere.governance.core.facade.fixture;

import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class RegistryCenterRepositoryFixture implements RegistryCenterRepository {
    
    @Override
    public void init(final String name, final RegistryCenterConfiguration config) {
    }
    
    @Override
    public String get(final String key) {
        return "";
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return Collections.emptyList();
    }
    
    @Override
    public void persist(final String key, final String value) {
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
    }
    
    @Override
    public void delete(final String key) {
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
    }
    
    @Override
    public boolean tryLock(final String key, final long time, final TimeUnit unit) {
        return false;
    }
    
    @Override
    public void releaseLock(final String key) {
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "TEST";
    }
}
