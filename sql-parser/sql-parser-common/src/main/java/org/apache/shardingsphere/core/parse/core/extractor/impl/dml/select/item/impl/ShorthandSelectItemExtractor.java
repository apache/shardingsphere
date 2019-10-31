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
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;

import java.util.Map;

/**
 * Shorthand select item extractor.
 *
 * @author zhangliang
 */
public final class ShorthandSelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ShorthandSelectItemSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            return Optional.of(new ShorthandSelectItemSegment(
                    unqualifiedShorthandNode.get().getStart().getStartIndex(), unqualifiedShorthandNode.get().getStop().getStopIndex(), unqualifiedShorthandNode.get().getText()));
        }
        Optional<ParserRuleContext> qualifiedShorthandNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.QUALIFIED_SHORTHAND);
        if (qualifiedShorthandNode.isPresent()) {
            ShorthandSelectItemSegment result = new ShorthandSelectItemSegment(
                    qualifiedShorthandNode.get().getStart().getStartIndex(), qualifiedShorthandNode.get().getStop().getStopIndex(), qualifiedShorthandNode.get().getText());
            ParserRuleContext ownerNode = (ParserRuleContext) qualifiedShorthandNode.get().getChild(0);
            result.setOwner(new TableSegment(ownerNode.getStart().getStartIndex(), ownerNode.getStop().getStopIndex(), ownerNode.getText()));
            return Optional.of(result);
        }
        return Optional.absent();
    }
}
