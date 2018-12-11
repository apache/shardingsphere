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

package io.shardingsphere.core.parsing.antlr.rule.registry.segment;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.segment.SQLSegmentRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.segment.SQLSegmentRuleEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL segment rule definition.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLSegmentRuleDefinition {
    
    private final DatabaseType databaseType;
    
    private final Map<String, SQLSegmentRule> rules = new LinkedHashMap<>();
    
    /**
     * Initialize SQL segment rule definition.
     * 
     * @param commonRuleDefinitionEntity common SQL segment rule definition
     * @param dialectRuleDefinitionEntity SQL dialect segment rule definition
     */
    public void init(final SQLSegmentRuleDefinitionEntity commonRuleDefinitionEntity, final SQLSegmentRuleDefinitionEntity dialectRuleDefinitionEntity) {
        init(commonRuleDefinitionEntity);
        init(dialectRuleDefinitionEntity);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void init(final SQLSegmentRuleDefinitionEntity ruleDefinitionEntity) {
        for (SQLSegmentRuleEntity each : ruleDefinitionEntity.getRules()) {
            rules.put(each.getId(), new SQLSegmentRule(each.getId(), 
                    getActualClass(ruleDefinitionEntity.getBasePackage(), ruleDefinitionEntity.getExtractorBasePackage(), each.getExtractorClass()),
                    getActualClass(ruleDefinitionEntity.getBasePackage(), ruleDefinitionEntity.getFillerBasePackage(), each.getFillerClass())));
        }
    }
    
    private Class getActualClass(final String basePackage, final String classPackage, final String className) throws ClassNotFoundException {
        return Strings.isNullOrEmpty(className) ? null : Class.forName(Joiner.on('.').join(basePackage, classPackage, className));
    }
}
