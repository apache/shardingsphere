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

final class LLMUsabilityRegressionBudget {
    
    private final double maxTaskSuccessRateDrop;
    
    private final double maxFirstCorrectActionRateDrop;
    
    private final double maxInvalidCallRateIncrease;
    
    private final double maxAverageRoundTripsIncrease;
    
    private final double maxQueryAnswerFidelityDrop;
    
    private final double maxBoundaryConfusionRateIncrease;
    
    LLMUsabilityRegressionBudget(final double maxTaskSuccessRateDrop, final double maxFirstCorrectActionRateDrop,
                                 final double maxInvalidCallRateIncrease, final double maxAverageRoundTripsIncrease,
                                 final double maxQueryAnswerFidelityDrop, final double maxBoundaryConfusionRateIncrease) {
        this.maxTaskSuccessRateDrop = maxTaskSuccessRateDrop;
        this.maxFirstCorrectActionRateDrop = maxFirstCorrectActionRateDrop;
        this.maxInvalidCallRateIncrease = maxInvalidCallRateIncrease;
        this.maxAverageRoundTripsIncrease = maxAverageRoundTripsIncrease;
        this.maxQueryAnswerFidelityDrop = maxQueryAnswerFidelityDrop;
        this.maxBoundaryConfusionRateIncrease = maxBoundaryConfusionRateIncrease;
    }
    
    static LLMUsabilityRegressionBudget createMinimalBaselineBudget() {
        return new LLMUsabilityRegressionBudget(0.05D, 0.08D, 0.05D, 0.50D, 0.05D, 0.05D);
    }
    
    double maxTaskSuccessRateDrop() {
        return maxTaskSuccessRateDrop;
    }
    
    double maxFirstCorrectActionRateDrop() {
        return maxFirstCorrectActionRateDrop;
    }
    
    double maxInvalidCallRateIncrease() {
        return maxInvalidCallRateIncrease;
    }
    
    double maxAverageRoundTripsIncrease() {
        return maxAverageRoundTripsIncrease;
    }
    
    double maxQueryAnswerFidelityDrop() {
        return maxQueryAnswerFidelityDrop;
    }
    
    double maxBoundaryConfusionRateIncrease() {
        return maxBoundaryConfusionRateIncrease;
    }
}
