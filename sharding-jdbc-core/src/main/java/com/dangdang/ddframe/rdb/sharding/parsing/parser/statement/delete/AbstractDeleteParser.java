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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.delete;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Delete语句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractDeleteParser implements SQLStatementParser {
    
    @Getter(AccessLevel.PROTECTED)
    private final com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser sqlParser;
    
    private final DeleteStatement deleteStatement;
    
    public AbstractDeleteParser(final com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        deleteStatement = new DeleteStatement();
    }
    
    @Override
    public DeleteStatement parse() {
        sqlParser.getLexer().nextToken();
        skipBetweenDeleteAndTable();
        sqlParser.parseSingleTable(deleteStatement);
        sqlParser.skipUntil(DefaultKeyword.WHERE);
        sqlParser.parseWhere(deleteStatement);
        return deleteStatement;
    }
    
    protected abstract void skipBetweenDeleteAndTable();
}
