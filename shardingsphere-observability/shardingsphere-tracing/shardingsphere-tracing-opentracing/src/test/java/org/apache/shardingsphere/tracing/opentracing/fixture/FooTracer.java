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

package org.apache.shardingsphere.tracing.opentracing.fixture;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public final class FooTracer implements Tracer {
    
    @Override
    public SpanBuilder buildSpan(final String operationName) {
        return null;
    }
    
    @Override
    public <C> void inject(final SpanContext spanContext, final Format<C> format, final C carrier) {
    }
    
    @Override
    public <C> SpanContext extract(final Format<C> format, final C carrier) {
        return null;
    }
    
    @Override
    public ActiveSpan activeSpan() {
        return null;
    }
    
    @Override
    public ActiveSpan makeActive(final Span span) {
        return null;
    }
}
