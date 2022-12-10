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

package org.apache.shardingsphere.shadow.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Shadow rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRuleStatementConverter {
    
    /**
     * Convert shadow rule segments to shadow rule configuration.
     *
     * @param rules shadow rule statements
     * @return shadow rule configuration
     */
    public static ShadowRuleConfiguration convert(final Collection<ShadowRuleSegment> rules) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(getShadowAlgorithms(rules));
        result.setDataSources(getDataSource(rules));
        result.setTables(getTables(rules));
        return result;
    }
    
    private static Map<String, ShadowTableConfiguration> getTables(final Collection<ShadowRuleSegment> rules) {
        Map<String, ShadowTableConfiguration> result = new HashMap<>();
        rules.forEach(each -> {
            Map<String, ShadowTableConfiguration> currentRuleTableConfig = each.getShadowTableRules().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, entry -> buildShadowTableConfiguration(each.getRuleName(), entry), ShadowRuleStatementSupporter::mergeConfiguration));
            currentRuleTableConfig.forEach((key, value) -> result.merge(key, value, ShadowRuleStatementSupporter::mergeConfiguration));
        });
        return result;
    }
    
    private static ShadowTableConfiguration buildShadowTableConfiguration(final String ruleName, final Entry<String, Collection<ShadowAlgorithmSegment>> entry) {
        return new ShadowTableConfiguration(new ArrayList<>(Collections.singleton(ruleName)), entry.getValue().stream().map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList()));
    }
    
    private static Collection<ShadowDataSourceConfiguration> getDataSource(final Collection<ShadowRuleSegment> rules) {
        Collection<ShadowDataSourceConfiguration> result = new LinkedList<>();
        rules.forEach(each -> result.add(new ShadowDataSourceConfiguration(each.getRuleName(), each.getSource(), each.getShadow())));
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> getShadowAlgorithms(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .collect(Collectors.toMap(ShadowAlgorithmSegment::getAlgorithmName, ShadowRuleStatementConverter::buildAlgorithmConfiguration));
    }
    
    private static AlgorithmConfiguration buildAlgorithmConfiguration(final ShadowAlgorithmSegment segment) {
        return new AlgorithmConfiguration(segment.getAlgorithmSegment().getName(), segment.getAlgorithmSegment().getProps());
    }
}
