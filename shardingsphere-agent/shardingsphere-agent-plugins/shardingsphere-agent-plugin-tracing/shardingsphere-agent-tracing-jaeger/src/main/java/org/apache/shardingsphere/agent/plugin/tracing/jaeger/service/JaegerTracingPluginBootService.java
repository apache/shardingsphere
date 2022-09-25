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

package org.apache.shardingsphere.agent.plugin.tracing.jaeger.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.jaegertracing.Configuration;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

import java.util.Optional;

/**
 * Jaeger tracing plugin boot service.
 */
public final class JaegerTracingPluginBootService implements PluginBootService {
    
    private Configuration config;
    
    @SuppressWarnings("AccessOfSystemProperties")
    @Override
    public void start(final PluginConfiguration pluginConfig) {
        pluginConfig.validate("Jaeger");
        pluginConfig.getProps().forEach((key, value) -> System.setProperty(String.valueOf(key), String.valueOf(value)));
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv();
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                .withSender(Configuration.SenderConfiguration.fromEnv().withAgentHost(pluginConfig.getHost()).withAgentPort(pluginConfig.getPort()));
        String serviceName = Optional.ofNullable(pluginConfig.getProps().getProperty("SERVICE_NAME")).orElse("shardingsphere-agent");
        config = new Configuration(serviceName).withSampler(samplerConfig).withReporter(reporterConfig);
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(config.getTracer());
        }
    }
    
    private void checkConfiguration(final PluginConfiguration pluginConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pluginConfig.getHost()), "Jaeger hostname is required");
        Preconditions.checkArgument(pluginConfig.getPort() > 0, "Jaeger port `%d` must be a positive number");
    }
    
    @Override
    public void close() {
        if (null != config) {
            config.closeTracer();
        }
    }
    
    @Override
    public String getType() {
        return "Jaeger";
    }
}
