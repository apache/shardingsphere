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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Workflow lifecycle vocabulary.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowLifecycle {
    
    public static final String DELIVERY_MODE_ALL_AT_ONCE = "all-at-once";
    
    public static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";
    
    public static final String EXECUTION_MODE_PREVIEW = "preview";
    
    public static final String EXECUTION_MODE_REVIEW_THEN_EXECUTE = "review-then-execute";
    
    public static final String STATUS_AWAITING_MANUAL_EXECUTION = "awaiting-manual-execution";
    
    public static final String STATUS_CLARIFYING = "clarifying";
    
    public static final String STATUS_COMPLETED = "completed";
    
    public static final String STATUS_EXECUTED = "executed";
    
    public static final String STATUS_FAILED = "failed";
    
    public static final String STATUS_PASSED = "passed";
    
    public static final String STATUS_PLANNED = "planned";
    
    public static final String STATUS_PREVIEWED = "previewed";
    
    public static final String STATUS_SKIPPED = "skipped";
    
    public static final String STATUS_VALIDATED = "validated";
    
    public static final String STEP_CLARIFYING = "clarifying";
    
    public static final String STEP_COLLECTING_PROPERTIES = "collecting-properties";
    
    public static final String STEP_COLLECTING_RULE_INPUTS = "collecting-rule-inputs";
    
    public static final String STEP_DISCOVERING = "discovering";
    
    public static final String STEP_EXECUTED = "executed";
    
    public static final String STEP_EXECUTING = "executing";
    
    public static final String STEP_FAILED = "failed";
    
    public static final String STEP_INTAKING = "intaking";
    
    public static final String STEP_MANUAL_EXECUTION = "manual-execution";
    
    public static final String STEP_PLANNING_ARTIFACTS = "planning-artifacts";
    
    public static final String STEP_REVIEW = "review";
    
    public static final String STEP_SELECTING_ALGORITHM = "selecting-algorithm";
    
    public static final String STEP_VALIDATED = "validated";
    
    public static final String STEP_VALIDATING = "validating";
    
    public static final String OPERATION_ALTER = "alter";
    
    public static final String OPERATION_CREATE = "create";
    
    public static final String OPERATION_DROP = "drop";
    
}
