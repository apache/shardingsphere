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

import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptWorkflowStateTest {
    
    @Test
    void assertCopy() {
        DerivedColumnPlan plan = new DerivedColumnPlan();
        plan.setLogicalColumn("phone");
        plan.setCipherColumnName("phone_cipher");
        plan.setCipherColumnRequired(true);
        plan.getNameCollisions().add(Map.of("original_name", "phone_cipher", "resolved_name", "phone_cipher_1"));
        EncryptWorkflowState state = new EncryptWorkflowState(List.of(Map.of("logic_column", "email")), List.of(Map.of("logic_column", "phone")));
        state.setDerivedColumnPlan(plan);
        EncryptWorkflowState actual = state.copy();
        state.getExpectedRules().getFirst().put("logic_column", "mutated");
        state.getDerivedColumnPlan().setCipherColumnName("mutated_cipher");
        assertThat(actual.getBeforeRules().getFirst().get("logic_column"), is("email"));
        assertThat(actual.getExpectedRules().getFirst().get("logic_column"), is("phone"));
        assertThat(actual.getDerivedColumnPlan().getCipherColumnName(), is("phone_cipher"));
        assertThat(actual.getDerivedColumnPlan().getNameCollisions().getFirst().get("resolved_name"), is("phone_cipher_1"));
    }
    
    @Test
    void assertGetAlgorithmProperties() {
        assertTrue(new EncryptWorkflowState().getAlgorithmProperties("primary").isEmpty());
    }
}
