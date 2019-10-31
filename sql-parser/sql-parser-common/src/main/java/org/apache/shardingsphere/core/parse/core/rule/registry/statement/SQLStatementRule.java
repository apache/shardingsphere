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

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.core.extractor.api.SQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.statement.SQLStatementRuleEntity;
import org.apache.shardingsphere.core.parse.core.rule.registry.extractor.ExtractorRuleDefinition;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * SQL statement rule.
 *
 * @author zhangliang
 */
@Getter
public final class SQLStatementRule {
    
    private final String contextName;
    
    private final Class<? extends SQLStatement> sqlStatementClass;
    
    private final Collection<SQLSegmentExtractor> extractors;
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SQLStatementRule(final SQLStatementRuleEntity entity, final ExtractorRuleDefinition extractorRuleDefinition) {
        contextName = entity.getContext();
        sqlStatementClass = (Class<? extends SQLStatement>) Class.forName(entity.getSqlStatementClass());
        extractors = getExtractors(entity.getExtractorRuleRefs(), extractorRuleDefinition);
    }
    
    private Collection<SQLSegmentExtractor> getExtractors(final String extractorRuleRefs, final ExtractorRuleDefinition extractorRuleDefinition) {
        if (null == extractorRuleRefs) {
            return Collections.emptyList();
        }
        Collection<SQLSegmentExtractor> result = new LinkedList<>();
        for (String each : Splitter.on(',').trimResults().splitToList(extractorRuleRefs)) {
            result.add(extractorRuleDefinition.getExtractor(each));
        }
        return result;
    }
}
