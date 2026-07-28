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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact;

import java.util.List;
import java.util.Map;

final class LLME2ERuntimeEvidenceValidator {
    
    private static final List<String> REQUIRED_SCORE_EVIDENCE_KEYS = List.of(
            "runtimeMode", "dockerOwned", "provider", "serverRuntime", "serverImage", "serverImageId", "baseServerImage", "baseServerImageDigest",
            "modelRepository", "modelReference", "servedModelId",
            "modelQuantization", "modelRevision", "modelFileName", "modelSha256", "modelPackaging", "contextWindowTokens", "baseUrlOwnedByTest");
    
    void validate(final Map<String, Object> runtimeEvidence) {
        if (!Boolean.TRUE.equals(runtimeEvidence.get("scoreClosing"))) {
            return;
        }
        for (String each : REQUIRED_SCORE_EVIDENCE_KEYS) {
            if (isMissingEvidenceValue(runtimeEvidence.get(each))) {
                throw new IllegalStateException(String.format("Missing score-closing LLM runtime evidence field `%s`.", each));
            }
        }
        if (!Boolean.TRUE.equals(runtimeEvidence.get("dockerOwned")) || !Boolean.TRUE.equals(runtimeEvidence.get("baseUrlOwnedByTest"))) {
            throw new IllegalStateException("Score-closing LLM runtime evidence must be Docker-owned and test-owned.");
        }
    }
    
    private boolean isMissingEvidenceValue(final Object value) {
        return null == value || value instanceof String && ((String) value).isBlank();
    }
}
