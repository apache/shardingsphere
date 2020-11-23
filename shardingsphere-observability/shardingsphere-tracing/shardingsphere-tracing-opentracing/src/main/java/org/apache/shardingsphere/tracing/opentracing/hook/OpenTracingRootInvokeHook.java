/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.tracing.opentracing.hook;

import io.opentracing.ActiveSpan;
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.hook.RootInvokeHook;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.tracing.opentracing.constant.ShardingTags;

/**
 * Open tracing root invoke hook.
 */
public final class OpenTracingRootInvokeHook implements RootInvokeHook {
    
    public static final String ACTIVE_SPAN_CONTINUATION = "ACTIVE_SPAN_CONTINUATION";
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/rootInvoke/";
    
    private ActiveSpan activeSpan;
    
    @Override
    public void start() {
        activeSpan = OpenTracingTracer.get().buildSpan(OPERATION_NAME).withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME).startActive();
        ExecutorDataMap.getValue().put(ACTIVE_SPAN_CONTINUATION, activeSpan.capture());
    }
    
    @Override
    public void finish(final int connectionCount) {
        activeSpan.setTag(ShardingTags.CONNECTION_COUNT.getKey(), connectionCount).deactivate();
    }
}
