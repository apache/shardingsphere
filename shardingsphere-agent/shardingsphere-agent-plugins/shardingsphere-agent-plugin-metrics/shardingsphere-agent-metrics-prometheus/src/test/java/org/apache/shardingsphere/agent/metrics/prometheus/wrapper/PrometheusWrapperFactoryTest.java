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

package org.apache.shardingsphere.agent.metrics.prometheus.wrapper;

import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;

public final class PrometheusWrapperFactoryTest {
    
    @Test
    public void assertCreate() {
        PrometheusWrapperFactory factory = new PrometheusWrapperFactory();
        Optional<MetricsWrapper> counterWrapper = factory.create("a");
        assertNotNull(counterWrapper.get());
        Optional<MetricsWrapper> gaugeWrapper = factory.create("b");
        assertNotNull(gaugeWrapper.get());
        Optional<MetricsWrapper> histogramWrapper1 = factory.create("c");
        assertNotNull(histogramWrapper1.get());
        Optional<MetricsWrapper> histogramWrapper2 = factory.create("d");
        assertNotNull(histogramWrapper2.get());
        Optional<MetricsWrapper> summaryWrapper = factory.create("e");
        assertNotNull(summaryWrapper.get());
    }
}
