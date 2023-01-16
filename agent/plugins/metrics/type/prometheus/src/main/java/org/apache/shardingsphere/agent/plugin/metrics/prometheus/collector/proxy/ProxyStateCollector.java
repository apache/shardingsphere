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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.proxy;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.PrometheusWrapperFactory;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Proxy state collector.
 */
public final class ProxyStateCollector extends Collector {
    
    public static final String PROXY_STATE_METRIC_KEY = "proxy_state";
    
    private static final String PROXY_STATE = "state";
    
    private static final PrometheusWrapperFactory FACTORY = new PrometheusWrapperFactory();
    
    @Override
    public List<MetricFamilySamples> collect() {
        if (null == ProxyContext.getInstance().getContextManager()) {
            return Collections.emptyList();
        }
        GaugeMetricFamily proxyState = FACTORY.createGaugeMetricFamily(PROXY_STATE_METRIC_KEY);
        Optional<StateContext> stateContext = ProxyContext.getInstance().getStateContext();
        if (!stateContext.isPresent()) {
            return Collections.emptyList();
        }
        List<MetricFamilySamples> result = new LinkedList<>();
        // TODO use original ordinal to display state value, zero should be the 1st ordinal.
        proxyState.addMetric(Collections.singletonList(PROXY_STATE), stateContext.get().getCurrentState().ordinal() + 1);
        result.add(proxyState);
        return result;
    }
}
