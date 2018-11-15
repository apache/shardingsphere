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
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Single table name extract handler.
 * 
 * @author duhongjun
 */
public class TableNameExtractHandler implements ASTExtractHandler {
    
    @Override
    public void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        Optional<ParserRuleContext> tableNameNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_NAME);
        if (tableNameNode.isPresent()) {
            String tableText = tableNameNode.get().getText();
            int dotPosition = tableText.contains(Symbol.DOT.getLiterals()) ? tableText.lastIndexOf(Symbol.DOT.getLiterals()) : 0;
            statement.getSQLTokens().add(new TableToken(tableNameNode.get().getStart().getStartIndex(), dotPosition, tableText));
            statement.getTables().add(new Table(SQLUtil.getExactlyValue(SQLUtil.getNameWithoutSchema(tableText)), Optional.<String>absent()));
        }
    }
}
