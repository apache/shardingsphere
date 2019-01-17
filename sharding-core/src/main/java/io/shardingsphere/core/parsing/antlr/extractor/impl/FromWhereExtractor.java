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

package io.shardingsphere.core.parsing.antlr.extractor.impl;

import java.util.Collection;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;

/**
 * From where extractor.
 *
 * @author duhongjun
 */
public final class FromWhereExtractor extends AbstractFromWhereExtractor {
    
    @Override
    protected Optional<ParserRuleContext> extractTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        Optional<ParserRuleContext> selectClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> fromNode = ExtractorUtils.findFirstChildNodeNoneRecursive(selectClauseNode.get().getParent(), RuleName.FROM_CLAUSE);
        if (!fromNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> tableReferenceNodes = ExtractorUtils.getAllDescendantNodes(fromNode.get(), RuleName.TABLE_REFERENCE);
        if (tableReferenceNodes.isEmpty()) {
            return Optional.absent();
        }
        for (ParserRuleContext each : tableReferenceNodes) {
            extractTableReference(fromWhereSegment, each, questionNodeIndexMap);
        }
        return ExtractorUtils.findFirstChildNodeNoneRecursive(fromNode.get().getParent(), RuleName.WHERE_CLAUSE);
    }
}
