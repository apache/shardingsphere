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

package io.shardingsphere.core.parsing.antler.phrase.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class RenameIndexVisitor implements PhraseVisitor {

    /**
     * Visit rename index node.
     *
     * @param ancestorNode ancestor node of ast
     * @param statement    sql statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        ParserRuleContext renameIndexNode = (ParserRuleContext) TreeUtils.getFirstChildByRuleName(ancestorNode,
                "renameIndex");

        if (null == renameIndexNode || 4 > renameIndexNode.getChildCount()) {
            return;
        }

        ParseTree oldIndexNode = renameIndexNode.getChild(2);
        if (!(oldIndexNode instanceof ParserRuleContext)) {
            return;
        }

        ParseTree newIndexNode = renameIndexNode.getChild(4);
        if (!(newIndexNode instanceof ParserRuleContext)) {
            return;
        }

        ParserRuleContext oldIndexCtx = (ParserRuleContext) oldIndexNode;
        ParserRuleContext newIndexCtx = (ParserRuleContext) newIndexNode;

        statement.getSqlTokens()
                .add(VisitorUtils.visitIndex(oldIndexCtx, statement.getTables().getSingleTableName()));

        statement.getSqlTokens()
                .add(VisitorUtils.visitIndex(newIndexCtx, statement.getTables().getSingleTableName()));
    }
}
