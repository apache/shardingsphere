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

package org.apache.shardingsphere.agent.metrics.prometheus.register;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import org.apache.shardingsphere.agent.metrics.prometheus.util.ReflectiveUtil;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PrometheusMetricsRegisterTest {
    
    private final PrometheusMetricsRegister prometheusMetricsRegister = PrometheusMetricsRegister.getInstance();
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertCounter() {
        String routeDatasource = "route_datasource";
        String[] labelNames = {"name"};
        prometheusMetricsRegister.registerCounter(routeDatasource, labelNames, "the shardingsphere proxy route datasource");
        prometheusMetricsRegister.counterIncrement(routeDatasource, labelNames);
        prometheusMetricsRegister.counterIncrement(routeDatasource, labelNames, 2);
        String routeTable = "route_table";
        prometheusMetricsRegister.registerCounter(routeTable, null, "the shardingsphere proxy route table");
        prometheusMetricsRegister.counterIncrement(routeTable, null);
        prometheusMetricsRegister.counterIncrement(routeTable, null, 2);
        Map<String, Counter> counterMap = (Map<String, Counter>) ReflectiveUtil.getFieldValue(prometheusMetricsRegister, "COUNTER_MAP");
        assertThat(counterMap.size(), is(2));
        Counter routeDatasourceCounter = counterMap.get(routeDatasource);
        assertThat(routeDatasourceCounter.labels(labelNames).get(), is(3.0d));
        Counter routeTableCounter = counterMap.get(routeTable);
        assertThat(routeTableCounter.get(), is(3.0d));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertGauge() {
        String connectionTotal = "proxy_connection_total";
        String[] labelNames = {"connectionTotal"};
        prometheusMetricsRegister.registerGauge(connectionTotal, labelNames, "the shardingsphere proxy request total");
        prometheusMetricsRegister.gaugeIncrement(connectionTotal, labelNames);
        prometheusMetricsRegister.gaugeIncrement(connectionTotal, labelNames);
        prometheusMetricsRegister.gaugeDecrement(connectionTotal, labelNames);
        String handlerTotal = "handler_total";
        prometheusMetricsRegister.registerGauge(handlerTotal, null, "the shardingsphere proxy handler total");
        prometheusMetricsRegister.gaugeIncrement(handlerTotal, null);
        prometheusMetricsRegister.gaugeIncrement(handlerTotal, null);
        prometheusMetricsRegister.gaugeDecrement(handlerTotal, null);
        Map<String, Gauge> gaugeMap = (Map<String, Gauge>) ReflectiveUtil.getFieldValue(prometheusMetricsRegister, "GAUGE_MAP");
        assertThat(gaugeMap.size(), is(2));
        Gauge connectionTotalGauge = gaugeMap.get(connectionTotal);
        assertThat(connectionTotalGauge.labels(labelNames).get(), is(1.0d));
        Gauge handlerTotalGauge = gaugeMap.get(handlerTotal);
        assertThat(handlerTotalGauge.get(), is(1.0d));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertHistogram() {
        String name = "proxy_execute_latency_millis";
        String[] labelNames = {"name"};
        prometheusMetricsRegister.registerHistogram(name, labelNames, "the shardingsphere proxy executor latency millis");
        prometheusMetricsRegister.recordTime(name, labelNames, 1000);
        String latencyMillis = "execute_latency_millis";
        prometheusMetricsRegister.registerHistogram(latencyMillis, null, "the shardingsphere executor latency millis");
        prometheusMetricsRegister.recordTime(latencyMillis, null, 1000);
        Map<String, Histogram> histogramMap = (Map<String, Histogram>) ReflectiveUtil.getFieldValue(prometheusMetricsRegister, "HISTOGRAM_MAP");
        assertThat(histogramMap.size(), is(2));
        Histogram histogram = histogramMap.get(name);
        assertThat(histogram.labels(labelNames).get().sum, is(1000.0));
    }
}
