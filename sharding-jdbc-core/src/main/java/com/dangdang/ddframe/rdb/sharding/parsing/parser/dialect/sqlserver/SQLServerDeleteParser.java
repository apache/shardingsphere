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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.delete.AbstractDeleteParser;

/**
 * SQLServer Delete语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerDeleteParser extends AbstractDeleteParser {
    
    public SQLServerDeleteParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        if (getSqlParser().equalAny(SQLServerKeyword.TOP)) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
        skipOutput();
        getSqlParser().skipIfEqual(DefaultKeyword.FROM);
    }
    
    private void skipOutput() {
        if (getSqlParser().equalAny(SQLServerKeyword.OUTPUT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.OUTPUT);
        }
    }
}
