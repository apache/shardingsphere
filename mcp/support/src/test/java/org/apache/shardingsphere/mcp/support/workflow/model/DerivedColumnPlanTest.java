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

package org.apache.shardingsphere.mcp.support.workflow.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DerivedColumnPlanTest {
    
    @Test
    void assertToMap() {
        DerivedColumnPlan plan = new DerivedColumnPlan();
        plan.setLogicalColumn(" phone ");
        plan.setCipherColumnName(" phone_cipher ");
        plan.setAssistedQueryColumnName(" phone_assisted ");
        plan.setLikeQueryColumnName(" phone_like ");
        plan.setCipherColumnRequired(true);
        plan.setAssistedQueryColumnRequired(true);
        plan.getNameCollisions().add(Map.of("original_name", "phone_cipher", "resolved_name", "phone_cipher_1"));
        Map<String, Object> actual = plan.toMap();
        assertThat(actual.get("logical_column"), is("phone"));
        assertThat(actual.get("cipher_column_name"), is("phone_cipher"));
        assertThat(actual.get("assisted_query_column_name"), is("phone_assisted"));
        assertThat(actual.get("like_query_column_name"), is("phone_like"));
        assertTrue((boolean) actual.get("cipher_column_required"));
        assertTrue((boolean) actual.get("assisted_query_column_required"));
        assertFalse((boolean) actual.get("like_query_column_required"));
    }
}
