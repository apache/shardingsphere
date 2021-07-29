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

package org.apache.shardingsphere.infra.yaml.config.swapper.persist;

import org.apache.shardingsphere.infra.constant.DistMetaDataPersistOrder;
import org.apache.shardingsphere.infra.rule.persist.DistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.persist.YamlDistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;

/**
 * Dist meta data persist rule configuration YAML swapper.
 */
public final class DistMetaDataPersistRuleConfigurationYamlSwapper implements 
        YamlRuleConfigurationSwapper<YamlDistMetaDataPersistRuleConfiguration, DistMetaDataPersistRuleConfiguration> {
    
    @Override
    public int getOrder() {
        return DistMetaDataPersistOrder.ORDER;
    }
    
    @Override
    public Class<DistMetaDataPersistRuleConfiguration> getTypeClass() {
        return DistMetaDataPersistRuleConfiguration.class;
    }
    
    @Override
    public YamlDistMetaDataPersistRuleConfiguration swapToYamlConfiguration(final DistMetaDataPersistRuleConfiguration data) {
        YamlDistMetaDataPersistRuleConfiguration result = new YamlDistMetaDataPersistRuleConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProps());
        return result;
    }
    
    @Override
    public DistMetaDataPersistRuleConfiguration swapToObject(final YamlDistMetaDataPersistRuleConfiguration yamlConfig) {
        return new DistMetaDataPersistRuleConfiguration(yamlConfig.getType(), yamlConfig.getProps());
    }
    
    @Override
    public String getRuleTagName() {
        return "DIST_METADATA_PERSIST";
    }
}
