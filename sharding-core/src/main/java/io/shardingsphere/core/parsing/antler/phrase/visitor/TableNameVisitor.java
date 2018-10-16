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

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antler.utils.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;

public class TableNameVisitor implements PhraseVisitor {

    /** Visit table name node.
     * @param ancestorNode ancestor node of ast
     * @param statement sql statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        ParserRuleContext tableNameCtx = TreeUtils.getFirstChildByRuleName(ancestorNode,
                RuleNameConstants.TABLE_NAME);
        if (null != tableNameCtx) {
            String name = tableNameCtx.getText();
            if (null == name) {
                return;
            }

            String dotString = Symbol.DOT.getLiterals();
            int pos = name.lastIndexOf(dotString);
            String literals = null;
            if (pos > 0) {
                literals = name.substring(pos + dotString.length());
            } else {
                pos = 0;
                literals = name;
            }

            statement.getSqlTokens().add(new TableToken(tableNameCtx.getStart().getStartIndex(), pos, name));
            statement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), Optional.<String>absent()));
        }
    }

}
