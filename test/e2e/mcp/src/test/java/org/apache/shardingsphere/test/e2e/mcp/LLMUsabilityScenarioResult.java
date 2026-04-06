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

package org.apache.shardingsphere.test.e2e.mcp;

import java.util.List;

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
    
    LLMUsabilityScenarioResult(final String scenarioId, final LLMUsabilityDimension dimension, final String runtimeKind, final boolean success,
                               final String failureType, final String message, final boolean firstCorrectAction, final int invalidCallCount,
                               final int roundTripCount, final boolean resourceHit, final boolean recoveredAfterError,
                               final double queryAnswerFidelity, final boolean boundaryConfusion, final boolean degradedSuccess,
                               final List<MCPInteractionTraceRecord> interactionTrace) {
        this.scenarioId = scenarioId;
        this.dimension = dimension;
        this.runtimeKind = runtimeKind;
        this.success = success;
        this.failureType = failureType;
        this.message = message;
        this.firstCorrectAction = firstCorrectAction;
        this.invalidCallCount = invalidCallCount;
        this.roundTripCount = roundTripCount;
        this.resourceHit = resourceHit;
        this.recoveredAfterError = recoveredAfterError;
        this.queryAnswerFidelity = queryAnswerFidelity;
        this.boundaryConfusion = boundaryConfusion;
        this.degradedSuccess = degradedSuccess;
        this.interactionTrace = List.copyOf(interactionTrace);
    }
    
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
