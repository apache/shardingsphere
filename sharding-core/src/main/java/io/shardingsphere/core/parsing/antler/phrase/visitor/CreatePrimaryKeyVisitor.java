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

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.utils.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

public class CreatePrimaryKeyVisitor implements PhraseVisitor {

    /**
     * Visit ast.
     * @param ancestorNode ancestor node of ast
     * @param statement sql statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        CreateTableStatement createStatement = (CreateTableStatement) statement;
        ParserRuleContext primaryKeyCtx = TreeUtils.getFirstChildByRuleName(ancestorNode, RuleNameConstants.PRIMARY_KEY);
        if (null == primaryKeyCtx) {
            return;
        }

        ParserRuleContext columnListCtx = TreeUtils.getFirstChildByRuleName(primaryKeyCtx.getParent().getParent(),
                RuleNameConstants.COLUMN_LIST);
        if (null == columnListCtx) {
            return;
        }

        List<ParserRuleContext> columnNodes = TreeUtils.getAllDescendantByRuleName(columnListCtx, RuleNameConstants.COLUMN_NAME);
        if (null == columnNodes) {
            return;
        }

        for (final ParserRuleContext each : columnNodes) {
            if (!createStatement.getPrimaryKeyColumns().contains(each.getText())) {
                createStatement.getPrimaryKeyColumns().add(each.getText());
            }
        }
    }

}
