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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.connection.ConnectionCloseEvent;
import io.shardingsphere.core.event.connection.GetConnectionEvent;
import io.shardingsphere.opentracing.ShardingTags;
import io.shardingsphere.opentracing.ShardingTracer;

/**
 * Connection close event listener.
 *
 * @author zhangyonglun
 */
public final class ConnectionCloseEventListener extends OpenTracingListener<ConnectionCloseEvent> {
    
    private static final String OPERATION_NAME_PREFIX = "/Sharding-Sphere/connectionClose/";
    
    private final ThreadLocal<Span> branchSpan = new ThreadLocal<>();

//    private final ThreadLocal<ActiveSpan> trunkInBranchSpan = new ThreadLocal<>();
    
    /**
     * Listen connectionClose event.
     *
     * @param event Connection close event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final ConnectionCloseEvent event) {
        tracing(event);
    }
    
    @Override
    protected void beforeExecute(final ConnectionCloseEvent event) {
//        if (ExecutorDataMap.getDataMap().containsKey(OverallExecuteEventListener.OVERALL_SPAN_CONTINUATION) && !OverallExecuteEventListener.isTrunkThread() && null == branchSpan.get()) {
//            trunkInBranchSpan.set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(OverallExecuteEventListener.OVERALL_SPAN_CONTINUATION)).activate());
//        }
//        trunkInBranchSpan.set(((ActiveSpan.Continuation) ExecutorDataMap.getDataMap().get(OverallExecuteEventListener.OVERALL_SPAN_CONTINUATION)).activate());
//        if (null == branchSpan.get()) {
        branchSpan.set(ShardingTracer.get().buildSpan(OPERATION_NAME_PREFIX).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).withTag(Tags.DB_INSTANCE.getKey(), event.getDataSource()).startManual());
//        }
    }
    
    @Override
    protected void tracingFinish() {
        if (null == branchSpan.get()) {
            return;
        }
        branchSpan.get().finish();
        branchSpan.remove();
//        if (null == trunkInBranchSpan.get()) {
//            return;
//        }
//        trunkInBranchSpan.get().deactivate();
//        trunkInBranchSpan.remove();
    }
    
    @Override
    protected Span getFailureSpan() {
        return branchSpan.get();
    }
}
