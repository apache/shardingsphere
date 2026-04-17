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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhysicalDDLPlanningServiceTest {
    
    @Test
    void assertPlanAddColumnArtifactsUsesDefaultDefinitionAndSkipsExistingColumns() {
        PhysicalDDLPlanningService service = new PhysicalDDLPlanningService();
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan();
        List<?> actual = service.planAddColumnArtifacts("orders", derivedColumnPlan, new LinkedHashSet<>(List.of("status_cipher")), "");
        assertThat(actual.size(), is(1));
        assertThat(((DDLArtifact) actual.get(0)).getSql(),
                is("ALTER TABLE orders ADD COLUMN status_assisted_query VARCHAR(4000), ADD COLUMN status_like_query VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsReturnsEmptyWhenColumnsAlreadyExist() {
        PhysicalDDLPlanningService service = new PhysicalDDLPlanningService();
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan();
        List<?> actual = service.planAddColumnArtifacts("orders", derivedColumnPlan,
                new LinkedHashSet<>(List.of("status_cipher", "status_assisted_query", "status_like_query")), "VARCHAR(32)");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    void assertPlanAddColumnArtifactsRejectsUnsafeIdentifier() {
        PhysicalDDLPlanningService service = new PhysicalDDLPlanningService();
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan();
        Exception actual = assertThrows(RuntimeException.class, () -> service.planAddColumnArtifacts("bad table", derivedColumnPlan, new LinkedHashSet<>(), "VARCHAR(32)"));
        assertThat(actual.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
    
    private DerivedColumnPlan createDerivedColumnPlan() {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setCipherColumnRequired(true);
        result.setCipherColumnName("status_cipher");
        result.setAssistedQueryColumnRequired(true);
        result.setAssistedQueryColumnName("status_assisted_query");
        result.setLikeQueryColumnRequired(true);
        result.setLikeQueryColumnName("status_like_query");
        return result;
    }
}
