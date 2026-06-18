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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.hint;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.route.determiner.HintShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.ShadowTableDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Shadow table hint data source mappings retriever.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class ShadowTableHintDataSourceMappingsRetriever implements ShadowTableDataSourceMappingsRetriever {
    
    private final ShadowOperationType operationType;
    
    private final boolean isShadow;
    
    @Override
    public Map<String, String> retrieve(final ShadowRule rule, final Collection<String> shadowTables) {
        return shadowTables.isEmpty() && isMatchDefaultAlgorithm(rule) ? rule.getAllShadowDataSourceMappings() : findShadowDataSourceMappingsBySQLHints(rule, shadowTables);
    }
    
    @SuppressWarnings("unchecked")
    private boolean isMatchDefaultAlgorithm(final ShadowRule rule) {
        return rule.getDefaultShadowAlgorithm()
                .filter(optional -> HintShadowAlgorithmDeterminer.isShadow((HintShadowAlgorithm<Comparable<?>>) optional, new ShadowCondition(), rule, isShadow)).isPresent();
    }
    
    private Map<String, String> findShadowDataSourceMappingsBySQLHints(final ShadowRule rule, final Collection<String> shadowTables) {
        for (String each : shadowTables) {
            if (containsShadowInSQLHints(rule, each, new ShadowCondition(each, operationType))) {
                return rule.getShadowDataSourceMappings(each);
            }
        }
        return Collections.emptyMap();
    }
    
    private boolean containsShadowInSQLHints(final ShadowRule rule, final String tableName, final ShadowCondition shadowCondition) {
        for (HintShadowAlgorithm<Comparable<?>> each : rule.getHintShadowAlgorithms(tableName)) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, rule, isShadow)) {
                return true;
            }
        }
        return false;
    }
}
