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

package io.shardingsphere.core.parsing.parser.sql.ddl.drop.table;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Drop parser.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDropTableParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public AbstractDropTableParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public final DDLStatement parse() {
        lexerEngine.skipAll(getSkippedKeywordsBetweenDropAndTable());
        DropTableStatement result = new DropTableStatement();
        if (lexerEngine.skipIfEqual(DefaultKeyword.TABLE)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenDropTableAndTableName());
            tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        } else {
            throw new SQLParsingException("Can't support other DROP grammar unless DROP TABLE.");
        }
        return result;
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenDropAndTable();
    
    protected abstract Keyword[] getSkippedKeywordsBetweenDropTableAndTableName();
}
