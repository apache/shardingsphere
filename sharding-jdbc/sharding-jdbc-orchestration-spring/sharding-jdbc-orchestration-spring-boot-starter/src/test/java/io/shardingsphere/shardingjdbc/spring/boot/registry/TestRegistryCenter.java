/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.spring.boot.registry;

import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.reg.listener.EventListener;

import java.util.Collections;
import java.util.List;

/**
 * Test registry center.
 *
 * @author panjuan
 */
public final class TestRegistryCenter implements RegistryCenter {
    
    @Override
    public void init(final RegistryCenterConfiguration config) {
    }
    
    @Override
    public String get(final String key) {
        return "";
    }
    
    @Override
    public String getDirectly(final String key) {
        return "";
    }
    
    @Override
    public boolean isExisted(final String key) {
        return true;
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return Collections.emptyList();
    }
    
    @Override
    public void persist(final String key, final String value) {
    }
    
    @Override
    public void update(final String key, final String value) {
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
    }
    
    @Override
    public void watch(final String key, final EventListener eventListener) {
    }
    
    @Override
    public void close() {
    }
}
