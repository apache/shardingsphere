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

package org.apache.shardingsphere.agent.metrics.prometheus.collector;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Build info collector.
 */
@Slf4j
public final class BuildInfoCollector extends Collector {
    
    private static final String PROXY_BOOTSTRAP_CLASS_STR = "org.apache.shardingsphere.proxy.Bootstrap";
    
    private static final PrometheusWrapperFactory FACTORY = new PrometheusWrapperFactory();
    
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new LinkedList<>();
        Optional<GaugeMetricFamily> artifactInfo = FACTORY.createGaugeMetricFamily(MetricIds.BUILD_INFO);
        Package pluginPkg = getClass().getPackage();
        final String pluginVersion = pluginPkg.getImplementationVersion();
        final String pluginName = pluginPkg.getImplementationTitle();
        artifactInfo.ifPresent(m -> 
                m.addMetric(Arrays.asList(null != pluginVersion ? pluginVersion : "unknown", null != pluginName ? pluginName : "unknown"), 1L));
        try {
            Package proxyPkg = Class.forName(PROXY_BOOTSTRAP_CLASS_STR).getPackage();
            final String proxyVersion = proxyPkg.getImplementationVersion();
            final String proxyName = proxyPkg.getImplementationTitle();
            artifactInfo.ifPresent(m -> 
                    m.addMetric(Arrays.asList(null != proxyVersion ? proxyVersion : "unknown", null != proxyName ? proxyName : "unknown"), 1L));
        } catch (ClassNotFoundException ex) {
            log.warn("No proxy class find");
        }
        artifactInfo.ifPresent(result::add);
        return result;
    }
}
