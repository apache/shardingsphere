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

import io.prometheus.client.Collector.MetricFamilySamples;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BuildInfoCollectorTest {

    @Test
    public void assertCollect() {
        BuildInfoCollector buildInfoCollector = new BuildInfoCollector();
        List<MetricFamilySamples> metricFamilySamples = buildInfoCollector.collect();
        assertThat(metricFamilySamples.toString(), is("[Name: jmx_exporter_build_info Type: GAUGE Help: "
                + "A metric with a constant '1' value labeled with the version of the JMX exporter. Samples: "
                + "[Name: jmx_exporter_build_info LabelNames: [version, name] labelValues: [unknown, unknown] Value: 1.0 TimestampMs: null]]"));
    }
}
