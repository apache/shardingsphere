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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.expression;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.StarItemExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;

/**
 * Star item expression extractor.
 *
 * @author zhangliang
 */
public final class StarItemExpressionExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<StarItemExpressionSegment> extract(final ParserRuleContext expressionNode) {
        String text = expressionNode.getText();
        if (!text.endsWith(Symbol.STAR.getLiterals())) {
            return Optional.absent();
        }
        StarItemExpressionSegment result = text.contains(Symbol.DOT.getLiterals())
                ? new StarItemExpressionSegment(text.substring(0, text.indexOf(Symbol.DOT.getLiterals())), expressionNode.getStart().getStartIndex())
                : new StarItemExpressionSegment(expressionNode.getStart().getStartIndex());
        return Optional.of(result);
    }
}
