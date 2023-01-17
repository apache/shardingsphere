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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapperRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapperFactory;
import org.junit.After;
import org.junit.BeforeClass;

public abstract class MetricsAdviceBaseTest {
    
    @BeforeClass
    public static void setup() {
        MetricsWrapperRegistry.setMetricsFactory(new FixtureWrapperFactory());
    }
    
    @After
    public void reset() {
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_insert_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_update_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_delete_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_select_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_ddl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_dcl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_dal_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_tcl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_rql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_rdl_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_ral_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("parsed_rul_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("routed_insert_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("routed_update_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("routed_delete_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("routed_select_sql_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("routed_data_sources_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("routed_tables_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("proxy_execute_latency_millis")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("proxy_execute_errors_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("proxy_current_connections")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("proxy_requests_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("proxy_commit_transactions_total")).reset();
        ((FixtureWrapper) MetricsWrapperRegistry.get("proxy_rollback_transactions_total")).reset();
    }
}
