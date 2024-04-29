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

package org.apache.shardingsphere.mask.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mask rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaskRuleStatementConverter {
    
    /**
     * Convert mask rule segments to mask rule configuration.
     *
     * @param ruleSegments mask rule segments
     * @return mask rule configuration
     */
    public static MaskRuleConfiguration convert(final Collection<MaskRuleSegment> ruleSegments) {
        Collection<MaskTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> algorithms = new HashMap<>();
        for (MaskRuleSegment each : ruleSegments) {
            tables.add(createMaskTableRuleConfiguration(each));
            algorithms.putAll(createMaskAlgorithmConfigurations(each));
        }
        return new MaskRuleConfiguration(tables, algorithms);
    }
    
    private static MaskTableRuleConfiguration createMaskTableRuleConfiguration(final MaskRuleSegment ruleSegment) {
        Collection<MaskColumnRuleConfiguration> columns = ruleSegment.getColumns().stream()
                .map(each -> createMaskColumnRuleConfiguration(ruleSegment.getTableName(), each)).collect(Collectors.toList());
        return new MaskTableRuleConfiguration(ruleSegment.getTableName(), columns);
    }
    
    private static MaskColumnRuleConfiguration createMaskColumnRuleConfiguration(final String tableName, final MaskColumnSegment columnSegment) {
        return new MaskColumnRuleConfiguration(columnSegment.getName(), getAlgorithmName(tableName, columnSegment));
    }
    
    private static Map<String, AlgorithmConfiguration> createMaskAlgorithmConfigurations(final MaskRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(ruleSegment.getColumns().size(), 1F);
        for (MaskColumnSegment each : ruleSegment.getColumns()) {
            result.put(getAlgorithmName(ruleSegment.getTableName(), each), new AlgorithmConfiguration(each.getAlgorithm().getName(), each.getAlgorithm().getProps()));
        }
        return result;
    }
    
    private static String getAlgorithmName(final String tableName, final MaskColumnSegment columnSegment) {
        return String.format("%s_%s_%s", tableName, columnSegment.getName(), columnSegment.getAlgorithm().getName()).toLowerCase();
    }
}
