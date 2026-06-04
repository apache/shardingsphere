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

package org.apache.shardingsphere.test.e2e.mcp.support.assertion;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPBaselineContractAssertionsTest {
    
    @Test
    void assertMatchesNormalizedBaselineContract() {
        assertDoesNotThrow(() -> MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(
                "baseline-contract/assertion/normalized-plan-id.yaml",
                Map.of("plan_id", "plan-1", "nested", List.of(Map.of("plan_id", "server_generated"), Map.of("plan_id", "plan-2")))));
    }
    
    @Test
    void assertMatchesNormalizedBaselineContractWithMissingResource() {
        assertThrows(AssertionError.class, () -> MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(
                "baseline-contract/assertion/missing.yaml", Map.of()));
    }
}
