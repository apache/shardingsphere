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

package io.shardingsphere.opentracing.listener.root;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.ActiveSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.root.RootInvokeFinishEvent;
import io.shardingsphere.core.event.root.RootInvokeStartEvent;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;

/**
 * Root invoke event listener.
 *
 * @author gaohongtao
 * @author wangkai
 * @author maxiaoguang
 */
public final class RootInvokeEventListener {
    
    public static final String OVERALL_SPAN_CONTINUATION = "OVERALL_SPAN_CONTINUATION";
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/rootInvoke/";
    
    private static final ThreadLocal<ActiveSpan> ACTIVE_SPAN = new ThreadLocal<>();
    
    /**
     * Register listener.
     */
    public void register() {
        ShardingEventBusInstance.getInstance().register(this);
    }
    
    /**
     * Listen root invoke start event.
     *
     * @param event root invoke start event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final RootInvokeStartEvent event) {
        ActiveSpan span = ShardingTracer.get().buildSpan(OPERATION_NAME).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).startActive();
        ACTIVE_SPAN.set(span);
        ExecutorDataMap.getDataMap().put(OVERALL_SPAN_CONTINUATION, span.capture());
    }
    
    /**
     * Listen root invoke finish event.
     *
     * @param event root invoke finish event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final RootInvokeFinishEvent event) {
        ACTIVE_SPAN.get().deactivate();
        ACTIVE_SPAN.remove();
    }
    
    /**
     * Tests if sql execute event in this overall event thread.
     *
     * @return sql execute event in this overall event thread or not.
     */
    public static boolean isTrunkThread() {
        return null != ACTIVE_SPAN.get();
    }
    
    /**
     * Get active span.
     * 
     * @return active span
     */
    public static ThreadLocal<ActiveSpan> getActiveSpan() {
        return ACTIVE_SPAN;
    }
}
