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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.lexer.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.AbstractUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLExprParser;

/**
 * PostgreSQL Update语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLUpdateParser extends AbstractUpdateParser {
    
    public PostgreSQLUpdateParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void skipBetweenUpdateAndTable() {
        getExprParser().skipIfEqual(PostgreSQLKeyword.ONLY);
    }
}
