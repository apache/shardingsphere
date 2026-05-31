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

import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
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
        List<IndexPlan> actual = service.planIndexes("MySQL", "orders", createDerivedColumnPlan(true, true), new LinkedHashSet<>());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_phone_assisted_query"));
        assertThat(actual.get(1).getIndexName(), is("idx_orders_phone_like_query"));
    }
    
    @Test
    void assertPlanIndexesWithIndexNameCollision() {
        Set<String> existingIndexes = new LinkedHashSet<>(List.of("idx_orders_phone_assisted_query"));
        List<IndexPlan> actual = service.planIndexes("MySQL", "orders", createDerivedColumnPlan(true, false), existingIndexes);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_phone_assisted_query_1"));
    }
    
    @Test
    void assertPlanIndexesMatchesCaseInsensitiveIndexNameCollision() {
        Set<String> existingIndexes = new LinkedHashSet<>(List.of("IDX_ORDERS_PHONE_ASSISTED_QUERY"));
        List<IndexPlan> actual = service.planIndexes("MySQL", "orders", createDerivedColumnPlan(true, false), existingIndexes);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_phone_assisted_query_1"));
    }
    
    @Test
    void assertPlanIndexesKeepsCaseSensitiveIndexNamesDistinct() {
        Set<String> existingIndexes = new LinkedHashSet<>(List.of("IDX_ORDERS_PHONE_ASSISTED_QUERY"));
        List<IndexPlan> actual = service.planIndexes("PostgreSQL", "orders", createDerivedColumnPlan(true, false), existingIndexes);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getIndexName(), is("idx_orders_phone_assisted_query"));
    }
    
    @Test
    void assertPlanIndexesFormatsSpecialCharacterIdentifiers() {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(true, false);
        derivedColumnPlan.setAssistedQueryColumnName("phone assisted");
        List<IndexPlan> actual = service.planIndexes("MySQL", "order detail", derivedColumnPlan, new LinkedHashSet<>());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("CREATE INDEX `idx_order detail_phone assisted` ON `order detail` (`phone assisted`)"));
    }
    
    @Test
    void assertPlanIndexesFormatsReservedIdentifiers() {
        List<IndexPlan> actual = service.planIndexes("MySQL", "key", createDerivedColumnPlan(true, false), new LinkedHashSet<>());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("CREATE INDEX idx_key_phone_assisted_query ON `key` (phone_assisted_query)"));
    }
    
    @Test
    void assertPlanIndexesFormatsPostgreSQLIdentifiers() {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(true, false);
        derivedColumnPlan.setAssistedQueryColumnName("phone assisted");
        List<IndexPlan> actual = service.planIndexes("PostgreSQL", "order detail", derivedColumnPlan, new LinkedHashSet<>());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("CREATE INDEX \"idx_order detail_phone assisted\" ON \"order detail\" (\"phone assisted\")"));
    }
    
    @Test
    void assertPlanIndexesWithoutOptionalColumns() {
        List<IndexPlan> actual = service.planIndexes("MySQL", "orders", createDerivedColumnPlan(false, false), new LinkedHashSet<>());
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
