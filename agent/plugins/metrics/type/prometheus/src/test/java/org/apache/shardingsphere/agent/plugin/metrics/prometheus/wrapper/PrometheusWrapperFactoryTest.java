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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper;

import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.CounterWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.GaugeWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.HistogramWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.SummaryWrapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PrometheusWrapperFactoryTest {
    
    @Test
    public void assertCreate() {
        PrometheusWrapperFactory factory = new PrometheusWrapperFactory();
        assertThat(factory.create("a"), instanceOf(CounterWrapper.class));
        assertThat(factory.create("b"), instanceOf(GaugeWrapper.class));
        assertThat(factory.create("c"), instanceOf(HistogramWrapper.class));
        assertThat(factory.create("d"), instanceOf(HistogramWrapper.class));
        assertThat(factory.create("e"), instanceOf(SummaryWrapper.class));
    }
}
