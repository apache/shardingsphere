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

package org.apache.shardingsphere.shardingproxy.config.yaml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.shadow.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.config.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Rule configuration for YAML.
 */
@Getter
@Setter
public final class YamlProxyRuleConfiguration implements YamlConfiguration {
    
    private String schemaName;
    
    private Map<String, YamlDataSourceParameter> dataSources = new HashMap<>();
    
    private YamlDataSourceParameter dataSource;
    
    private YamlShardingRuleConfiguration shardingRule;
    
    private YamlMasterSlaveRuleConfiguration masterSlaveRule;
    
    private YamlEncryptRuleConfiguration encryptRule;
    
    private YamlShadowRuleConfiguration shadowRule;
}
