/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.parser.sql.dml.delete;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingjdbc.core.parsing.parser.sql.SQLParser;
import io.shardingjdbc.core.parsing.parser.sql.dml.DMLStatement;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

/**
 * Delete parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractDeleteParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AbstractDeleteClauseParserFacade deleteClauseParserFacade;
    
    @Override
    public DMLStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipAll(getSkippedKeywordsBetweenDeleteAndTable());
        lexerEngine.unsupportedIfEqual(getUnsupportedKeywordsBetweenDeleteAndTable());
        DMLStatement result = new DMLStatement();
        deleteClauseParserFacade.getTableReferencesClauseParser().parse(result, true);
        lexerEngine.skipUntil(DefaultKeyword.WHERE);
        deleteClauseParserFacade.getWhereClauseParser().parse(shardingRule, result, Collections.<SelectItem>emptyList());
        return result;
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenDeleteAndTable();
    
    protected Keyword[] getUnsupportedKeywordsBetweenDeleteAndTable() {
        return new Keyword[0];
    }
}
