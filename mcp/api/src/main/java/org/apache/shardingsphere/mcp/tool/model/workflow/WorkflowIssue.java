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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Workflow issue.
 */
@RequiredArgsConstructor
@Getter
public final class WorkflowIssue {
    
    private final String code;
    
    private final String severity;
    
    private final String stage;
    
    private final String message;
    
    private final String userAction;
    
    private final boolean retryable;
    
    private final Map<String, Object> details;
    
    /**
     * Convert to map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("code", code);
        result.put("severity", severity);
        result.put("stage", stage);
        result.put("message", message);
        result.put("user_action", userAction);
        result.put("retryable", retryable);
        result.put("details", details);
        return result;
    }
}
