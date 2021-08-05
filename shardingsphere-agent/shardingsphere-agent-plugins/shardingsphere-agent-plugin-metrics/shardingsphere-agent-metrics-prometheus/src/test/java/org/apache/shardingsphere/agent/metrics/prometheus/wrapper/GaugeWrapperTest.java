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

import io.prometheus.client.Gauge;
import org.apache.shardingsphere.agent.metrics.prometheus.util.ReflectiveUtil;
import org.hamcrest.Matchers;
import org.junit.Test;

public final class GaugeWrapperTest {
    
    @Test
    public void assertCreate() {
        Gauge gauge = Gauge.build().name("a").help("help").create();
        GaugeWrapper gaugeWrapper = new GaugeWrapper(gauge);
        gaugeWrapper.inc();
        gaugeWrapper.inc(1);
        gauge = (Gauge) ReflectiveUtil.getFieldValue(gaugeWrapper, "gauge");
        org.hamcrest.MatcherAssert.assertThat(gauge.get(), Matchers.is(2.0));
    }
}
