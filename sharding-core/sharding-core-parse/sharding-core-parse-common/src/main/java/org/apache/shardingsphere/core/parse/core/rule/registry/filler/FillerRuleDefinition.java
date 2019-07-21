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

package org.apache.shardingsphere.core.parse.core.rule.registry.filler;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.core.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.filler.FillerRuleEntity;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filler rule definition.
 *
 * @author zhangliang
 */
public final class FillerRuleDefinition {
    
    private final Map<Class<? extends SQLSegment>, SQLSegmentFiller> rules;
    
    public FillerRuleDefinition(final FillerRuleDefinitionEntity... entities) {
        rules = new LinkedHashMap<>();
        for (FillerRuleDefinitionEntity each : entities) {
            put(each);
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void put(final FillerRuleDefinitionEntity entity) {
        for (FillerRuleEntity each : entity.getRules()) {
            rules.put((Class<? extends SQLSegment>) Class.forName(each.getSqlSegmentClass()), (SQLSegmentFiller) Class.forName(each.getFillerClass()).newInstance());
        }
    }
    
    /**
     * Get SQL segment filler.
     *
     * @param className SQL segment class name
     * @return SQL segment filler
     */
    public SQLSegmentFiller getFiller(final Class<? extends SQLSegment> className) {
        return rules.get(className);
    }
}
