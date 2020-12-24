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

import io.jaegertracing.Configuration;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.core.config.JaegerPluginConfiguration;
import org.apache.shardingsphere.agent.core.constant.AgentConstant;
import org.apache.shardingsphere.agent.core.plugin.service.BootService;

/**
 * Jaeger tracing boot service.
 */
public final class JaegerTracingBootService implements BootService<JaegerPluginConfiguration> {
    
    @Override
    public void setup(final JaegerPluginConfiguration configuration) {
        configuration.getExtra().forEach(System::setProperty);
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv();
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                .withSender(
                        Configuration.SenderConfiguration.fromEnv()
                                .withAgentHost(configuration.getHost())
                                .withAgentPort(configuration.getPort())
                );
        Configuration config = new Configuration(configuration.getApplicationName())
                .withSampler(samplerConfig)
                .withReporter(reporterConfig);
        GlobalTracer.register(config.getTracer());
    }
    
    @Override
    public void cleanup() {
    }
    
    @Override
    public String getType() {
        return AgentConstant.PLUGIN_NAME_JAEGER;
    }
}
