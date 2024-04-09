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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.plugin.core.context.PluginContext;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.agent.spi.PluginLifecycleService;

/**
 * Open telemetry tracing plugin lifecycle service.
 */
public final class OpenTelemetryTracingPluginLifecycleService implements PluginLifecycleService {
    
    @Override
    public void start(final PluginConfiguration pluginConfig, final boolean isEnhancedForProxy) {
        PluginContext.getInstance().setEnhancedForProxy(isEnhancedForProxy);
        pluginConfig.getProps().forEach((key, value) -> setSystemProperty(String.valueOf(key), String.valueOf(value)));
        OpenTelemetrySdk openTelemetrySdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
        openTelemetrySdk.getTracer(OpenTelemetryConstants.TRACER_NAME);
    }
    
    private void setSystemProperty(final String key, final String value) {
        String propertyKey = key.replaceAll("-", ".");
        System.setProperty(propertyKey, String.valueOf(value));
    }
    
    @Override
    public String getType() {
        return "OpenTelemetry";
    }
}
