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

package io.shardingsphere.orchestration.internal.registry.listener;

import com.google.common.eventbus.EventBus;
import io.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import io.shardingsphere.orchestration.reg.listener.DataChangedEventListener;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;

/**
 * Post sharding orchestration event listener.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public abstract class PostShardingOrchestrationEventListener implements ShardingOrchestrationListener {
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private final RegistryCenter regCenter;
    
    private final String watchKey;
    
    @Override
    public final void watch(final ChangedType... watchedChangedTypes) {
        final Collection<ChangedType> watchedChangedTypeList = Arrays.asList(watchedChangedTypes);
        regCenter.watch(watchKey, new DataChangedEventListener() {
            
            @Override
            public void onChange(final DataChangedEvent dataChangedEvent) {
                if (watchedChangedTypeList.contains(dataChangedEvent.getChangedType())) {
                    eventBus.post(createShardingOrchestrationEvent(dataChangedEvent));
                }
            }
        });
    }
    
    protected abstract ShardingOrchestrationEvent createShardingOrchestrationEvent(DataChangedEvent event);
}
