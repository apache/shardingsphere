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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.Locale;

/**
 * Shared workflow intent resolver support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowIntentResolverSupport {
    
    /**
     * Resolve workflow operation type from explicit fields and heuristics.
     *
     * @param request workflow request
     * @return resolved operation type
     */
    public static String resolveOperationType(final WorkflowRequest request) {
        String actualOperationType = WorkflowSqlUtils.trimToEmpty(request.getOperationType()).toLowerCase(Locale.ENGLISH);
        if (!actualOperationType.isEmpty()) {
            return actualOperationType;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        if (naturalLanguageIntent.contains("drop") || naturalLanguageIntent.contains("删除")) {
            return WorkflowLifecycle.OPERATION_DROP;
        }
        if (naturalLanguageIntent.contains("alter") || naturalLanguageIntent.contains("修改") || naturalLanguageIntent.contains("补")) {
            return "alter";
        }
        return "create";
    }
    
    /**
     * Resolve field semantics from explicit fields and heuristics.
     *
     * @param request workflow request
     * @return resolved field semantics
     */
    public static String resolveFieldSemantics(final WorkflowRequest request) {
        String actualFieldSemantics = WorkflowSqlUtils.trimToEmpty(request.getFieldSemantics()).toLowerCase(Locale.ENGLISH);
        if (!actualFieldSemantics.isEmpty()) {
            return actualFieldSemantics;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        String columnName = WorkflowSqlUtils.trimToEmpty(request.getColumn()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("手机号") || columnName.contains("phone") || columnName.contains("mobile") || columnName.contains("tel")) {
            return "phone";
        }
        if (naturalLanguageIntent.contains("身份证") || columnName.contains("id_card")) {
            return "id_card";
        }
        return columnName;
    }
    
    private static String getNaturalLanguageIntent(final WorkflowRequest request) {
        return WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
    }
}
