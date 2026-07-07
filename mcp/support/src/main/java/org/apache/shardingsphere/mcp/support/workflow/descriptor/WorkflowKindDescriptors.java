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

package org.apache.shardingsphere.mcp.support.workflow.descriptor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Collection;
import java.util.Set;

/**
 * Shared workflow kind descriptors.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowKindDescriptors {
    
    public static final String ENCRYPT_RULE = "encrypt.rule";
    
    public static final String MASK_RULE = "mask.rule";
    
    public static final String BROADCAST_RULE = "broadcast.rule";
    
    public static final String READWRITE_RULE = "readwrite.rule";
    
    public static final String READWRITE_STATUS = "readwrite.status";
    
    public static final String SHADOW_RULE = "shadow.rule";
    
    public static final String SHADOW_DEFAULT_ALGORITHM = "shadow.default";
    
    public static final String SHADOW_ALGORITHM_CLEANUP = "shadow.cleanup";
    
    public static final String SHARDING_TABLE_RULE = "sharding.table.rule";
    
    public static final String SHARDING_TABLE_REFERENCE = "sharding.table.reference";
    
    public static final String SHARDING_DEFAULT_STRATEGY = "sharding.default.strategy";
    
    public static final String SHARDING_KEY_GENERATOR = "sharding.key.generator";
    
    public static final String SHARDING_KEY_GENERATE_STRATEGY = "sharding.key.generate.strategy";
    
    public static final String SHARDING_COMPONENT_CLEANUP = "sharding.component.cleanup";
    
    private static final Collection<String> RULE_DISTSQL_ONLY_WORKFLOW_KINDS = Set.of(ENCRYPT_RULE, MASK_RULE, BROADCAST_RULE, READWRITE_RULE, READWRITE_STATUS, SHADOW_RULE,
            SHADOW_DEFAULT_ALGORITHM, SHADOW_ALGORITHM_CLEANUP, SHARDING_TABLE_RULE, SHARDING_TABLE_REFERENCE, SHARDING_DEFAULT_STRATEGY, SHARDING_KEY_GENERATOR,
            SHARDING_KEY_GENERATE_STRATEGY, SHARDING_COMPONENT_CLEANUP);
    
    /**
     * Judge whether workflow must expose rule DistSQL artifacts only.
     *
     * @param workflowKind workflow kind
     * @return whether workflow is rule DistSQL only
     */
    public static boolean isRuleDistSQLOnly(final String workflowKind) {
        return RULE_DISTSQL_ONLY_WORKFLOW_KINDS.contains(workflowKind);
    }
}
