/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.constant.OrderDirection;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;

/**
 * Order by clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleOrderByClauseParser extends OrderByClauseParser {
    
    public OracleOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderDirection getNullOrderDirection() {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.NULLS)) {
            return OrderDirection.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.FIRST)) {
            return OrderDirection.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.LAST)) {
            return OrderDirection.DESC;
        }
        throw new SQLParsingException(getLexerEngine());
    }
}
