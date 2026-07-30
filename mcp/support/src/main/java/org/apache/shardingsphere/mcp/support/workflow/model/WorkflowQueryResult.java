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

package org.apache.shardingsphere.mcp.support.workflow.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Workflow query result that distinguishes confirmed backend data from compatibility fallback data.
 */
@RequiredArgsConstructor
@Getter
public final class WorkflowQueryResult {
    
    private final List<Map<String, Object>> rows;
    
    private final boolean availabilityConfirmed;
    
    /**
     * Create a confirmed query result.
     *
     * @param rows queried rows
     * @return confirmed query result
     */
    public static WorkflowQueryResult confirmed(final List<Map<String, Object>> rows) {
        return new WorkflowQueryResult(rows, true);
    }
    
    /**
     * Create a compatibility fallback result.
     *
     * @param rows fallback rows
     * @return fallback query result
     */
    public static WorkflowQueryResult fallback(final List<Map<String, Object>> rows) {
        return new WorkflowQueryResult(rows, false);
    }
}
