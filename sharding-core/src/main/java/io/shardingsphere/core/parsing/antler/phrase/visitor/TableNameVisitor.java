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

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antler.util.ASTUtils;
import io.shardingsphere.core.parsing.antler.util.RuleNameConstants;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Visit single tableName phrase.
 * 
 * @author duhongjun
 */
public class TableNameVisitor implements PhraseVisitor {
    
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        Optional<ParserRuleContext> tableNameContext = ASTUtils.findFirstChildByRuleName(ancestorNode, RuleNameConstants.TABLE_NAME);
        if (tableNameContext.isPresent()) {
            String name = tableNameContext.get().getText();
            if (null == name) {
                return;
            }
            String dotString = Symbol.DOT.getLiterals();
            int position = name.lastIndexOf(dotString);
            String literals;
            if (position > 0) {
                literals = name.substring(position + dotString.length());
            } else {
                position = 0;
                literals = name;
            }
            statement.getSQLTokens().add(new TableToken(tableNameContext.get().getStart().getStartIndex(), position, name));
            statement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), Optional.<String>absent()));
        }
    }
}
