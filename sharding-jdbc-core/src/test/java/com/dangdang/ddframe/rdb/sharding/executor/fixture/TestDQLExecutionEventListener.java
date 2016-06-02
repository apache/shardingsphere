/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.executor.fixture;

import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEventListener;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class TestDQLExecutionEventListener implements DQLExecutionEventListener {
    
    private final EventCaller eventCaller;
    
    @Override
    public String getName() {
        return "test_listener" + System.nanoTime();
    }
    
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final DQLExecutionEvent event) {
        eventCaller.verifyDataSource(event.getDataSource());
        eventCaller.verifySQL(event.getSql());
        eventCaller.verifyParameters(event.getParameters());
        eventCaller.verifyEventExecutionType(event.getEventExecutionType());
        if (EventExecutionType.EXECUTE_FAILURE.equals(event.getEventExecutionType()) && event.getExp().isPresent()) {
            eventCaller.verifyException(event.getExp().get());
        }
    }
}
