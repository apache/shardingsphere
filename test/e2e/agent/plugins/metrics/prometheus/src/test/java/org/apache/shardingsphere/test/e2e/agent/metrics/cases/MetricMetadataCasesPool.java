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

package org.apache.shardingsphere.test.e2e.agent.metrics.cases;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Metric meta data cases pool.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricMetadataCasesPool {
    
    /**
     * Get metric cases.
     *
     * @return metric cases
     */
    public static Collection<MetricMetadataCase> getMetricCases() {
        Collection<MetricMetadataCase> result = new LinkedHashSet<>();
        result.add(new MetricMetadataCase("proxy_commit_transactions_total", "counter"));
        result.add(new MetricMetadataCase("proxy_rollback_transactions_total", "counter"));
        result.add(new MetricMetadataCase("proxy_execute_errors_total", "counter"));
        result.add(new MetricMetadataCase("proxy_current_connections", "gauge"));
        result.add(new MetricMetadataCase("proxy_requests_total", "counter"));
        result.add(new MetricMetadataCase("routed_result_total", "counter"));
        result.add(new MetricMetadataCase("proxy_execute_latency_millis", "histogram"));
        result.add(new MetricMetadataCase("routed_sql_total", "counter"));
        result.add(new MetricMetadataCase("parsed_sql_total", "counter"));
        result.add(new MetricMetadataCase("proxy_state", "gauge"));
        result.add(new MetricMetadataCase("build_info", "gauge"));
        result.add(new MetricMetadataCase("proxy_meta_data_info", "gauge"));
        return result;
    }
}
