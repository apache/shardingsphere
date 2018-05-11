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

package io.shardingsphere.core.parsing.parser.dialect.mysql.clause;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Select table references clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLTableReferencesClauseParser extends TableReferencesClauseParser {
    
    public MySQLTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
        parsePartition();
        parseIndexHint(sqlStatement);
    }
    
    private void parsePartition() {
        getLexerEngine().unsupportedIfEqual(MySQLKeyword.PARTITION);
    }
    
    private void parseIndexHint(final SQLStatement sqlStatement) {
        if (getLexerEngine().skipIfEqual(DefaultKeyword.USE, MySQLKeyword.IGNORE, MySQLKeyword.FORCE)) {
            getLexerEngine().skipAll(DefaultKeyword.INDEX, DefaultKeyword.KEY, DefaultKeyword.FOR, DefaultKeyword.JOIN, DefaultKeyword.ORDER, DefaultKeyword.GROUP, DefaultKeyword.BY);
            getLexerEngine().skipParentheses(sqlStatement);
        }
    }
    
    @Override
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[] {MySQLKeyword.STRAIGHT_JOIN};
    }
}
