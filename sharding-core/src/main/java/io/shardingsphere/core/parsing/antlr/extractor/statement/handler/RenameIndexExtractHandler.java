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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Rename index extract handler.
 * 
 * @author duhongjun
 */
public final class RenameIndexExtractHandler implements ASTExtractHandler {
    
    @Override
    public void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        Optional<ParserRuleContext> renameIndexNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.RENAME_INDEX);
        if (!renameIndexNode.isPresent() || 4 > renameIndexNode.get().getChildCount()) {
            return;
        }
        ParseTree oldIndexNode = renameIndexNode.get().getChild(2);
        if (!(oldIndexNode instanceof ParserRuleContext)) {
            return;
        }
        ParseTree newIndexNode = renameIndexNode.get().getChild(4);
        if (!(newIndexNode instanceof ParserRuleContext)) {
            return;
        }
        statement.getSQLTokens().add(getIndexToken(statement, (ParserRuleContext) oldIndexNode));
        statement.getSQLTokens().add(getIndexToken(statement, (ParserRuleContext) newIndexNode));
    }
    
    private IndexToken getIndexToken(final SQLStatement statement, final ParserRuleContext indexNode) {
        return new IndexToken(indexNode.getStop().getStartIndex(), SQLUtil.getNameWithoutSchema(indexNode.getText()), statement.getTables().getSingleTableName());
    }
}
