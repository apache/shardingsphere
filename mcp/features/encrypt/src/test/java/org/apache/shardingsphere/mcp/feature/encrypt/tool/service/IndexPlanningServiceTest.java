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

import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexPlanningServiceTest {
    
    private final IndexPlanningService service = new IndexPlanningService();
    
    @Test
    void assertPlanIndexesWithRequiredDerivedColumns() {
        List<IndexPlan> actual = service.planIndexes("orders", createDerivedColumnPlan(true, true), new LinkedHashSet<>());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_phone_assisted_query"));
        assertThat(actual.get(1).getIndexName(), is("idx_orders_phone_like_query"));
    }
    
    @Test
    void assertPlanIndexesWithIndexNameCollision() {
        Set<String> existingIndexes = new LinkedHashSet<>(List.of("idx_orders_phone_assisted_query"));
        List<IndexPlan> actual = service.planIndexes("orders", createDerivedColumnPlan(true, false), existingIndexes);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_phone_assisted_query_1"));
    }
    
    @Test
    void assertPlanIndexesWithoutOptionalColumns() {
        List<IndexPlan> actual = service.planIndexes("orders", createDerivedColumnPlan(false, false), new LinkedHashSet<>());
        assertTrue(actual.isEmpty());
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final boolean assistedQuery, final boolean likeQuery) {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setAssistedQueryColumnRequired(assistedQuery);
        result.setAssistedQueryColumnName("phone_assisted_query");
        result.setLikeQueryColumnRequired(likeQuery);
        result.setLikeQueryColumnName("phone_like_query");
        return result;
    }
}
