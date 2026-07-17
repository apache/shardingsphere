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
 * Workflow issue codes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowIssueCode {
    
    public static final String DATABASE_REQUIRED = "WF-CTX-001";
    
    public static final String SESSION_OWNERSHIP_MISMATCH = "WF-CTX-002";
    
    public static final String TABLE_REQUIRED = "WF-CTX-003";
    
    public static final String COLUMN_REQUIRED = "WF-CTX-004";
    
    public static final String TABLE_NOT_FOUND = "WF-META-001";
    
    public static final String COLUMN_NOT_FOUND = "WF-META-002";
    
    public static final String ALGORITHM_NOT_FOUND = "WF-ALGO-001";
    
    public static final String ALGORITHM_CAPABILITY_CONFLICT = "WF-ALGO-002";
    
    public static final String REQUIRED_PROPERTY_MISSING = "WF-PROP-001";
    
    public static final String SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED = "WF-PROP-002";
    
    public static final String DROP_TARGET_RULE_NOT_FOUND = "WF-LIFE-001";
    
    public static final String WORKFLOW_STATUS_INVALID = "WF-LIFE-002";
    
    public static final String ENCRYPT_DROP_SCOPE_LIMITED = "WF-LIFE-003";
    
    public static final String ENCRYPT_RULE_REWRITE_LIMITED = "WF-LIFE-005";
    
    public static final String MASK_RULE_REWRITE_LIMITED = "WF-LIFE-006";
    
    public static final String RULE_EXECUTION_FAILED = "WF-RULE-001";
    
    public static final String RULE_INPUT_REQUIRED = "WF-RULE-002";
    
    public static final String RULE_INPUT_CONFLICT = "WF-RULE-003";
    
    public static final String MANUAL_EXECUTION_PENDING = "WF-MODE-001";
    
    public static final String UNSUPPORTED_IDENTIFIER = "WF-SQL-001";
    
    public static final String RULE_STATE_MISMATCH = "WF-VAL-002";
    
    public static final String SQL_EXECUTABILITY_FAILED = "WF-VAL-003";
}
