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
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.util.MetricsUtil;
import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy info collector.
 */
public final class ProxyInfoCollector extends Collector {
    
    private static final String PROXY_STATE = "state";
    
    private static final String PROXY_CLASS_STR = "org.apache.shardingsphere.proxy.backend.context.ProxyContext";
    
    private static final PrometheusWrapperFactory FACTORY = new PrometheusWrapperFactory();
    
    private static final ConcurrentHashMap<String, Integer> PROXY_STATE_MAP = new ConcurrentHashMap<String, Integer>();
    
    static {
        PROXY_STATE_MAP.put("OK", 1);
        PROXY_STATE_MAP.put("CIRCUIT_BREAK", 2);
    }
    
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new LinkedList<>();
        if (MetricsUtil.classNotExist(PROXY_CLASS_STR)) {
            return result;
        }
        Optional<GaugeMetricFamily> proxyInfo = FACTORY.createGaugeMetricFamily(MetricIds.PROXY_INFO);
        String currentState = ProxyContext.getInstance().getStateContext().getCurrentState();
        proxyInfo.ifPresent(m -> 
                m.addMetric(Collections.singletonList(PROXY_STATE), PROXY_STATE_MAP.get(currentState)));
        proxyInfo.ifPresent(result::add);
        return result;
    }
}
