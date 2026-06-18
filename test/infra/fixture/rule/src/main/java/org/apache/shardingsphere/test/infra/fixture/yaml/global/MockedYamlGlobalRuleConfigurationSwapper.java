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

package org.apache.shardingsphere.test.infra.fixture.yaml.global;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.test.infra.fixture.rule.global.MockedGlobalRuleConfiguration;

/**
 * Mocked YAML global rule configuration swapper.
 */
public final class MockedYamlGlobalRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<MockedYamlGlobalRuleConfiguration, MockedGlobalRuleConfiguration> {
    
    @Override
    public Class<MockedGlobalRuleConfiguration> getTypeClass() {
        return MockedGlobalRuleConfiguration.class;
    }
    
    @Override
    public MockedYamlGlobalRuleConfiguration swapToYamlConfiguration(final MockedGlobalRuleConfiguration data) {
        MockedYamlGlobalRuleConfiguration result = new MockedYamlGlobalRuleConfiguration();
        result.setName(data.getName());
        return result;
    }
    
    @Override
    public MockedGlobalRuleConfiguration swapToObject(final MockedYamlGlobalRuleConfiguration yamlConfig) {
        return new MockedGlobalRuleConfiguration(yamlConfig.getName());
    }
    
    @Override
    public String getRuleTagName() {
        return "GLOBAL_FIXTURE";
    }
    
    @Override
    public int getOrder() {
        return -20000;
    }
}
