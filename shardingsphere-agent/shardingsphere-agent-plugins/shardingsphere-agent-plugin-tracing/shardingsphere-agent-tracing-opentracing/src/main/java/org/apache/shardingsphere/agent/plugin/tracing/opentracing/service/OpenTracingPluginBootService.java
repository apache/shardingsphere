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

package org.apache.shardingsphere.agent.plugin.tracing.opentracing.service;

import com.google.common.base.Preconditions;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

/**
 * Open tracing plugin boot service.
 */
public final class OpenTracingPluginBootService implements PluginBootService {
    
    private static final String KEY_OPENTRACING_TRACER_CLASS_NAME = "opentracing-tracer-class-name";
    
    @Override
    public void start(final PluginConfiguration pluginConfig) {
        String tracerClassName = pluginConfig.getProps().getProperty(KEY_OPENTRACING_TRACER_CLASS_NAME);
        Preconditions.checkNotNull(tracerClassName, "Can not find opentracing tracer implementation in you config");
        try {
            init((Tracer) Class.forName(tracerClassName).getDeclaredConstructor().newInstance());
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException("Initialize opentracing tracer class failure", ex);
        }
    }
    
    private void init(final Tracer tracer) {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(tracer);
        }
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "OpenTracing";
    }
}
