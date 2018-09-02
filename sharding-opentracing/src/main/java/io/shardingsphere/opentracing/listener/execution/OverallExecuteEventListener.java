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

package io.shardingsphere.opentracing.listener.execution;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.ActiveSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.executor.overall.OverallExecutionEvent;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.listener.OpenTracingListener;

/**
 * SQL execute overall event listener.
 * 
 * @author gaohongtao
 * @author wangkai
 * @author maxiaoguang
 */
public final class OverallExecuteEventListener extends OpenTracingListener<OverallExecutionEvent> {
    
    public static final String OVERALL_SPAN_CONTINUATION = "OVERALL_SPAN_CONTINUATION";
    
    private static final String OPERATION_NAME_PREFIX = "/Sharding-Sphere/execute/";
    
    private static final ThreadLocal<ActiveSpan> SPAN = new ThreadLocal<>();
    
    /**
     * Listen overall sql execution event.
     *
     * @param event overall sql execution event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final OverallExecutionEvent event) {
        tracing(event);
    }
    
    @Override
    protected void beforeExecute(final OverallExecutionEvent event) {
        ActiveSpan activeSpan = ShardingTracer.get().buildSpan(OPERATION_NAME_PREFIX).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).startActive();
        SPAN.set(activeSpan);
        if (event.isParallelExecute()) {
            ExecutorDataMap.getDataMap().put(OVERALL_SPAN_CONTINUATION, activeSpan.capture());
        }
    }
    
    @Override
    protected void tracingFinish() {
        SPAN.get().deactivate();
        SPAN.remove();
    }
    
    @Override
    protected ActiveSpan getFailureSpan() {
        return SPAN.get();
    }
    
    /**
     * Tests if sql execute event in this overall event thread.
     * 
     * @return sql execute event in this overall event thread or not.
     */
    public static boolean isTrunkThread() {
        return null != SPAN.get();
    }

}
