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

package org.apache.shardingsphere.core.parse.core.rule.registry.statement;

import com.google.common.base.CaseFormat;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.statement.SQLStatementRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.statement.SQLStatementRuleEntity;
import org.apache.shardingsphere.core.parse.core.rule.registry.extractor.ExtractorRuleDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL statement rule definition.
 *
 * @author zhangliang
 */
public final class SQLStatementRuleDefinition {
    
    private final Map<String, SQLStatementRule> rules = new LinkedHashMap<>();
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SQLStatementRuleDefinition(final SQLStatementRuleDefinitionEntity entity, final ExtractorRuleDefinition extractorRuleDefinition) {
        for (SQLStatementRuleEntity each : entity.getRules()) {
            rules.put(getContextClassName(each.getContext()), new SQLStatementRule(each, extractorRuleDefinition));
        }
    }
    
    private String getContextClassName(final String context) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, context + "Context");
    }
    
    /**
     * Get SQL statement rule.
     *
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public SQLStatementRule getSQLStatementRule(final String contextClassName) {
        return rules.get(contextClassName);
    }
}
