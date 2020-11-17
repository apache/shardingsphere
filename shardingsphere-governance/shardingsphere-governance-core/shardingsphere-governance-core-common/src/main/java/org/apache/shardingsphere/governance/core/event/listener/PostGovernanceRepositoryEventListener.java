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

package org.apache.shardingsphere.governance.core.event.listener;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.repository.api.GovernanceRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Post governance repository event listener.
 */
@RequiredArgsConstructor
public abstract class PostGovernanceRepositoryEventListener implements GovernanceListener {
    
    private final GovernanceRepository governanceRepository;
    
    private final Collection<String> watchKeys;
    
    @Override
    public final void watch(final Type... types) {
        Collection<Type> typeList = Arrays.asList(types);
        for (String watchKey : watchKeys) {
            watch(watchKey, typeList);
        }
    }
    
    private void watch(final String watchKey, final Collection<Type> types) {
        governanceRepository.watch(watchKey, dataChangedEvent -> {
            if (types.contains(dataChangedEvent.getType())) {
                Optional<Object> event = createEvent(dataChangedEvent);
                event.ifPresent(GovernanceEventBus.getInstance()::post);
            }
        });
    }
    
    protected abstract Optional<Object> createEvent(DataChangedEvent event);
}
