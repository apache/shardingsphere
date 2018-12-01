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

package io.shardingsphere.orchestration.internal.eventbus;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEventListener;
import lombok.RequiredArgsConstructor;

/**
 * Post orchestration event listener.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class PostOrchestrationEventListener implements DataChangedEventListener {
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    @Override
    public final void onChange(final DataChangedEvent event) {
        Optional<Object> newEvent = createEvent(event);
        if (newEvent.isPresent()) {
            eventBus.post(newEvent);
        }
    }
    
    protected abstract Optional<Object> createEvent(DataChangedEvent event);
}
