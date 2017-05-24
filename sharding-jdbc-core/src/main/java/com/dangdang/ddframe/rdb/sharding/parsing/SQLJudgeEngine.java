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

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.analyzer.Dictionary;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.DeleteSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import lombok.RequiredArgsConstructor;

/**
 * SQL判断引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final String sql;
    
    /**
     * 判断并获取SQL类型.
     *
     * @return SQL解析上下文
     */
    public SQLContext judge() {
        Lexer lexer = new Lexer(sql, new Dictionary());
        lexer.nextToken();
        while (true) {
            TokenType tokenType = lexer.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (tokenType.equals(DefaultKeyword.SELECT)) {
                    return new SelectSQLContext();
                } else if (tokenType.equals(DefaultKeyword.UPDATE)) {
                    return new UpdateSQLContext();
                } else if (tokenType.equals(DefaultKeyword.INSERT)) {
                    return new InsertSQLContext();
                } else if (tokenType.equals(DefaultKeyword.DELETE)) {
                    return new DeleteSQLContext();
                }
            }
            if (tokenType instanceof Assist && tokenType.equals(Assist.END)) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
            lexer.nextToken();
        }
    }
}
