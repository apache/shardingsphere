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

package io.shardingsphere.core.parsing.antlr.extractor;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.parser.SQLAST;
import io.shardingsphere.core.parsing.antlr.rule.registry.segment.SQLSegmentRule;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL segments extractor engine.
 * 
 * @author zhangliang
 */
public final class SQLSegmentsExtractorEngine {
    
    /** 
     * Extract SQL segments.
     * 
     * @param ast SQL AST
     * @return SQL segments
     */
    public Collection<SQLSegment> extract(final SQLAST ast) {
        Collection<SQLSegment> result = new LinkedList<>();
        for (SQLSegmentRule each : ast.getRule().getSqlSegmentRules()) {
            if (each.getExtractor() instanceof OptionalSQLSegmentExtractor) {
                Optional<? extends SQLSegment> sqlSegment = ((OptionalSQLSegmentExtractor) each.getExtractor()).extract(ast.getParserRuleContext());
                if (sqlSegment.isPresent()) {
                    result.add(sqlSegment.get());
                }
            }
            if (each.getExtractor() instanceof CollectionSQLSegmentExtractor) {
                result.addAll(((CollectionSQLSegmentExtractor) each.getExtractor()).extract(ast.getParserRuleContext()));
            }
        }
        return result;
    }
}
