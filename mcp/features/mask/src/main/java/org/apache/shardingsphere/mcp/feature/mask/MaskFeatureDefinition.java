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

package org.apache.shardingsphere.mcp.feature.mask;

/**
 * Mask MCP feature definition.
 */
public final class MaskFeatureDefinition {
    
    public static final String PLAN_TOOL_NAME = "plan_mask_rule";
    
    public static final String APPLY_TOOL_NAME = "apply_mask_rule";
    
    public static final String VALIDATE_TOOL_NAME = "validate_mask_rule";
    
    public static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/mask/algorithms";
    
    public static final String RULES_RESOURCE_URI = "shardingsphere://features/mask/databases/{database}/rules";
    
    public static final String RULE_RESOURCE_URI = "shardingsphere://features/mask/databases/{database}/tables/{table}/rules";
    
    private MaskFeatureDefinition() {
    }
}
