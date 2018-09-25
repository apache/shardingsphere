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

package io.shardingsphere.opentracing.handler;

import io.opentracing.ActiveSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.spi.root.RootInvokeHook;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.constant.ShardingTags;

/**
 * Open tracing root invoke handler.
 *
 * @author zhangliang
 */
public final class OpenTracingRootInvokeHandler implements RootInvokeHook {
    
    public static final String ROOT_SPAN_CONTINUATION = "ROOT_SPAN_CONTINUATION";
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/rootInvoke/";
    
    private static final ThreadLocal<ActiveSpan> ACTIVE_SPAN = new ThreadLocal<>();
    
    @Override
    public void start() {
        ACTIVE_SPAN.set(ShardingTracer.get().buildSpan(OPERATION_NAME).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).startActive());
        ExecutorDataMap.getDataMap().put(ROOT_SPAN_CONTINUATION, ACTIVE_SPAN.get().capture());
    }
    
    @Override
    public void finish(final int connectionCount) {
        ACTIVE_SPAN.get().setTag(ShardingTags.CONNECTION_COUNT.getKey(), connectionCount).deactivate();
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
