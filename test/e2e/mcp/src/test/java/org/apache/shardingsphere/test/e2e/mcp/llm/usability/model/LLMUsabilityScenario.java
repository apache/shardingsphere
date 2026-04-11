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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.model;

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;

import java.util.List;

public final class LLMUsabilityScenario {
    
    private final String scenarioId;
    
    private final LLMUsabilityDimension dimension;
    
    private final String runtimeKind;
    
    private final LLME2EScenario llmScenario;
    
    private final List<String> expectedFirstActionNames;
    
    private final List<String> expectedResourceUris;
    
    private final boolean resourceHitRequired;
    
    private final boolean recoveryExpected;
    
    public LLMUsabilityScenario(final String scenarioId, final LLMUsabilityDimension dimension, final String runtimeKind, final LLME2EScenario llmScenario,
                                final List<String> expectedFirstActionNames, final List<String> expectedResourceUris,
                                final boolean resourceHitRequired, final boolean recoveryExpected) {
        this.scenarioId = scenarioId;
        this.dimension = dimension;
        this.runtimeKind = runtimeKind;
        this.llmScenario = llmScenario;
        this.expectedFirstActionNames = List.copyOf(expectedFirstActionNames);
        this.expectedResourceUris = List.copyOf(expectedResourceUris);
        this.resourceHitRequired = resourceHitRequired;
        this.recoveryExpected = recoveryExpected;
    }
    
    public String scenarioId() {
        return scenarioId;
    }
    
    public LLMUsabilityDimension dimension() {
        return dimension;
    }
    
    public String runtimeKind() {
        return runtimeKind;
    }
    
    public LLME2EScenario llmScenario() {
        return llmScenario;
    }
    
    public List<String> expectedFirstActionNames() {
        return expectedFirstActionNames;
    }
    
    public List<String> expectedResourceUris() {
        return expectedResourceUris;
    }
    
    public boolean resourceHitRequired() {
        return resourceHitRequired;
    }
    
    public boolean recoveryExpected() {
        return recoveryExpected;
    }
    
    public boolean isQueryScenario() {
        return LLMUsabilityDimension.QUERY == dimension || LLMUsabilityDimension.RECOVERY == dimension || LLMUsabilityDimension.RESOURCE == dimension;
    }
}
