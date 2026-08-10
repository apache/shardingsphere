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

package org.apache.shardingsphere.mcp.support.descriptor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Core MCP tool names.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreToolNames {
    
    public static final String SEARCH_METADATA = "database_gateway_search_metadata";
    
    public static final String VALIDATE_RUNTIME_DATABASE = "database_gateway_validate_runtime_database";
    
    public static final String EXECUTE_QUERY = "database_gateway_execute_query";
    
    public static final String EXECUTE_EXPLAIN_QUERY = "database_gateway_execute_explain_query";
    
    public static final String EXECUTE_UPDATE = "database_gateway_execute_update";
}
