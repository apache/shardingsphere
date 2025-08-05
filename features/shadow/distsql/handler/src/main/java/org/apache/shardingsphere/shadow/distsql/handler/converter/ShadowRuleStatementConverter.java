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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;

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
     * @param segments shadow rule segments
     * @return shadow rule configuration
     */
    public static ShadowRuleConfiguration convert(final Collection<ShadowRuleSegment> segments) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(getShadowAlgorithms(segments));
        result.setDataSources(getDataSource(segments));
        result.setTables(getTables(segments));
        return result;
    }
    
    private static Map<String, ShadowTableConfiguration> getTables(final Collection<ShadowRuleSegment> segments) {
        Map<String, ShadowTableConfiguration> result = new HashMap<>();
        segments.forEach(each -> {
            Map<String, ShadowTableConfiguration> currentRuleTableConfig = each.getShadowTableRules().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, entry -> buildShadowTableConfiguration(each.getRuleName(), entry.getValue()), ShadowRuleStatementSupporter::mergeConfiguration));
            currentRuleTableConfig.forEach((key, value) -> result.merge(key, value, ShadowRuleStatementSupporter::mergeConfiguration));
        });
        return result;
    }
    
    private static ShadowTableConfiguration buildShadowTableConfiguration(final String ruleName, final Collection<ShadowAlgorithmSegment> algorithmSegments) {
        return new ShadowTableConfiguration(new ArrayList<>(Collections.singleton(ruleName)), algorithmSegments.stream().map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList()));
    }
    
    private static Collection<ShadowDataSourceConfiguration> getDataSource(final Collection<ShadowRuleSegment> segments) {
        Collection<ShadowDataSourceConfiguration> result = new LinkedList<>();
        segments.forEach(each -> result.add(new ShadowDataSourceConfiguration(each.getRuleName(), each.getSource(), each.getShadow())));
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> getShadowAlgorithms(final Collection<ShadowRuleSegment> segments) {
        return segments.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .collect(Collectors.toMap(ShadowAlgorithmSegment::getAlgorithmName, ShadowRuleStatementConverter::buildAlgorithmConfiguration));
    }
    
    private static AlgorithmConfiguration buildAlgorithmConfiguration(final ShadowAlgorithmSegment segment) {
        return new AlgorithmConfiguration(segment.getAlgorithmSegment().getName(), segment.getAlgorithmSegment().getProps());
    }
}
