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

package io.shardingsphere.opentracing.listener;

import com.google.common.eventbus.EventBus;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.root.RootInvokeFinishEvent;
import io.shardingsphere.core.event.root.RootInvokeStartEvent;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class RootInvokeEventListenerTest extends BaseEventListenerTest {
    
    private final EventBus shardingEventBus = ShardingEventBusInstance.getInstance();
    
    @Test
    public void assertRootInvoke() {
        shardingEventBus.post(new RootInvokeStartEvent());
        assertTrue(RootInvokeEventListener.isTrunkThread());
        assertNotNull(RootInvokeEventListener.getActiveSpan().get());
        assertTrue(ExecutorDataMap.getDataMap().containsKey(RootInvokeEventListener.OVERALL_SPAN_CONTINUATION));
        shardingEventBus.post(new RootInvokeFinishEvent());
        assertFalse(RootInvokeEventListener.isTrunkThread());
        assertNull(RootInvokeEventListener.getActiveSpan().get());
    }
}
