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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dql.item;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dql.SubqueryExtractor;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.SelectItemSegment;

/**
 * Select item extractor.
 *
 * @author zhangliang
 */
public final class SelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    private final StarSelectItemSegmentExtractor starItemExpressionExtractor = new StarSelectItemSegmentExtractor();
    
    private final ColumnSelectItemSegmentExtractor columnSelectItemSegmentExtractor = new ColumnSelectItemSegmentExtractor();
    
    private final FunctionSelectItemSegmentExtractor functionSelectItemSegmentExtractor = new FunctionSelectItemSegmentExtractor();
    
    private final ExpressionSelectItemSegmentExtractor expressionSelectItemSegmentExtractor = new ExpressionSelectItemSegmentExtractor();
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    @Override
    public Optional<? extends SelectItemSegment> extract(final ParserRuleContext expressionNode) {
        Optional<? extends SelectItemSegment> result;
        result = starItemExpressionExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = columnSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = functionSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = expressionSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        return subqueryExtractor.extract(expressionNode);
    }
}
