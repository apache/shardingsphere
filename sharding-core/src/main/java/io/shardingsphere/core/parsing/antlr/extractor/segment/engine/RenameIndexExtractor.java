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

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.IndexSegment;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Rename index extractor.
 *
 * @author duhongjun
 */
public final class RenameIndexExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<IndexSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> renameIndexNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.RENAME_INDEX);
        if (!renameIndexNode.isPresent() || 4 > renameIndexNode.get().getChildCount()) {
            return Collections.emptyList();
        }
        ParseTree oldIndexNode = renameIndexNode.get().getChild(2);
        if (!(oldIndexNode instanceof ParserRuleContext)) {
            return Collections.emptyList();
        }
        ParseTree newIndexNode = renameIndexNode.get().getChild(4);
        if (!(newIndexNode instanceof ParserRuleContext)) {
            return Collections.emptyList();
        }
        Collection<IndexSegment> result = new LinkedList<>();
        result.add(getIndexToken((ParserRuleContext) newIndexNode));
        result.add(getIndexToken((ParserRuleContext) oldIndexNode));
        return result;
    }
    
    private IndexSegment getIndexToken(final ParserRuleContext indexNode) {
        String name = SQLUtil.getNameWithoutSchema(indexNode.getText());
        return new IndexSegment(name, new IndexToken(indexNode.getStop().getStartIndex(), name));
    }
}
