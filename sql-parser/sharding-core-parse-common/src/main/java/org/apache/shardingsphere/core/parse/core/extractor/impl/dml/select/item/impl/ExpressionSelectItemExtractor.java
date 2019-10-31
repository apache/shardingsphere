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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;

import java.util.Map;

/**
 * Expression select item extractor.
 *
 * @author zhangliang
 */
public final class ExpressionSelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ExpressionSelectItemSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        // TODO parse table inside expression
        ExpressionSelectItemSegment result = new ExpressionSelectItemSegment(expressionNode.getStart().getStartIndex(), expressionNode.getStop().getStopIndex(), expressionNode.getText());
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNodeNoneRecursive(expressionNode, RuleName.ALIAS);
        if (aliasNode.isPresent()) {
            result.setAlias(aliasNode.get().getText());
        }
        return Optional.of(result);
    }
}
