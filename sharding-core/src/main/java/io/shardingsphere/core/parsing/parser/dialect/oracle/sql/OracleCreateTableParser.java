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

package io.shardingsphere.core.parsing.parser.dialect.oracle.sql;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.AbstractCreateTableParser;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Create parser for Oracle.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class OracleCreateTableParser extends AbstractCreateTableParser {
    
    public OracleCreateTableParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenCreateAndKeyword() {
        return new Keyword[] {DefaultKeyword.GLOBAL, DefaultKeyword.TEMPORARY};
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenCreateTableAndTableName() {
        return new Keyword[] {};
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenCreateIndexAndKeyword() {
        return new Keyword[] {DefaultKeyword.UNIQUE, DefaultKeyword.BITMAP};
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBeforeTableConstraint() {
        return new Keyword[0];
    }
}
