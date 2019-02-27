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

package io.shardingsphere.core.parsing.antlr.extractor.impl.dml;

import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.impl.AbstractFromWhereExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;

/**
 * Update where extractor.
 *
 * @author duhongjun
 */
public final class UpdateWhereExtractor extends AbstractFromWhereExtractor {
    
    @Override
    protected Optional<ParserRuleContext> extractTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        Optional<ParserRuleContext> tableReferenceNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCE);
        if (!tableReferenceNode.isPresent()) {
            return Optional.absent();
        }
        this.extractTableReference(fromWhereSegment, tableReferenceNode.get(), questionNodeIndexMap);
        return ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
    }
}
