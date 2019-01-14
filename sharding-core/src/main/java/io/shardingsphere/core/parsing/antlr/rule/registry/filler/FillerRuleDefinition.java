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

package io.shardingsphere.core.parsing.antlr.rule.registry.filler;

import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.filler.FillerRuleEntity;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filler rule definition.
 *
 * @author zhangliang
 */
@Getter
public final class FillerRuleDefinition {
    
    private final Map<Class<? extends SQLSegment>, SQLStatementFiller> rules = new LinkedHashMap<>();
    
    /**
     * Initialize filler rule definition.
     * 
     * @param fillerRuleDefinitionEntity filler rule definition entity
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public void init(final FillerRuleDefinitionEntity fillerRuleDefinitionEntity) {
        for (FillerRuleEntity each : fillerRuleDefinitionEntity.getRules()) {
            rules.put((Class<? extends SQLSegment>) Class.forName(each.getSqlSegmentClass()), (SQLStatementFiller) Class.forName(each.getFillerClass()).newInstance());
        }
    }
}
