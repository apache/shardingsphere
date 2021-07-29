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
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Proxy info collector.
 */
public final class ProxyInfoCollector extends Collector {
    
    private static final String PROXY_STATE = "state";
    
    private static final String PROXY_UP_TIME = "uptime";
    
    @Override
    public List<Collector.MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new LinkedList<>();
        if (!checkProxy()) {
            return result;
        }
        GaugeMetricFamily proxyInfo = new GaugeMetricFamily(
                "proxy_info",
                "Proxy information.",
                Arrays.asList("name"));
        proxyInfo.addMetric(Arrays.asList(PROXY_STATE), ProxyContext.getInstance().getStateContext().getCurrentState().ordinal());
        Date now = new Date();
        if (0 >= ShardingSphereProxy.getStartTime()) {
            proxyInfo.addMetric(Arrays.asList(PROXY_UP_TIME), 0);
        } else {
            proxyInfo.addMetric(Arrays.asList(PROXY_UP_TIME), now.getTime() - ShardingSphereProxy.getStartTime());
        }
        result.add(proxyInfo);
        return result;
    }
    
    private boolean checkProxy() {
        try {
            Class.forName("org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy");
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }
}
