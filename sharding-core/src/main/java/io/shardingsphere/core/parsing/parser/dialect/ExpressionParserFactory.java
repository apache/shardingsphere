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

package io.shardingsphere.core.parsing.parser.dialect;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.dialect.mysql.clause.expression.MySQLAliasExpressionParser;
import io.shardingsphere.core.parsing.parser.dialect.oracle.clause.expression.OracleAliasExpressionParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.expression.PostgreSQLAliasExpressionParser;
import io.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.expression.SQLServerAliasExpressionParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Expression parser factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionParserFactory {
    
    /**
     * Create alias parser instance.
     * 
     * @param lexerEngine lexical analysis engine.
     * @return alias parser instance
     */
    public static AliasExpressionParser createAliasExpressionParser(final LexerEngine lexerEngine) {
        switch (lexerEngine.getDatabaseType()) {
            case H2:
                return new MySQLAliasExpressionParser(lexerEngine);
            case MySQL:
                return new MySQLAliasExpressionParser(lexerEngine);
            case Oracle:
                return new OracleAliasExpressionParser(lexerEngine);
            case SQLServer:
                return new SQLServerAliasExpressionParser(lexerEngine);
            case PostgreSQL:
                return new PostgreSQLAliasExpressionParser(lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type: %s", lexerEngine.getDatabaseType()));
        }
    }
    
    /**
     * Create expression parser instance.
     *
     * @param lexerEngine lexical analysis engine.
     * @return expression parser instance
     */
    public static BasicExpressionParser createBasicExpressionParser(final LexerEngine lexerEngine) {
        return new BasicExpressionParser(lexerEngine);
    }
}
