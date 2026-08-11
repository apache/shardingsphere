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
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Rule workflow feature-scoped state.
 */
@Getter
@NoArgsConstructor
public final class RuleWorkflowFeatureData {
    
    private final List<Map<String, Object>> expectedRules = new LinkedList<>();
    
    public RuleWorkflowFeatureData(final List<Map<String, Object>> expectedRules) {
        expectedRules.forEach(each -> this.expectedRules.add(WorkflowContextSnapshot.copyMap(each)));
    }
    
    RuleWorkflowFeatureData copy() {
        return new RuleWorkflowFeatureData(expectedRules);
    }
}
