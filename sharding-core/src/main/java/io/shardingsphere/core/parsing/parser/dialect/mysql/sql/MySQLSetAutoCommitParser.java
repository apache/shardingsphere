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

package io.shardingsphere.core.parsing.parser.dialect.mysql.sql;

import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.sql.tcl.TCLStatement;
import io.shardingsphere.core.parsing.parser.sql.tcl.set.autocommit.AbstractSetAutoCommitParser;

/**
 * Set auto commit parser for MySQL.
 *
 * @author maxiaoguang
 */
public final class MySQLSetAutoCommitParser extends AbstractSetAutoCommitParser {
    
    public MySQLSetAutoCommitParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    public TCLStatement parse() {
        boolean autoCommit = false;
        while (!getLexerEngine().isEnd()) {
            getLexerEngine().nextToken();
            getLexerEngine().accept(Symbol.EQ);
            if ("1".equals(getLexerEngine().getCurrentToken().getLiterals())) {
                autoCommit = true;
            } else {
                autoCommit = false;
            }
            getLexerEngine().skipUntil(DefaultKeyword.AUTOCOMMIT);
        }
        return autoCommit ? new TCLStatement(TransactionOperationType.IGNORE) : new TCLStatement(TransactionOperationType.BEGIN);
    }
}
