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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.runtime.MCPInteractionTraceRecord;

import java.util.List;

@RequiredArgsConstructor
final class LLMUsabilityScenarioResult {
    
    private final String scenarioId;
    
    private final LLMUsabilityDimension dimension;
    
    private final String runtimeKind;
    
    private final boolean success;
    
    private final String failureType;
    
    private final String message;
    
    private final boolean firstCorrectAction;
    
    private final int invalidCallCount;
    
    private final int roundTripCount;
    
    private final boolean resourceHit;
    
    private final boolean recoveredAfterError;
    
    private final double queryAnswerFidelity;
    
    private final boolean boundaryConfusion;
    
    private final boolean degradedSuccess;
    
    private final List<MCPInteractionTraceRecord> interactionTrace;
    
    String scenarioId() {
        return scenarioId;
    }
    
    LLMUsabilityDimension dimension() {
        return dimension;
    }
    
    String runtimeKind() {
        return runtimeKind;
    }
    
    boolean success() {
        return success;
    }
    
    String failureType() {
        return failureType;
    }
    
    String message() {
        return message;
    }
    
    boolean firstCorrectAction() {
        return firstCorrectAction;
    }
    
    int invalidCallCount() {
        return invalidCallCount;
    }
    
    int roundTripCount() {
        return roundTripCount;
    }
    
    boolean resourceHit() {
        return resourceHit;
    }
    
    boolean recoveredAfterError() {
        return recoveredAfterError;
    }
    
    double queryAnswerFidelity() {
        return queryAnswerFidelity;
    }
    
    boolean boundaryConfusion() {
        return boundaryConfusion;
    }
    
    boolean degradedSuccess() {
        return degradedSuccess;
    }
    
    List<MCPInteractionTraceRecord> interactionTrace() {
        return interactionTrace;
    }
}
