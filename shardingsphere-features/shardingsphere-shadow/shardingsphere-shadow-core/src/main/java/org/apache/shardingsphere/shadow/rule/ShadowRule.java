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

package org.apache.shardingsphere.shadow.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.identifier.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Databases shadow rule.
 */
@Getter
public final class ShadowRule implements FeatureRule, SchemaRule, DataSourceContainedRule {
    
    private final Map<String, String> shadowMappings;
    
    private final String column;
    
    public ShadowRule(final ShadowRuleConfiguration shadowRuleConfig) {
        column = shadowRuleConfig.getColumn();
        shadowMappings = new HashMap<>(shadowRuleConfig.getShadowDataSourceNames().size());
        for (int i = 0; i < shadowRuleConfig.getSourceDataSourceNames().size(); i++) {
            shadowMappings.put(shadowRuleConfig.getSourceDataSourceNames().get(i), shadowRuleConfig.getShadowDataSourceNames().get(i));
        }
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(shadowMappings.size());
        for (Entry<String, String> entry : shadowMappings.entrySet()) {
            result.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        return result;
    }
}
