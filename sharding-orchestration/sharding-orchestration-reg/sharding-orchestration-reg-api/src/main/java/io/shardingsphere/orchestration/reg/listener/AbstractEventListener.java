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

package io.shardingsphere.orchestration.reg.listener;

import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.Type;
import lombok.RequiredArgsConstructor;

/**
 * Abstract event listener.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractEventListener implements EventListener {
    
    private final boolean isFireOnUpdate;
    
    private final boolean isFireOnDelete;
    
    @Override
    public final void onChange(final DataChangedEvent event) {
        if (isFireWhenUpdate(event.getEventType()) || isFireWhenDelete(event.getEventType())) {
            Object newEvent = createEvent(event);
            if (null != newEvent) {
                ShardingEventBusInstance.getInstance().post(newEvent);
            }
        }
    }
    
    private boolean isFireWhenUpdate(final Type type) {
        return isFireOnUpdate && Type.UPDATED == type;
    }
    
    private boolean isFireWhenDelete(final Type type) {
        return isFireOnDelete && Type.DELETED == type;
    }
    
    protected abstract Object createEvent(DataChangedEvent event);
}
