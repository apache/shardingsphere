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
 * ShardingSphere MCP metadata keys.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPShardingSphereMetadataKeys {
    
    public static final String PREFIX = "org.apache.shardingsphere/";
    
    public static final String RESOURCE_KIND = PREFIX + "resource-kind";
    
    public static final String OBJECT_SCOPE = PREFIX + "object-scope";
    
    public static final String FEATURE = PREFIX + "feature";
    
    public static final String RUNTIME_VISIBILITY = PREFIX + "runtime-visibility";
    
    public static final String URI_VARIABLES = PREFIX + "uri-variables";
    
    public static final String RELATED_TOOLS = PREFIX + "related-tools";
    
    public static final String FOLLOW_UP_TOOLS = PREFIX + "follow-up-tools";
    
    public static final String REJECTED_STATEMENT_CLASSES = PREFIX + "rejected-statement-classes";
    
    public static final String WORKFLOW_KIND = PREFIX + "workflow-kind";
    
    public static final String RELATED_RESOURCE_URIS = PREFIX + "related-resource-uris";
    
    public static final String USE_BEFORE = PREFIX + "use-before";
    
    public static final String STOP_CONDITIONS = PREFIX + "stop-conditions";
    
    public static final String ASK_USER_CONDITIONS = PREFIX + "ask-user-conditions";
    
    public static final String RESOURCE_LINKS_EMITTED = PREFIX + "resource-links-emitted";
    
    public static final String RESOURCE_LINKS_OMITTED = PREFIX + "resource-links-omitted";
    
    public static final String RESOURCE_LINK_LIMIT = PREFIX + "resource-link-limit";
    
    public static final String PURPOSE = PREFIX + "purpose";
    
    public static final String SOURCE_FIELD = PREFIX + "source-field";
    
    public static final String TOOL = PREFIX + "tool";
    
    public static final String PLAN_ID = PREFIX + "plan-id";
    
    public static final String FORM_REQUEST_ID = PREFIX + "form-request-id";
    
    public static final String RESPONSE_MODE = PREFIX + "response-mode";
    
    public static final String REFERENCE_TYPE = PREFIX + "reference-type";
    
    public static final String REFERENCE = PREFIX + "reference";
    
    public static final String ARGUMENT = PREFIX + "argument";
    
    public static final String PREFIX_ARGUMENT = PREFIX + "prefix";
    
    public static final String MATCH_STRATEGY = PREFIX + "match-strategy";
    
    public static final String CONTEXT_ARGUMENTS = PREFIX + "context-arguments";
    
    public static final String CANDIDATE_COUNT = PREFIX + "candidate-count";
    
    public static final String MATCHED_CANDIDATE_COUNT = PREFIX + "matched-candidate-count";
    
    public static final String RETURNED_CANDIDATE_COUNT = PREFIX + "returned-candidate-count";
    
    public static final String CONTINUATION_MODE = PREFIX + "continuation-mode";
    
    public static final String MISSING_CONTEXT_ARGUMENTS = PREFIX + "missing-context-arguments";
    
    public static final String DIAGNOSTIC = PREFIX + "diagnostic";
    
    public static final String RECOVERY = PREFIX + "recovery";
    
    public static final String NEXT_ACTIONS = PREFIX + "next-actions";
    
    public static final String RANKING_POLICY = PREFIX + "ranking-policy";
    
    public static final String VALUE_DETAILS = PREFIX + "value-details";
    
    public static final String INFERRED_CONTEXT_ARGUMENTS = PREFIX + "inferred-context-arguments";
    
    public static final String ARGUMENT_PROVENANCE = PREFIX + "argument-provenance";
}
