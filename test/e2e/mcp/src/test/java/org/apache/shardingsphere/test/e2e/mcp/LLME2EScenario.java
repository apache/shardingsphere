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

final class LLME2EScenario {
    
    private final String scenarioId;
    
    private final String systemPrompt;
    
    private final String userPrompt;
    
    private final LLMStructuredAnswer expectedAnswer;
    
    private final List<String> allowedToolNames;
    
    private final List<String> requiredToolNames;
    
    LLME2EScenario(final String scenarioId, final String systemPrompt, final String userPrompt,
                   final LLMStructuredAnswer expectedAnswer, final List<String> allowedToolNames,
                   final List<String> requiredToolNames) {
        this.scenarioId = scenarioId;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.expectedAnswer = expectedAnswer;
        this.allowedToolNames = List.copyOf(allowedToolNames);
        this.requiredToolNames = List.copyOf(requiredToolNames);
    }
    
    String scenarioId() {
        return scenarioId;
    }
    
    String systemPrompt() {
        return systemPrompt;
    }
    
    String userPrompt() {
        return userPrompt;
    }
    
    LLMStructuredAnswer expectedAnswer() {
        return expectedAnswer;
    }
    
    List<String> allowedToolNames() {
        return allowedToolNames;
    }
    
    List<String> requiredToolNames() {
        return requiredToolNames;
    }
}
