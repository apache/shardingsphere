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
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.exception.PluginConfigurationException;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;

import java.util.Optional;

/**
 * Jaeger tracing plugin boot service.
 */
public final class JaegerTracingPluginBootService implements PluginBootService {
    
    private static final String DEFAULT_SERVICE_NAME = "shardingsphere";
    
    private static final String KEY_SERVICE_NAME = "service-name";
    
    private Configuration config;
    
    @SuppressWarnings("AccessOfSystemProperties")
    @Override
    public void start(final PluginConfiguration pluginConfig) {
        if (!checkConfiguration(pluginConfig)) {
            throw new PluginConfigurationException("jaeger config error, host is null or port is %s", pluginConfig.getPort());
        }
        pluginConfig.getProps().forEach((key, value) -> setSystemProperty(String.valueOf(key), String.valueOf(value)));
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv();
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                .withSender(Configuration.SenderConfiguration.fromEnv().withAgentHost(pluginConfig.getHost()).withAgentPort(pluginConfig.getPort()));
        String serviceName = Optional.ofNullable(pluginConfig.getProps().getProperty(KEY_SERVICE_NAME)).orElse(DEFAULT_SERVICE_NAME);
        config = new Configuration(serviceName).withSampler(samplerConfig).withReporter(reporterConfig);
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(config.getTracer());
        }
    }
    
    private void setSystemProperty(final String key, final String value) {
        if (!KEY_SERVICE_NAME.equalsIgnoreCase(key)) {
            String propertyKey = key.replaceAll("-", "_").toUpperCase();
            System.setProperty(propertyKey, String.valueOf(value));
        }
    }
    
    private boolean checkConfiguration(final PluginConfiguration pluginConfig) {
        String host = pluginConfig.getHost();
        int port = pluginConfig.getPort();
        return null != host && !"".equalsIgnoreCase(host) && port > 0;
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
