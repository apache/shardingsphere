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
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.executor.event.OverallExecutionEvent;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.listener.TracingListener;
import io.shardingsphere.opentracing.tag.LocalTags;

/**
 * SQL execute overall event listener.
 * 
 * @author gaohongtao
 * @author wangkai
 * @author maxiaoguang
 */
public final class OverallExecuteEventListener extends TracingListener<OverallExecutionEvent> {
    
    private static final String SNAPSHOT_DATA_KEY = "OPENTRACING_SNAPSHOT_DATA";

    private static final String OPERATION_NAME_PREFIX = "/SHARDING-SPHERE/EXECUTE/";

    private final ThreadLocal<ActiveSpan> trunkContainer = new ThreadLocal<>();
    
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
        Tracer tracer = ShardingTracer.get();
        ActiveSpan activeSpan = tracer.buildSpan(OPERATION_NAME_PREFIX + event.getSqlType().name()).withTag(Tags.COMPONENT.getKey(), LocalTags.COMPONENT_NAME).startActive();
        trunkContainer.set(activeSpan);
        if (event.isParallelExecute()) {
            ExecutorDataMap.getDataMap().put(SNAPSHOT_DATA_KEY, activeSpan.capture());
        }
    }
    
    @Override
    protected void tracingFinish() {
        trunkContainer.get().deactivate();
        trunkContainer.remove();
    }
    
    @Override
    protected void tracingFailure(final OverallExecutionEvent event) {
        ActiveSpan activeSpan = trunkContainer.get();
        activeSpan.setTag(Tags.ERROR.getKey(), true);
        if (event.getException().isPresent()) {
            activeSpan.log(System.currentTimeMillis(), log(event.getException().get()));
        }
    }
}
