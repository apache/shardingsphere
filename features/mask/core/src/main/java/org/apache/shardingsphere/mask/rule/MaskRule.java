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

package org.apache.shardingsphere.mask.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.factory.MaskAlgorithmFactory;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mask rule.
 */
@SuppressWarnings("rawtypes")
public final class MaskRule implements DatabaseRule, TableContainedRule {
    
    @Getter
    private final RuleConfiguration configuration;
    
    private final Map<String, MaskAlgorithm> maskAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, MaskTable> tables = new LinkedHashMap<>();
    
    public MaskRule(final MaskRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        ruleConfig.getMaskAlgorithms().forEach((key, value) -> maskAlgorithms.put(key, MaskAlgorithmFactory.newInstance(value)));
        ruleConfig.getTables().forEach(each -> tables.put(each.getName().toLowerCase(), new MaskTable(each)));
    }
    
    @Override
    public Collection<String> getTables() {
        return tables.keySet();
    }
    
    @Override
    public String getType() {
        return MaskRule.class.getSimpleName();
    }
}
