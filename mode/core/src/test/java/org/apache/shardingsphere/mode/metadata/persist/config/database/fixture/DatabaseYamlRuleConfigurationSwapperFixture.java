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

package org.apache.shardingsphere.mode.metadata.persist.config.database.fixture;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

public final class DatabaseYamlRuleConfigurationSwapperFixture implements YamlRuleConfigurationSwapper<DatabaseYamlRuleConfigurationFixture, DatabaseRuleConfigurationFixture> {
    
    @Override
    public DatabaseYamlRuleConfigurationFixture swapToYamlConfiguration(final DatabaseRuleConfigurationFixture data) {
        DatabaseYamlRuleConfigurationFixture result = new DatabaseYamlRuleConfigurationFixture();
        result.setUnique(data.getUnique());
        result.setNamed(data.getNamed());
        return result;
    }
    
    @Override
    public DatabaseRuleConfigurationFixture swapToObject(final DatabaseYamlRuleConfigurationFixture yamlConfig) {
        return new DatabaseRuleConfigurationFixture(yamlConfig.getUnique(), yamlConfig.getNamed());
    }
    
    @Override
    public String getRuleTagName() {
        return "DATABASE.RULE.FIXTURE";
    }
    
    @Override
    public int getOrder() {
        return -410;
    }
    
    @Override
    public Class<DatabaseRuleConfigurationFixture> getTypeClass() {
        return DatabaseRuleConfigurationFixture.class;
    }
}
