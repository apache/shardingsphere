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

package org.apache.shardingsphere.agent.plugin.tracing.rule;

import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import lombok.SneakyThrows;
import org.junit.rules.ExternalResource;
import zipkin2.Span;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ZipkinCollector extends ExternalResource implements CollectorRule {
    
    private static final ConcurrentLinkedDeque<Span> SPANS = new ConcurrentLinkedDeque<>();
    
    @Override
    @SneakyThrows
    protected void before() {
        Tracing.newBuilder()
                .currentTraceContext(StrictCurrentTraceContext.create())
                .spanReporter(SPANS::add)
                .build();
    }
    
    @Override
    @SneakyThrows
    protected void after() {
        Tracing.current().close();
    }
    
    /**
     * Get the first Span.
     *
     * @return span
     */
    public Span pop() {
        return SPANS.pollFirst();
    }
    
    @Override
    public void cleanup() {
        SPANS.clear();
    }
}
