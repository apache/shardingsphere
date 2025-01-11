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

package org.apache.shardingsphere.mode.metadata.persist.fixture;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

public final class NoTupleYamlRuleConfigurationSwapperFixture implements YamlRuleConfigurationSwapper<NoTupleYamlRuleConfigurationFixture, NoTupleRuleConfigurationFixture> {
    
    @Override
    public NoTupleYamlRuleConfigurationFixture swapToYamlConfiguration(final NoTupleRuleConfigurationFixture data) {
        NoTupleYamlRuleConfigurationFixture result = new NoTupleYamlRuleConfigurationFixture();
        result.setName(data.getName());
        return result;
    }
    
    @Override
    public NoTupleRuleConfigurationFixture swapToObject(final NoTupleYamlRuleConfigurationFixture yamlConfig) {
        return new NoTupleRuleConfigurationFixture(yamlConfig.getName());
    }
    
    @Override
    public String getRuleTagName() {
        return "NO_TUPLE.FIXTURE";
    }
    
    @Override
    public int getOrder() {
        return -401;
    }
    
    @Override
    public Class<NoTupleRuleConfigurationFixture> getTypeClass() {
        return NoTupleRuleConfigurationFixture.class;
    }
}
