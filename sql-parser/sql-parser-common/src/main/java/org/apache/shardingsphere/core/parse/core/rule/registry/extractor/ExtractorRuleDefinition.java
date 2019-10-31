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

package org.apache.shardingsphere.core.parse.core.rule.registry.extractor;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.core.extractor.api.SQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.extractor.ExtractorRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.extractor.ExtractorRuleEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extractor rule definition.
 *
 * @author zhangliang
 * @author duhongjun
 */
public final class ExtractorRuleDefinition {
    
    private final Map<String, SQLSegmentExtractor> rules;
    
    public ExtractorRuleDefinition(final ExtractorRuleDefinitionEntity... entities) {
        rules = new LinkedHashMap<>();
        for (ExtractorRuleDefinitionEntity each : entities) {
            put(each);
        }
    }
    
    @SneakyThrows
    private void put(final ExtractorRuleDefinitionEntity entity) {
        for (ExtractorRuleEntity each : entity.getRules()) {
            rules.put(each.getId(), (SQLSegmentExtractor) Class.forName(each.getExtractorClass()).newInstance());
        }
    }
    
    /**
     * Get SQL segment extractor.
     * 
     * @param name extractor name
     * @return SQL segment extractor
     */
    public SQLSegmentExtractor getExtractor(final String name) {
        return rules.get(name);
    }
}
