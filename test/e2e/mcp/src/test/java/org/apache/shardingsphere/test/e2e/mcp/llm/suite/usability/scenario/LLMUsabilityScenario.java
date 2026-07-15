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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityDimension;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public final class LLMUsabilityScenario {
    
    public static final String NATURAL_TASK_TAG = "natural-task";
    
    public static final String PROTOCOL_CONTRACT_TAG = "protocol-contract";
    
    private final String scenarioId;
    
    private final LLMUsabilityDimension dimension;
    
    private final String runtimeKind;
    
    private final List<String> tags;
    
    private final LLME2EScenario llmScenario;
    
    private final List<String> expectedFirstActionNames;
    
    private final List<String> expectedResourceUris;
    
    private final boolean resourceHitRequired;
    
    private final boolean recoveryExpected;
    
    private final String expectedRecoveryCategory;
    
    /**
     * Is query scenario.
     *
     * @return query scenario
     */
    public boolean isQueryScenario() {
        return LLMUsabilityDimension.TOOL == dimension || LLMUsabilityDimension.RECOVERY == dimension || LLMUsabilityDimension.RESOURCE == dimension;
    }
    
    /**
     * Is natural task.
     *
     * @return natural task
     */
    public boolean isNaturalTask() {
        return tags.contains(NATURAL_TASK_TAG);
    }
    
    /**
     * Is protocol contract.
     *
     * @return protocol contract
     */
    public boolean isProtocolContract() {
        return tags.contains(PROTOCOL_CONTRACT_TAG);
    }
}
