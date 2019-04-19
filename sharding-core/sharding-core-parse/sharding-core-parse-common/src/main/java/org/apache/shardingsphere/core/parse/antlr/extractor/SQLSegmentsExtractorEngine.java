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

package org.apache.shardingsphere.core.parse.antlr.extractor;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.SQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.parser.SQLAST;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SQLSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
        Preconditions.checkState(ast.getSQLStatementRule().isPresent());
        Map<ParserRuleContext, Integer> parameterMarkerIndexes = getParameterMarkerIndexes(ast.getParserRuleContext());
        for (SQLSegmentExtractor each : ast.getSQLStatementRule().get().getExtractors()) {
            if (each instanceof OptionalSQLSegmentExtractor) {
                Optional<? extends SQLSegment> sqlSegment = ((OptionalSQLSegmentExtractor) each).extract(ast.getParserRuleContext(), parameterMarkerIndexes);
                if (sqlSegment.isPresent()) {
                    result.add(sqlSegment.get());
                }
            } else if (each instanceof CollectionSQLSegmentExtractor) {
                result.addAll(((CollectionSQLSegmentExtractor) each).extract(ast.getParserRuleContext(), parameterMarkerIndexes));
            }
        }
        return result;
    }
    
    private Map<ParserRuleContext, Integer> getParameterMarkerIndexes(final ParserRuleContext rootNode) {
        Collection<ParserRuleContext> placeholderNodes = ExtractorUtils.getAllDescendantNodes(rootNode, RuleName.PARAMETER_MARKER);
        Map<ParserRuleContext, Integer> result = new HashMap<>(placeholderNodes.size(), 1);
        int index = 0;
        for (ParserRuleContext each : placeholderNodes) {
            result.put(each, index++);
        }
        return result;
    }
}
