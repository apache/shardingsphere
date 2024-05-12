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

package org.apache.shardingsphere.readwritesplitting.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.tuple.annotation.RepositoryTupleField;
import org.apache.shardingsphere.mode.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.mode.tuple.annotation.RepositoryTupleField.Type;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Readwrite-splitting rule configuration for YAML.
 */
@RepositoryTupleEntity("readwrite_splitting")
@Getter
@Setter
public final class YamlReadwriteSplittingRuleConfiguration implements YamlRuleConfiguration {
    
    @RepositoryTupleField(type = Type.DATA_SOURCE)
    private Map<String, YamlReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroups = new LinkedHashMap<>();
    
    @RepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
