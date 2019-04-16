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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select.item;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select.SubqueryExtractor;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.item.SelectItemSegment;

/**
 * Select item extractor.
 *
 * @author zhangliang
 */
public final class SelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    private final ShorthandSelectItemExtractor shorthandSelectItemExtractor = new ShorthandSelectItemExtractor();
    
    private final ColumnSelectItemExtractor columnSelectItemExtractor = new ColumnSelectItemExtractor();
    
    private final FunctionSelectItemExtractor functionSelectItemSegmentExtractor = new FunctionSelectItemExtractor();
    
    private final ExpressionSelectItemExtractor expressionSelectItemExtractor = new ExpressionSelectItemExtractor();
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    @Override
    public Optional<? extends SelectItemSegment> extract(final ParserRuleContext expressionNode) {
        Optional<? extends SelectItemSegment> result;
        result = shorthandSelectItemExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = columnSelectItemExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = functionSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = expressionSelectItemExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        return subqueryExtractor.extract(expressionNode);
    }
}
