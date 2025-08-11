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

package org.apache.shardingsphere.test.infra.fixture.yaml;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRuleConfiguration;

/**
 * Mocked YAML rule configuration swapper.
 */
public final class MockedYamlRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<MockedYamlRuleConfiguration, MockedRuleConfiguration> {
    
    @Override
    public Class<MockedRuleConfiguration> getTypeClass() {
        return MockedRuleConfiguration.class;
    }
    
    @Override
    public MockedYamlRuleConfiguration swapToYamlConfiguration(final MockedRuleConfiguration data) {
        MockedYamlRuleConfiguration result = new MockedYamlRuleConfiguration();
        result.setUnique(data.getUnique());
        result.setNamed(data.getNamed());
        return result;
    }
    
    @Override
    public MockedRuleConfiguration swapToObject(final MockedYamlRuleConfiguration yamlConfig) {
        return new MockedRuleConfiguration(yamlConfig.getUnique(), yamlConfig.getNamed());
    }
    
    @Override
    public String getRuleTagName() {
        return "FIXTURE";
    }
    
    @Override
    public int getOrder() {
        return -10000;
    }
}
