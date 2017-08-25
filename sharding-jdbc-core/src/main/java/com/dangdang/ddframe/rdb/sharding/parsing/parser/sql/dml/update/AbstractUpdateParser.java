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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.update;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.DMLStatement;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

/**
 * Update parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractUpdateParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AbstractUpdateClauseParserFacade updateClauseParserFacade;
    
    @Override
    public DMLStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipAll(getSkippedKeywordsBetweenUpdateAndTable());
        lexerEngine.unsupportedIfEqual(getUnsupportedKeywordsBetweenUpdateAndTable());
        DMLStatement result = new DMLStatement();
        updateClauseParserFacade.getTableReferencesClauseParser().parse(result, true);
        updateClauseParserFacade.getUpdateSetItemsClauseParser().parse(result);
        lexerEngine.skipUntil(DefaultKeyword.WHERE);
        updateClauseParserFacade.getWhereClauseParser().parse(shardingRule, result, Collections.<SelectItem>emptyList());
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    protected Keyword[] getUnsupportedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
}
