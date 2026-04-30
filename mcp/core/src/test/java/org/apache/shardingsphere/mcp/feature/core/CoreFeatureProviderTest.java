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

package org.apache.shardingsphere.mcp.feature.core;

import org.apache.shardingsphere.mcp.feature.spi.MCPContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectToolContribution;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreFeatureProviderTest {
    
    @Test
    void assertGetContributionsWithToolContributions() {
        Collection<MCPContribution> contributions = new CoreFeatureProvider().getContributions();
        List<String> actual = contributions.stream().filter(MCPDirectToolContribution.class::isInstance).map(MCPDirectToolContribution.class::cast)
                .map(each -> each.getToolDescriptor().getName()).toList();
        assertThat(actual, is(List.of("search_metadata", "execute_query")));
    }
    
    @Test
    void assertGetContributionsWithResourceContributions() {
        Collection<MCPContribution> contributions = new CoreFeatureProvider().getContributions();
        List<String> actual = contributions.stream().filter(MCPDirectResourceContribution.class::isInstance).map(MCPDirectResourceContribution.class::cast)
                .map(MCPDirectResourceContribution::getUriPattern).toList();
        assertThat(actual.size(), is(18));
        assertTrue(actual.contains("shardingsphere://capabilities"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    }
}
