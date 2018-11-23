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

package io.shardingsphere.core.parsing.antlr.extractor.sql.segment;

import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.result.IndexExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.sql.util.ASTUtils;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Indexes name extractor.
 *
 * @author duhongjun
 */
public final class IndexesNameExtractor implements SQLSegmentExtractor<Collection<IndexExtractResult>> {
    
    @Override
    public Collection<IndexExtractResult> extract(final ParserRuleContext ancestorNode) {
        Collection<ParserRuleContext> indexNameNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.INDEX_NAME);
        if (indexNameNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<IndexExtractResult> result = new LinkedList<>();
        for (ParserRuleContext each : indexNameNodes) {
            String name = SQLUtil.getNameWithoutSchema(each.getText());
            result.add(new IndexExtractResult(name, new IndexToken(each.getStop().getStartIndex(), name, null)));
        }
        return result;
    }
}
