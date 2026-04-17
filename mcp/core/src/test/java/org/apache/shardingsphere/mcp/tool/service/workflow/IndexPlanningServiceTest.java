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

import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndexPlanningServiceTest {
    
    @Test
    void assertPlanIndexesCreatesUniqueIndexNames() {
        IndexPlanningService service = new IndexPlanningService();
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan();
        List<IndexPlan> actual = service.planIndexes("orders", derivedColumnPlan, new LinkedHashSet<>(List.of("idx_orders_status_assisted_query")));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_status_assisted_query_1"));
        assertThat(actual.get(1).getIndexName(), is("idx_orders_status_like_query"));
    }
    
    @Test
    void assertPlanIndexesReturnsEmptyWhenDerivedColumnsAreNotRequired() {
        IndexPlanningService service = new IndexPlanningService();
        DerivedColumnPlan derivedColumnPlan = new DerivedColumnPlan();
        List<IndexPlan> actual = service.planIndexes("orders", derivedColumnPlan, new LinkedHashSet<>());
        assertThat(actual.size(), is(0));
    }
    
    @Test
    void assertPlanIndexesRejectsUnsafeIdentifier() {
        IndexPlanningService service = new IndexPlanningService();
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan();
        Exception actual = assertThrows(RuntimeException.class, () -> service.planIndexes("bad table", derivedColumnPlan, new LinkedHashSet<>()));
        assertThat(actual.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
    
    private DerivedColumnPlan createDerivedColumnPlan() {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setAssistedQueryColumnRequired(true);
        result.setAssistedQueryColumnName("status_assisted_query");
        result.setLikeQueryColumnRequired(true);
        result.setLikeQueryColumnName("status_like_query");
        return result;
    }
}
