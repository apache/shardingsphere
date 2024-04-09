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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.attribute.MaskTableMapperRuleAttribute;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mask rule.
 */
public final class MaskRule implements DatabaseRule {
    
    @Getter
    private final MaskRuleConfiguration configuration;
    
    private final Map<String, MaskTable> tables;
    
    @Getter
    private final RuleAttributes attributes;
    
    @SuppressWarnings("unchecked")
    public MaskRule(final MaskRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        Map<String, MaskAlgorithm<?, ?>> maskAlgorithms = ruleConfig.getMaskAlgorithms().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> TypedSPILoader.getService(MaskAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps())));
        tables = ruleConfig.getTables().stream()
                .collect(Collectors.toMap(each -> each.getName().toLowerCase(), each -> new MaskTable(each, maskAlgorithms), (oldValue, currentValue) -> oldValue, CaseInsensitiveMap::new));
        attributes = new RuleAttributes(new MaskTableMapperRuleAttribute(ruleConfig.getTables()));
    }
    
    /**
     * Find mask table.
     *
     * @param tableName table name
     * @return found mask table
     */
    public Optional<MaskTable> findMaskTable(final String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }
}
