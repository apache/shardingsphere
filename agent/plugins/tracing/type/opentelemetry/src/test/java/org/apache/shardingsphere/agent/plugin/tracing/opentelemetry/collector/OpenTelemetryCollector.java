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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.collector;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.apache.shardingsphere.agent.plugin.tracing.rule.CollectorRule;
import org.junit.rules.ExternalResource;

import java.util.List;

public final class OpenTelemetryCollector extends ExternalResource implements CollectorRule {
    
    private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
    
    @Override
    protected void before() {
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(testExporter))
                .build();
        OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal()
                .getTracer("shardingsphere-agent");
    }
    
    /**
     * Get a List of the finished Spans, represented by SpanData.
     *
     * @return a List of the finished Spans.
     */
    public List<SpanData> getSpanItems() {
        return testExporter.getFinishedSpanItems();
    }
    
    @Override
    public void cleanup() {
        testExporter.reset();
    }
    
    @Override
    protected void after() {
        GlobalOpenTelemetry.resetForTest();
    }
}
