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

/**
 * Workflow lifecycle vocabulary.
 */
public final class WorkflowLifecycle {
    
    public static final String STATUS_AWAITING_MANUAL_EXECUTION = "awaiting-manual-execution";
    
    public static final String STATUS_CLARIFYING = "clarifying";
    
    public static final String STATUS_COMPLETED = "completed";
    
    public static final String STATUS_EXECUTED = "executed";
    
    public static final String STATUS_FAILED = "failed";
    
    public static final String STATUS_PASSED = "passed";
    
    public static final String STATUS_PLANNED = "planned";
    
    public static final String STATUS_SKIPPED = "skipped";
    
    public static final String STATUS_VALIDATED = "validated";
    
    public static final String STEP_CLARIFYING = "clarifying";
    
    public static final String STEP_EXECUTED = "executed";
    
    public static final String STEP_FAILED = "failed";
    
    public static final String STEP_INTAKING = "intaking";
    
    public static final String STEP_MANUAL_EXECUTION = "manual-execution";
    
    public static final String STEP_REVIEW = "review";
    
    public static final String STEP_VALIDATED = "validated";
    
    public static final String OPERATION_DROP = "drop";
    
    private WorkflowLifecycle() {
    }
}
