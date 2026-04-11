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

import java.util.List;

final class LLMUsabilityScorecard {
    
    private final String suiteId;
    
    private final String runId;
    
    private final double taskSuccessRate;
    
    private final double firstCorrectActionRate;
    
    private final double invalidCallRate;
    
    private final double averageRoundTrips;
    
    private final double queryAnswerFidelity;
    
    private final double boundaryConfusionRate;
    
    private final double resourceHitRate;
    
    private final double recoveryRate;
    
    private final List<LLMUsabilityDimensionScore> dimensionScores;
    
    private final List<LLMUsabilityScenarioResult> scenarioResults;
    
    LLMUsabilityScorecard(final String suiteId, final String runId, final double taskSuccessRate, final double firstCorrectActionRate,
                          final double invalidCallRate, final double averageRoundTrips, final double queryAnswerFidelity,
                          final double boundaryConfusionRate, final double resourceHitRate, final double recoveryRate,
                          final List<LLMUsabilityDimensionScore> dimensionScores, final List<LLMUsabilityScenarioResult> scenarioResults) {
        this.suiteId = suiteId;
        this.runId = runId;
        this.taskSuccessRate = taskSuccessRate;
        this.firstCorrectActionRate = firstCorrectActionRate;
        this.invalidCallRate = invalidCallRate;
        this.averageRoundTrips = averageRoundTrips;
        this.queryAnswerFidelity = queryAnswerFidelity;
        this.boundaryConfusionRate = boundaryConfusionRate;
        this.resourceHitRate = resourceHitRate;
        this.recoveryRate = recoveryRate;
        this.dimensionScores = List.copyOf(dimensionScores);
        this.scenarioResults = List.copyOf(scenarioResults);
    }
    
    String suiteId() {
        return suiteId;
    }
    
    String runId() {
        return runId;
    }
    
    double taskSuccessRate() {
        return taskSuccessRate;
    }
    
    double firstCorrectActionRate() {
        return firstCorrectActionRate;
    }
    
    double invalidCallRate() {
        return invalidCallRate;
    }
    
    double averageRoundTrips() {
        return averageRoundTrips;
    }
    
    double queryAnswerFidelity() {
        return queryAnswerFidelity;
    }
    
    double boundaryConfusionRate() {
        return boundaryConfusionRate;
    }
    
    double resourceHitRate() {
        return resourceHitRate;
    }
    
    double recoveryRate() {
        return recoveryRate;
    }
    
    List<LLMUsabilityDimensionScore> dimensionScores() {
        return dimensionScores;
    }
    
    List<LLMUsabilityScenarioResult> scenarioResults() {
        return scenarioResults;
    }
}
