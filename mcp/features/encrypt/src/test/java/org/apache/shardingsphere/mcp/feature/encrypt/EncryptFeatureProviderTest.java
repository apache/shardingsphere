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

package org.apache.shardingsphere.mcp.feature.encrypt;

import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowValidationService;
import org.apache.shardingsphere.mcp.feature.spi.MCPWorkflowToolContribution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EncryptFeatureProviderTest {
    
    @Test
    void assertGetToolHandlers() {
        EncryptFeatureProvider featureProvider = new EncryptFeatureProvider();
        assertThat(featureProvider.getToolHandlers().stream().map(each -> each.getToolDescriptor().getName()).toList(), is(List.of("plan_encrypt_rule")));
    }
    
    @Test
    void assertGetWorkflowToolContributions() {
        EncryptFeatureProvider featureProvider = new EncryptFeatureProvider();
        assertThat(featureProvider.getWorkflowToolContributions().stream().map(MCPWorkflowToolContribution::getApplyToolName).toList(), is(List.of("apply_encrypt_rule")));
        assertThat(featureProvider.getWorkflowToolContributions().stream().map(MCPWorkflowToolContribution::getValidateToolName).toList(), is(List.of("validate_encrypt_rule")));
        assertThat(featureProvider.getWorkflowToolContributions().iterator().next().getWorkflowValidationHandler(),
                org.hamcrest.Matchers.instanceOf(EncryptWorkflowValidationService.class));
    }
}
