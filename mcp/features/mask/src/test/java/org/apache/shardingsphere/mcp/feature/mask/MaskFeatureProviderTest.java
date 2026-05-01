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

import org.apache.shardingsphere.mcp.feature.spi.MCPContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectResourceContribution;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowValidationService;
import org.apache.shardingsphere.mcp.workflow.spi.MCPWorkflowToolContribution;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.is;

class MaskFeatureProviderTest {
    
    @Test
    void assertGetContributionsWithResourceContributions() {
        Collection<MCPContribution> contributions = new MaskFeatureProvider().getContributions();
        List<String> actual = contributions.stream().filter(MCPDirectResourceContribution.class::isInstance).map(MCPDirectResourceContribution.class::cast)
                .map(MCPDirectResourceContribution::getUriPattern).toList();
        assertThat(actual, is(List.of(
                "shardingsphere://features/mask/algorithms",
                "shardingsphere://features/mask/databases/{database}/rules",
                "shardingsphere://features/mask/databases/{database}/tables/{table}/rules")));
    }
    
    @Test
    void assertGetContributionsWithWorkflowToolContribution() {
        Collection<MCPContribution> contributions = new MaskFeatureProvider().getContributions();
        MCPWorkflowToolContribution actual = contributions.stream().filter(MCPWorkflowToolContribution.class::isInstance).map(MCPWorkflowToolContribution.class::cast).findFirst().orElseThrow();
        assertThat(actual.getPlanningToolDescriptor().getName(), is("plan_mask_rule"));
        assertThat(actual.getApplyToolName(), is("apply_mask_rule"));
        assertThat(actual.getValidateToolName(), is("validate_mask_rule"));
        assertThat(actual.getWorkflowValidationHandler(), isA(MaskWorkflowValidationService.class));
    }
}
