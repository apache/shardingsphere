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

package org.apache.shardingsphere.mcp.support.resource;

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPUriTemplateTest {
    
    @Test
    void assertGetVariableNames() {
        List<String> actualVariableNames = new MCPUriTemplate("shardingsphere://databases/{database}/schemas/{schema}").getVariableNames();
        assertThat(actualVariableNames, is(List.of("database", "schema")));
    }
    
    @Test
    void assertExpandIfComplete() {
        Optional<String> actualUri = new MCPUriTemplate("shardingsphere://databases/{database}/schemas/{schema}")
                .expandIfComplete(new MCPResourceURIVariables(Map.of("database", "logic_db", "schema", "public")));
        assertThat(actualUri, is(Optional.of("shardingsphere://databases/logic_db/schemas/public")));
    }
    
    @Test
    void assertExpandIfCompleteWithMissingVariable() {
        Optional<String> actualUri = new MCPUriTemplate("shardingsphere://databases/{database}/schemas/{schema}")
                .expandIfComplete(new MCPResourceURIVariables(Map.of("database", "logic_db")));
        assertThat(actualUri, is(Optional.empty()));
    }
}
