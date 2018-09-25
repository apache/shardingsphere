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

package io.shardingsphere.opentracing.handler.tracing;

import io.opentracing.Span;
import io.shardingsphere.opentracing.handler.root.OpenTracingRootInvokeHandler;
import lombok.RequiredArgsConstructor;

/**
 * Open tracing span finish root clean callback adapter.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class OpenTracingSpanFinishRootCleanCallbackAdapter implements OpenTracingSpanFinishCallback {
    
    private final boolean isTrunkThread;
    
    @Override
    public void updateSpan(final Span span) {
    }
    
    @Override
    public final void afterTracingFinish() {
        if (!isTrunkThread) {
            OpenTracingRootInvokeHandler.getActiveSpan().get().deactivate();
            OpenTracingRootInvokeHandler.getActiveSpan().remove();
        }
    }
}
