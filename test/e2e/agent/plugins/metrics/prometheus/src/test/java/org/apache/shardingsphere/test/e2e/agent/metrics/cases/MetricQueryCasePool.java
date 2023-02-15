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
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Metric query case pool.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricQueryCasePool {
    
    /**
     * Get metric query cases.
     *
     * @return metric query cases
     */
    public static Collection<MetricQueryCase> getMetricQueryCases() {
        Collection<MetricQueryCase> result = new LinkedHashSet<>();
        result.add(new MetricQueryCase("proxy_commit_transactions_total", "proxy_commit_transactions_total{}"));
        result.add(new MetricQueryCase("proxy_rollback_transactions_total", "proxy_rollback_transactions_total{}"));
        result.add(new MetricQueryCase("proxy_current_connections", "proxy_current_connections{}"));
        result.add(new MetricQueryCase("proxy_requests_total", "proxy_requests_total{}"));
        result.add(new MetricQueryCase("routed_result_total", "routed_result_total{object='data_source', name='ds_0'}"));
        result.add(new MetricQueryCase("routed_result_total", "routed_result_total{object='data_source', name='ds_1'}"));
        result.add(new MetricQueryCase("routed_result_total", "routed_result_total{object='table', name='t_order_0'}"));
        result.add(new MetricQueryCase("routed_result_total", "routed_result_total{object='table', name='t_order_1'}"));
        result.add(new MetricQueryCase("proxy_execute_latency_millis_bucket", "proxy_execute_latency_millis_bucket{}"));
        result.add(new MetricQueryCase("routed_sql_total", "routed_sql_total{type='INSERT'}"));
        result.add(new MetricQueryCase("routed_sql_total", "routed_sql_total{type='SELECT'}"));
        result.add(new MetricQueryCase("routed_sql_total", "routed_sql_total{type='UPDATE'}"));
        result.add(new MetricQueryCase("routed_sql_total", "routed_sql_total{type='DELETE'}"));
        result.add(new MetricQueryCase("parsed_sql_total", "parsed_sql_total{type='INSERT'}"));
        result.add(new MetricQueryCase("parsed_sql_total", "parsed_sql_total{type='SELECT'}"));
        result.add(new MetricQueryCase("parsed_sql_total", "parsed_sql_total{type='UPDATE'}"));
        result.add(new MetricQueryCase("parsed_sql_total", "parsed_sql_total{type='DELETE'}"));
        result.add(new MetricQueryCase("parsed_sql_total", "parsed_sql_total{type='TCL'}"));
        result.add(new MetricQueryCase("proxy_state", "proxy_state{}", 0));
        result.add(new MetricQueryCase("build_info", String.format("build_info{name='ShardingSphere', version='%s'}", ShardingSphereVersion.VERSION)));
        result.add(new MetricQueryCase("proxy_meta_data_info", "proxy_meta_data_info{name='schema_count'}"));
        result.add(new MetricQueryCase("proxy_meta_data_info", "proxy_meta_data_info{name='database_count'}"));
        result.add(new MetricQueryCase("proxy_execute_errors_total", "proxy_execute_errors_total{}"));
        return result;
    }
}
