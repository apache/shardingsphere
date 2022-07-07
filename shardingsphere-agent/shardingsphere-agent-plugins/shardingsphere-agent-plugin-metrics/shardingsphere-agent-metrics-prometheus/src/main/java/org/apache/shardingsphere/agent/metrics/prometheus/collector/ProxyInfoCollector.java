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
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy information collector.
 */
public final class ProxyInfoCollector extends Collector {
    
    private static final String PROXY_STATE = "state";
    
    private static final String PROXY_CLASS = "org.apache.shardingsphere.proxy.backend.context.ProxyContext";
    
    private static final PrometheusWrapperFactory FACTORY = new PrometheusWrapperFactory();
    
    private static final ConcurrentHashMap<StateType, Integer> PROXY_STATE_MAP = new ConcurrentHashMap<>();
    
    static {
        PROXY_STATE_MAP.put(StateType.OK, 1);
        PROXY_STATE_MAP.put(StateType.CIRCUIT_BREAK, 2);
    }
    
    @Override
    public List<MetricFamilySamples> collect() {
        if (!MetricsUtil.isClassExisted(PROXY_CLASS) || null == ProxyContext.getInstance().getContextManager()) {
            return Collections.emptyList();
        }
        Optional<GaugeMetricFamily> proxyInfo = FACTORY.createGaugeMetricFamily(MetricIds.PROXY_INFO);
        Optional<StateContext> stateContext = ProxyContext.getInstance().getStateContext();
        if (!proxyInfo.isPresent() || !stateContext.isPresent()) {
            return Collections.emptyList();
        }
        List<MetricFamilySamples> result = new LinkedList<>();
        proxyInfo.get().addMetric(Collections.singletonList(PROXY_STATE), PROXY_STATE_MAP.get(stateContext.get().getCurrentState()));
        result.add(proxyInfo.get());
        return result;
    }
}
