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

package org.apache.shardingsphere.agent.metrics.api.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Build info collector.
 */
public final class BuildInfoCollector extends Collector {
    
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new ArrayList<>();
        GaugeMetricFamily artifactInfo = new GaugeMetricFamily(
                "jmx_exporter_build_info",
                "A metric with a constant '1' value labeled with the version of the JMX exporter.",
                Arrays.asList("version", "name"));
        Package pkg = this.getClass().getPackage();
        String version = pkg.getImplementationVersion();
        String name = pkg.getImplementationTitle();
        artifactInfo.addMetric(Arrays.asList(null != version ? version : "unknown", null != name ? name : "unknown"), 1L);
        result.add(artifactInfo);
        return result;
    }
}
