/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.rule.registry.extractor;

import io.shardingsphere.core.parsing.antlr.extractor.SQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.extractor.ExtractorRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.extractor.ExtractorRuleEntity;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extractor rule definition.
 *
 * @author zhangliang
 */
@Getter
public final class ExtractorRuleDefinition {
    
    private final Map<String, SQLSegmentExtractor> rules = new LinkedHashMap<>();
    
    /**
     * Initialize SQL extractor rule definition.
     * 
     * @param commonRuleDefinitionEntity common extractor rule definition entity
     * @param dialectRuleDefinitionEntity SQL dialect extractor rule definition entity
     */
    public void init(final ExtractorRuleDefinitionEntity commonRuleDefinitionEntity, final ExtractorRuleDefinitionEntity dialectRuleDefinitionEntity) {
        init(commonRuleDefinitionEntity);
        init(dialectRuleDefinitionEntity);
    }
    
    @SneakyThrows
    private void init(final ExtractorRuleDefinitionEntity ruleDefinitionEntity) {
        for (ExtractorRuleEntity each : ruleDefinitionEntity.getRules()) {
            rules.put(each.getId(), (SQLSegmentExtractor) Class.forName(each.getExtractorClass()).newInstance());
        }
    }
}
