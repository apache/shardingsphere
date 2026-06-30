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

package org.apache.shardingsphere.test.e2e.mcp.support;

import java.util.List;

/**
 * Official MCP tool names packaged by default.
 */
public final class OfficialMCPToolNames {
    
    private static final List<String> ALL = List.of(
            "database_gateway_search_metadata",
            "database_gateway_validate_runtime_database",
            "database_gateway_execute_query",
            "database_gateway_execute_update",
            "database_gateway_apply_workflow",
            "database_gateway_validate_workflow",
            "database_gateway_plan_encrypt_rule",
            "database_gateway_plan_mask_rule",
            "database_gateway_plan_broadcast_rule",
            "database_gateway_plan_readwrite_splitting_rule",
            "database_gateway_plan_readwrite_splitting_status",
            "database_gateway_plan_shadow_rule",
            "database_gateway_plan_default_shadow_algorithm",
            "database_gateway_plan_shadow_algorithm_cleanup",
            "database_gateway_plan_sharding_table_rule",
            "database_gateway_plan_sharding_table_reference_rule",
            "database_gateway_plan_sharding_default_strategy",
            "database_gateway_plan_sharding_key_generator",
            "database_gateway_plan_sharding_key_generate_strategy",
            "database_gateway_plan_sharding_rule_component_cleanup");
    
    private OfficialMCPToolNames() {
    }
    
    /**
     * Get official MCP tool names.
     *
     * @return official MCP tool names
     */
    public static List<String> getAll() {
        return ALL;
    }
}
