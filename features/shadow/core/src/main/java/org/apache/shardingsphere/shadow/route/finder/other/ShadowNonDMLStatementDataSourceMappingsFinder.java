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

package org.apache.shardingsphere.shadow.route.finder.other;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.route.determiner.HintShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.finder.ShadowDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;

import java.util.Collections;
import java.util.Map;

/**
 * Shadow non-DML statement data source mappings finder.
 */
@RequiredArgsConstructor
public final class ShadowNonDMLStatementDataSourceMappingsFinder implements ShadowDataSourceMappingsFinder {
    
    private final HintValueContext hintValueContext;
    
    @Override
    public Map<String, String> find(final ShadowRule rule) {
        if (!hintValueContext.isShadow()) {
            return Collections.emptyMap();
        }
        if (isMatchAnyHintShadowAlgorithms(rule, new ShadowCondition("", ShadowOperationType.HINT_MATCH))) {
            return rule.getAllShadowDataSourceMappings();
        }
        return Collections.emptyMap();
    }
    
    private boolean isMatchAnyHintShadowAlgorithms(final ShadowRule rule, final ShadowCondition shadowCondition) {
        return rule.getAllHintShadowAlgorithms().stream().anyMatch(each -> HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, rule, hintValueContext.isShadow()));
    }
}
