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

package org.apache.shardingsphere.governance.core.registry.listener;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Governance listener.
 * 
 * @param <T> type of event
 */
@RequiredArgsConstructor
public abstract class GovernanceListener<T> {
    
    private final RegistryCenterRepository registryCenterRepository;
    
    private final Collection<String> watchKeys;
    
    /**
     * Start to watch.
     *
     * @param types watched data change types
     */
    public final void watch(final Type... types) {
        Collection<Type> typeList = Arrays.asList(types);
        for (String each : watchKeys) {
            watch(each, typeList);
        }
    }
    
    private void watch(final String watchKey, final Collection<Type> types) {
        registryCenterRepository.watch(watchKey, dataChangedEvent -> {
            if (types.contains(dataChangedEvent.getType())) {
                createEvent(dataChangedEvent).ifPresent(ShardingSphereEventBus.getInstance()::post);
            }
        });
    }
    
    protected abstract Optional<T> createEvent(DataChangedEvent event);
}
