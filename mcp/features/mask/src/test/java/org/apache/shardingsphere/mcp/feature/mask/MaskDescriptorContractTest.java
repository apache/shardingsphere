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

package org.apache.shardingsphere.mcp.feature.mask;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MaskDescriptorContractTest {
    
    @Test
    void assertPlanToolOperationTypesExposeOnlySupportedLifecycleActions() {
        Map<?, ?> actualOperationType = MCPToolDescriptorValidationUtils.findToolInputProperty(findToolDescriptor(), "operation_type").orElseThrow();
        assertThat(actualOperationType.get("enum"), is(List.of("create", "drop")));
    }
    
    private MCPToolDescriptor findToolDescriptor() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        return catalog.getProtocolDescriptors().getToolDescriptors().stream()
                .filter(each -> MaskFeatureDefinition.PLAN_TOOL_NAME.equals(each.getName())).findFirst().orElseThrow();
    }
}
