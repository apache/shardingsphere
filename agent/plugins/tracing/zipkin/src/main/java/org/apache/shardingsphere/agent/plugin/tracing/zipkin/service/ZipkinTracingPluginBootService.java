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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.service;

import brave.Tracing;
import brave.sampler.BoundarySampler;
import brave.sampler.RateLimitingSampler;
import brave.sampler.Sampler;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.spi.boot.PluginBootService;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.Optional;
import java.util.Properties;

/**
 * Zipkin tracing plugin boot service.
 */
public final class ZipkinTracingPluginBootService implements PluginBootService {
    
    private static final String DEFAULT_SERVICE_NAME = "shardingsphere";
    
    private static final String KEY_SERVICE_NAME = "service-name";
    
    private static final String DEFAULT_URL_VERSION = "/api/v2/spans";
    
    private static final String KEY_URL_VERSION = "url-version";
    
    private static final String DEFAULT_SAMPLER_TYPE = "const";
    
    private static final String KEY_SAMPLER_TYPE = "sampler-type";
    
    private static final String DEFAULT_SAMPLER_PARAM = "1";
    
    private static final String KEY_SAMPLER_PARAM = "sampler-param";
    
    private AsyncZipkinSpanHandler zipkinSpanHandler;
    
    private OkHttpSender sender;
    
    private Tracing tracing;
    
    @Override
    public void start(final PluginConfiguration pluginConfig) {
        pluginConfig.validate("Zipkin");
        Properties props = pluginConfig.getProps();
        String urlVersion = Optional.ofNullable(props.getProperty(KEY_URL_VERSION)).orElse(DEFAULT_URL_VERSION);
        String serviceName = Optional.ofNullable(props.getProperty(KEY_SERVICE_NAME)).orElse(DEFAULT_SERVICE_NAME);
        sender = OkHttpSender.create(String.format("http://%s:%s%s", pluginConfig.getHost(), pluginConfig.getPort(), urlVersion));
        Sampler sampler = createSampler(pluginConfig);
        zipkinSpanHandler = AsyncZipkinSpanHandler.create(sender);
        tracing = Tracing.newBuilder().localServiceName(serviceName).sampler(sampler).addSpanHandler(zipkinSpanHandler).build();
    }
    
    private Sampler createSampler(final PluginConfiguration pluginConfig) {
        String samplerType = Optional.ofNullable(pluginConfig.getProps().getProperty(KEY_SAMPLER_TYPE)).orElse(DEFAULT_SAMPLER_TYPE);
        String samplerParam = Optional.ofNullable(pluginConfig.getProps().getProperty(KEY_SAMPLER_PARAM)).orElse(DEFAULT_SAMPLER_PARAM);
        switch (samplerType) {
            case "const":
                return "0".equals(samplerParam) ? Sampler.NEVER_SAMPLE : Sampler.ALWAYS_SAMPLE;
            case "counting":
                return Sampler.create(Float.parseFloat(samplerParam));
            case "ratelimiting":
                return RateLimitingSampler.create(Integer.parseInt(samplerParam));
            case "boundary":
                return BoundarySampler.create(Float.parseFloat(samplerParam));
            default:
                return Sampler.ALWAYS_SAMPLE;
        }
    }
    
    @Override
    public void close() {
        if (null != tracing) {
            tracing.close();
        }
        if (null != zipkinSpanHandler) {
            zipkinSpanHandler.close();
        }
        if (null != sender) {
            sender.close();
        }
    }
    
    @Override
    public String getType() {
        return "Zipkin";
    }
}
