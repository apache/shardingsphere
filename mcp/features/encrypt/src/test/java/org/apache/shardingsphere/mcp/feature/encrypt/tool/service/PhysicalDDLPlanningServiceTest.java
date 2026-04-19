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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalDDLPlanningServiceTest {
    
    private final PhysicalDDLPlanningService service = new PhysicalDDLPlanningService();
    
    @Test
    void assertPlanAddColumnArtifactsWithDefaultDefinition() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("orders", createDerivedColumnPlan(true, true), new LinkedHashSet<>(), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is(
                "ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(4000), ADD COLUMN phone_assisted_query VARCHAR(4000), ADD COLUMN phone_like_query VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsWithExistingColumns() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("orders", createDerivedColumnPlan(true, false),
                new LinkedHashSet<>(List.of("phone_cipher", "phone_assisted_query")), "");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertPlanAddColumnArtifactsWithCustomDefinition() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("orders", createDerivedColumnPlan(false, true), new LinkedHashSet<>(), "VARCHAR(64)");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(64), ADD COLUMN phone_like_query VARCHAR(64)"));
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final boolean assistedQuery, final boolean likeQuery) {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setCipherColumnRequired(true);
        result.setCipherColumnName("phone_cipher");
        result.setAssistedQueryColumnRequired(assistedQuery);
        result.setAssistedQueryColumnName("phone_assisted_query");
        result.setLikeQueryColumnRequired(likeQuery);
        result.setLikeQueryColumnName("phone_like_query");
        return result;
    }
}
