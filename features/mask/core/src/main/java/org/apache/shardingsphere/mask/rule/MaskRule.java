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
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RuleIdentifiers;
import org.apache.shardingsphere.infra.rule.identifier.type.table.TableMapperContainedRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mask rule.
 */
@SuppressWarnings("rawtypes")
public final class MaskRule implements DatabaseRule, TableMapperContainedRule {
    
    @Getter
    private final MaskRuleConfiguration configuration;
    
    private final Map<String, MaskAlgorithm> maskAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, MaskTable> tables = new LinkedHashMap<>();
    
    @Getter
    private final MaskTableMapperRule tableMapperRule;
    
    @Getter
    private final RuleIdentifiers ruleIdentifiers;
    
    public MaskRule(final MaskRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        ruleConfig.getMaskAlgorithms().forEach((key, value) -> maskAlgorithms.put(key, TypedSPILoader.getService(MaskAlgorithm.class, value.getType(), value.getProps())));
        ruleConfig.getTables().forEach(each -> tables.put(each.getName().toLowerCase(), new MaskTable(each)));
        tableMapperRule = new MaskTableMapperRule(ruleConfig.getTables());
        ruleIdentifiers = new RuleIdentifiers(tableMapperRule);
    }
    
    /**
     * Find mask algorithm.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return maskAlgorithm
     */
    public Optional<MaskAlgorithm> findMaskAlgorithm(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findMaskAlgorithmName(logicColumn).map(maskAlgorithms::get) : Optional.empty();
    }
}
