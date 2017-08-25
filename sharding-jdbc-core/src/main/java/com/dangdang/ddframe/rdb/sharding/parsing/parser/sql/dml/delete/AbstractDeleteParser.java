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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.delete;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.DMLStatement;
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
