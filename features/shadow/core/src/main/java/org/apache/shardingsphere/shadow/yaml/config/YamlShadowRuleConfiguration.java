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

package org.apache.shardingsphere.shadow.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RepositoryTupleField;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowRuleNodePathProvider;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shadow rule configuration.
 */
@RepositoryTupleEntity
@Getter
@Setter
public final class YamlShadowRuleConfiguration implements YamlRuleConfiguration {
    
    @RepositoryTupleField(value = ShadowRuleNodePathProvider.DATA_SOURCES, order = 100)
    private Map<String, YamlShadowDataSourceConfiguration> dataSources = new LinkedHashMap<>();
    
    @RepositoryTupleField(value = ShadowRuleNodePathProvider.TABLES, order = 101)
    private Map<String, YamlShadowTableConfiguration> tables = new LinkedHashMap<>();
    
    @RepositoryTupleField(value = ShadowRuleNodePathProvider.ALGORITHMS, order = 0)
    private Map<String, YamlAlgorithmConfiguration> shadowAlgorithms = new LinkedHashMap<>();
    
    @RepositoryTupleField(value = ShadowRuleNodePathProvider.DEFAULT_ALGORITHM, order = 1)
    private String defaultShadowAlgorithmName;
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationType() {
        return ShadowRuleConfiguration.class;
    }
}
