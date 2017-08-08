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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.alter;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.DDLStatement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Alter语句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractAlterParser implements SQLStatementParser {
    
    private final SQLParser sqlParser;
    
    private final DDLStatement alterStatement;
    
    public AbstractAlterParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        alterStatement = new DDLStatement();
    }
    
    @Override
    public DDLStatement parse() {
        sqlParser.getLexer().nextToken();
        if (!sqlParser.skipIfEqual(DefaultKeyword.TABLE)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        getSqlParser().skipAll(getSkipWordsBetweenKeywordAndTableName());
        sqlParser.parseSingleTable(alterStatement);
        return alterStatement;
    }
    
    protected abstract Keyword[] getSkipWordsBetweenKeywordAndTableName();
}
