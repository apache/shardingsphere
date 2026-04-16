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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;

import java.util.List;
import java.util.Map;

abstract class AbstractProductionRuntimeE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    protected final List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("items"));
    }
    
    protected final List<Map<String, Object>> getResources(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("resources"));
    }
    
    protected final List<String> getNestedNames(final Map<String, Object> item, final String nestedKey, final String nameKey) {
        return ((List<?>) item.get(nestedKey)).stream().map(each -> String.valueOf(((Map<?, ?>) each).get(nameKey))).toList();
    }
}
