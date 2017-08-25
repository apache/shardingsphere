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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import lombok.RequiredArgsConstructor;

/**
 * SQL judge engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final String sql;
    
    /**
     * judge SQL Type only.
     *
     * @return SQL statement
     */
    public SQLStatement judge() {
        Lexer lexer = new Lexer(sql, new Dictionary());
        lexer.nextToken();
        while (true) {
            TokenType tokenType = lexer.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (DefaultKeyword.SELECT == tokenType) {
                    return new SelectStatement();
                } else if (DefaultKeyword.INSERT == tokenType || DefaultKeyword.UPDATE == tokenType || DefaultKeyword.DELETE == tokenType) {
                    return new DMLStatement();
                }
            }
            if (tokenType instanceof Assist && Assist.END == tokenType) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
            lexer.nextToken();
        }
    }
}
