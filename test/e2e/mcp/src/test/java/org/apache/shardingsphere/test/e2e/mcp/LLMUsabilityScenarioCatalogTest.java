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

package org.apache.shardingsphere.test.e2e.mcp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class LLMUsabilityScenarioCatalogTest {
    
    @Test
    void assertCreateMinimalBaseline() {
        List<LLMUsabilityScenario> actual = new LLMUsabilityScenarioCatalog().createMinimalBaseline("h2", "logic_db", "public",
                "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2);
        
        assertThat(actual, hasSize(6));
        assertThat(actual.get(0).dimension(), is(LLMUsabilityDimension.RESOURCE));
        assertThat(actual.get(4).dimension(), is(LLMUsabilityDimension.RECOVERY));
        assertThat(actual.get(1).expectedResourceUris().get(0), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
    }
}
