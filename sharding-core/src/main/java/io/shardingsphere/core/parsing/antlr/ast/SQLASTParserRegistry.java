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

package io.shardingsphere.core.parsing.antlr.ast;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.ast.dialect.MySQLStatementASTParser;
import io.shardingsphere.core.parsing.antlr.ast.dialect.OracleStatementASTParser;
import io.shardingsphere.core.parsing.antlr.ast.dialect.PostgreSQLStatementASTParser;
import io.shardingsphere.core.parsing.antlr.ast.dialect.SQLServerStatementASTParser;
import io.shardingsphere.core.parsing.antlr.autogen.MySQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.OracleStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.PostgreSQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.SQLServerStatementLexer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.Lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL AST parser registry.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLASTParserRegistry {
    
    private static final Map<DatabaseType, Class<? extends Lexer>> LEXER_CLASSES = new HashMap<>();
    
    private static final Map<DatabaseType, Class<? extends SQLASTParser>> PARSER_CLASSES = new HashMap<>();
    
    static {
        registerLexer();
        registerParser();
    }
    
    private static void registerLexer() {
        LEXER_CLASSES.put(DatabaseType.H2, MySQLStatementLexer.class);
        LEXER_CLASSES.put(DatabaseType.MySQL, MySQLStatementLexer.class);
        LEXER_CLASSES.put(DatabaseType.PostgreSQL, PostgreSQLStatementLexer.class);
        LEXER_CLASSES.put(DatabaseType.SQLServer, SQLServerStatementLexer.class);
        LEXER_CLASSES.put(DatabaseType.Oracle, OracleStatementLexer.class);
    }
    
    private static void registerParser() {
        PARSER_CLASSES.put(DatabaseType.H2, MySQLStatementASTParser.class);
        PARSER_CLASSES.put(DatabaseType.MySQL, MySQLStatementASTParser.class);
        PARSER_CLASSES.put(DatabaseType.PostgreSQL, PostgreSQLStatementASTParser.class);
        PARSER_CLASSES.put(DatabaseType.SQLServer, SQLServerStatementASTParser.class);
        PARSER_CLASSES.put(DatabaseType.Oracle, OracleStatementASTParser.class);
    }
    
    /**
     * Get lexer class.
     * 
     * @param databaseType database type
     * @return lexer class
     */
    public static Class<? extends Lexer> getLexerClass(final DatabaseType databaseType) {
        return LEXER_CLASSES.get(databaseType);
    }
    
    /**
     * Get parser class.
     *
     * @param databaseType database type
     * @return parser class
     */
    public static Class<? extends SQLASTParser> getParserClass(final DatabaseType databaseType) {
        return PARSER_CLASSES.get(databaseType);
    }
}
