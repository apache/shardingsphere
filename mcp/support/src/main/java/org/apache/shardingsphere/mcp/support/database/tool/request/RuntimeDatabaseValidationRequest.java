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

package org.apache.shardingsphere.mcp.support.database.tool.request;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * Runtime database validation request.
 */
@Getter
public final class RuntimeDatabaseValidationRequest {
    
    private final String database;
    
    public RuntimeDatabaseValidationRequest(final String database) {
        this.database = Objects.toString(database, "");
    }
    
    /**
     * Create request from MCP tool arguments.
     *
     * @param arguments tool arguments
     * @return request
     */
    public static RuntimeDatabaseValidationRequest from(final Map<String, Object> arguments) {
        return new RuntimeDatabaseValidationRequest(getRawString(arguments, "database"));
    }
    
    private static String getRawString(final Map<String, Object> arguments, final String fieldName) {
        return Objects.toString(arguments.get(fieldName), "");
    }
}
