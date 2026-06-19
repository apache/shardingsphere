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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptWorkflowStateTest {
    
    @Test
    void assertCopy() {
        EncryptWorkflowState state = new EncryptWorkflowState(List.of(Map.of("logic_column", "email")), List.of(Map.of("logic_column", "phone")));
        EncryptWorkflowState actual = state.copy();
        state.getExpectedRules().getFirst().put("logic_column", "mutated");
        assertThat(actual.getBeforeRules().getFirst().get("logic_column"), is("email"));
        assertThat(actual.getExpectedRules().getFirst().get("logic_column"), is("phone"));
    }
    
    @Test
    void assertGetAlgorithmProperties() {
        assertTrue(new EncryptWorkflowState().getAlgorithmProperties("primary").isEmpty());
    }
}
