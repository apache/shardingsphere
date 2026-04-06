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

final class LLMUsabilityComparisonResult {
    
    private final String baselineRunId;
    
    private final String candidateRunId;
    
    private final boolean regressionDetected;
    
    private final List<String> regressedMetrics;
    
    private final List<String> improvedMetrics;
    
    private final List<String> stableMetrics;
    
    private final String decision;
    
    LLMUsabilityComparisonResult(final String baselineRunId, final String candidateRunId, final boolean regressionDetected,
                                 final List<String> regressedMetrics, final List<String> improvedMetrics,
                                 final List<String> stableMetrics, final String decision) {
        this.baselineRunId = baselineRunId;
        this.candidateRunId = candidateRunId;
        this.regressionDetected = regressionDetected;
        this.regressedMetrics = List.copyOf(regressedMetrics);
        this.improvedMetrics = List.copyOf(improvedMetrics);
        this.stableMetrics = List.copyOf(stableMetrics);
        this.decision = decision;
    }
    
    String baselineRunId() {
        return baselineRunId;
    }
    
    String candidateRunId() {
        return candidateRunId;
    }
    
    boolean regressionDetected() {
        return regressionDetected;
    }
    
    List<String> regressedMetrics() {
        return regressedMetrics;
    }
    
    List<String> improvedMetrics() {
        return improvedMetrics;
    }
    
    List<String> stableMetrics() {
        return stableMetrics;
    }
    
    String decision() {
        return decision;
    }
}
