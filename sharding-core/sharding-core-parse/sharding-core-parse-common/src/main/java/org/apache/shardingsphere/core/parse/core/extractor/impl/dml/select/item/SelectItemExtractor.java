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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.SubqueryExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl.ColumnSelectItemExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl.ExpressionSelectItemExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl.FunctionSelectItemExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl.ShorthandSelectItemExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl.TopSelectItemExtractor;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;

import java.util.Map;

/**
 * Select item extractor.
 *
 * @author zhangliang
 */
public final class SelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    private final ShorthandSelectItemExtractor shorthandSelectItemExtractor = new ShorthandSelectItemExtractor();
    
    private final ColumnSelectItemExtractor columnSelectItemExtractor = new ColumnSelectItemExtractor();
    
    private final TopSelectItemExtractor topSelectItemExtractor = new TopSelectItemExtractor();
    
    private final FunctionSelectItemExtractor functionSelectItemSegmentExtractor = new FunctionSelectItemExtractor();
    
    private final ExpressionSelectItemExtractor expressionSelectItemExtractor = new ExpressionSelectItemExtractor();
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    @Override
    public Optional<? extends SelectItemSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<? extends SelectItemSegment> result;
        result = shorthandSelectItemExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = topSelectItemExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = columnSelectItemExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = functionSelectItemSegmentExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = subqueryExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        return expressionSelectItemExtractor.extract(expressionNode, parameterMarkerIndexes);
    }
}
