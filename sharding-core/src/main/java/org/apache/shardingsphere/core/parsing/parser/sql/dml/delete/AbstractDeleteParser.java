/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.parser.sql.dml.delete;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLParser;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
    public final DMLStatement parse() {
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
    
    protected abstract Keyword[] getUnsupportedKeywordsBetweenDeleteAndTable();
}
