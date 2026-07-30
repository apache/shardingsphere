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

package org.apache.shardingsphere.mcp.support.database.tool.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Runtime database validation result.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class RuntimeDatabaseValidationResult {
    
    private final String status;
    
    private final String database;
    
    private final List<RuntimeDatabaseValidationCheckResult> checks;
    
    private final String category;
    
    /**
     * Create a ready validation result.
     *
     * @param database database name
     * @param checks check results
     * @return validation result
     */
    public static RuntimeDatabaseValidationResult ready(final String database, final List<RuntimeDatabaseValidationCheckResult> checks) {
        return new RuntimeDatabaseValidationResult("ready", database, checks, "ready");
    }
    
    /**
     * Create a failed validation result.
     *
     * @param database database name
     * @param checks check results
     * @param category failure category
     * @return validation result
     */
    public static RuntimeDatabaseValidationResult failed(final String database, final List<RuntimeDatabaseValidationCheckResult> checks, final String category) {
        return new RuntimeDatabaseValidationResult("failed", database, checks, category);
    }
}
