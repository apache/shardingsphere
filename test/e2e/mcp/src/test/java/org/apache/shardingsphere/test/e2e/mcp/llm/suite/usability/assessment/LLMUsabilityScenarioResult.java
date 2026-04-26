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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.List;

@RequiredArgsConstructor
@Getter
public final class LLMUsabilityScenarioResult {
    
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
    
    private final List<MCPInteractionTraceRecord> interactionTrace;
}
