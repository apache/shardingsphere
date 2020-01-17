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

package org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.sql.parser.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.SubqueryExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item.impl.ColumnProjectionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item.impl.ExpressionProjectionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item.impl.FunctionProjectionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item.impl.ShorthandProjectionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.select.item.impl.TopProjectionExtractor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;

import java.util.Map;

/**
 * Projection extractor.
 *
 * @author zhangliang
 */
public final class ProjectionExtractor implements OptionalSQLSegmentExtractor {
    
    private final ShorthandProjectionExtractor shorthandProjectionExtractor = new ShorthandProjectionExtractor();
    
    private final ColumnProjectionExtractor columnProjectionExtractor = new ColumnProjectionExtractor();
    
    private final TopProjectionExtractor topProjectionExtractor = new TopProjectionExtractor();
    
    private final FunctionProjectionExtractor functionProjectionExtractor = new FunctionProjectionExtractor();
    
    private final ExpressionProjectionExtractor expressionProjectionExtractor = new ExpressionProjectionExtractor();
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    @Override
    public Optional<? extends ProjectionSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<? extends ProjectionSegment> result;
        result = shorthandProjectionExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = topProjectionExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = columnProjectionExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = functionProjectionExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        result = subqueryExtractor.extract(expressionNode, parameterMarkerIndexes);
        if (result.isPresent()) {
            return result;
        }
        return expressionProjectionExtractor.extract(expressionNode, parameterMarkerIndexes);
    }
}
