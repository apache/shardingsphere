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

package com.alibaba.druid.sql.lexer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 语言标记对象.
 * 
 * @author zhangliang 
 */
@RequiredArgsConstructor
public enum Token {
    
    SELECT("SELECT", true, false, false, false, false, false), 
    DELETE("DELETE", true, false, false, false, false, false), 
    INSERT("INSERT", true, false, false, false, false, false), 
    UPDATE("UPDATE", true, false, false, false, false, false), 
    FROM("FROM", true, false, false, false, false, false), 
    HAVING("HAVING", true, false, false, false, false, false), 
    WHERE("WHERE", true, false, false, false, false, false), 
    ORDER("ORDER", true, false, false, false, false, false), 
    BY("BY", true, false, false, false, false, false), 
    GROUP("GROUP", true, false, false, false, false, false), 
    INTO("INTO", true, false, false, false, false, false), 
    AS("AS", true, false, false, false, false, false), 
    CREATE("CREATE", true, false, false, false, false, false), 
    ALTER("ALTER", true, false, false, false, false, false), 
    DROP("DROP", true, false, false, false, false, false), 
    SET("SET", true, false, false, false, false, false), 
    NULL("NULL", true, false, false, false, false, false), 
    NOT("NOT", true, false, false, false, false, false), 
    DISTINCT("DISTINCT", true, false, false, false, false, false),
    DISTINCTROW("DISTINCTROW", false, true, false, false, false, false),
    TABLE("TABLE", true, false, false, false, false, false), 
    TABLESPACE("TABLESPACE", true, false, false, false, false, false), 
    VIEW("VIEW", true, false, false, false, false, false), 
    SEQUENCE("SEQUENCE", true, false, false, false, false, false), 
    TRIGGER("TRIGGER", true, false, false, false, false, false), 
    USER("USER", true, false, false, false, false, false), 
    INDEX("INDEX", true, false, false, false, false, false),
    PROCEDURE("PROCEDURE", true, false, false, false, false, false), 
    FUNCTION("FUNCTION", true, false, false, false, false, false), 
    PRIMARY("PRIMARY", true, false, false, false, false, false),
    KEY("KEY", true, false, false, false, false, false), 
    DEFAULT("DEFAULT", true, false, false, false, false, false), 
    CONSTRAINT("CONSTRAINT", true, false, false, false, false, false), 
    CHECK("CHECK", true, false, false, false, false, false), 
    UNIQUE("UNIQUE", true, false, false, false, false, false), 
    FOREIGN("FOREIGN", true, false, false, false, false, false), 
    REFERENCES("REFERENCES", true, false, false, false, false, false), 
    EXPLAIN("EXPLAIN", true, false, false, false, false, false), 
    FOR("FOR", true, false, false, false, false, false),
    ALL("ALL", true, false, false, false, false, false), 
    UNION("UNION", true, false, false, false, false, false), 
    EXCEPT("EXCEPT", true, false, false, false, false, false), 
    INTERSECT("INTERSECT", true, false, false, false, false, false), 
    MINUS("MINUS", true, false, false, false, false, false), 
    INNER("INNER", true, false, false, false, false, false), 
    LEFT("LEFT", true, false, false, false, false, false), 
    RIGHT("RIGHT", true, false, false, false, false, false), 
    FULL("FULL", true, false, false, false, false, false), 
    OUTER("OUTER", true, false, false, false, false, false), 
    JOIN("JOIN", true, false, false, false, false, false),
    ON("ON", true, false, false, false, false, false), 
    SCHEMA("SCHEMA", true, false, false, false, false, false), 
    CAST("CAST", true, false, false, false, false, false), 
    COLUMN("COLUMN", true, false, false, false, false, false), 
    USE("USE", true, false, false, false, false, false), 
    DATABASE("DATABASE", true, false, false, false, false, false), 
    TO("TO", true, false, false, false, false, false), 
    AND("AND", true, false, false, false, false, false), 
    OR("OR", true, false, false, false, false, false), 
    XOR("XOR", true, false, false, false, false, false), 
    CASE("CASE", true, false, false, false, false, false), 
    WHEN("WHEN", true, false, false, false, false, false), 
    THEN("THEN", true, false, false, false, false, false), 
    ELSE("ELSE", true, false, false, false, false, false), 
    END("END", true, false, false, false, false, false), 
    EXISTS("EXISTS", true, false, false, false, false, false), 
    IN("IN", true, false, false, false, false, false), 
    NEW("NEW", true, false, false, false, false, false),
    ASC("ASC", true, false, false, false, false, false), 
    DESC("DESC", true, false, false, false, false, false), 
    IS("IS", true, false, false, false, false, false), 
    LIKE("LIKE", true, false, false, false, false, false), 
    ESCAPE("ESCAPE", true, false, false, false, false, false), 
    BETWEEN("BETWEEN", true, false, false, false, false, false), 
    VALUES("VALUES", true, false, false, false, false, false),
    VALUE("VALUE", false, true, false, false, false, false),
    INTERVAL("INTERVAL", true, false, false, false, false, false), 
    LOCK("LOCK", true, false, false, false, false, false), 
    SOME("SOME", true, false, false, false, false, false), 
    ANY("ANY", true, false, false, false, false, false), 
    TRUNCATE("TRUNCATE", true, false, false, false, false, false),
    REPLACE("REPLACE", true, false, false, false, false, false),
    WHILE("WHILE", true, false, false, false, false, false),
    DO("DO", true, false, false, false, false, false),
    LEAVE("LEAVE", true, false, false, false, false, false),
    ITERATE("ITERATE", true, false, false, false, false, false),
    REPEAT("REPEAT", true, false, false, false, false, false),
    UNTIL("UNTIL", true, false, false, false, false, false),
    OPEN("OPEN", true, false, false, false, false, false),
    CLOSE("CLOSE", true, false, false, false, false, false),
    OUT("OUT", true, false, false, false, false, false),
    INOUT("INOUT", true, false, false, false, false, false),
    PASSWORD("PASSWORD", true, false, false, false, false, false),
    COMMENT("COMMENT", true, false, false, false, false, false),
    OVER("OVER", true, false, false, false, false, false),
    FETCH("FETCH", true, false, false, false, false, false),
    WITH("WITH", true, false, false, false, false, false),
    CURSOR("CURSOR", true, false, false, false, false, false),
    ADVISE("ADVISE", true, false, false, false, false, false),
    DECLARE("DECLARE", true, false, false, false, false, false),
    GRANT("GRANT", true, false, false, false, false, false),
    REVOKE("REVOKE", true, false, false, false, false, false),
    LOOP("LOOP", true, false, false, false, false, false),
    ENABLE("ENABLE", true, false, false, false, false, false),
    DISABLE("DISABLE", true, false, false, false, false, false),
    SESSION("SESSION", false, false, false, true, false, false),
    IF("IF", false, true, true, true, true, false),
    TRUE("TRUE", false, true, true, false, false, false),
    FALSE("FALSE", false, true, true, false, false, false),
    LIMIT("LIMIT", false, true, true, true, false, false),
    KILL("KILL", false, true, false, false, false, false), 
    IDENTIFIED("IDENTIFIED", false, true, false, true, false, false),
    DUAL("DUAL", false, true, false, false, false, false),
    BINARY("BINARY", false, true, false, false, false, false),
    SHOW("SHOW", false, true, true, false, false, false),
    WINDOW("WINDOW", false, false, true, false, false, false),
    OFFSET("OFFSET", false, true, true, false, true, false),
    ROW("ROW", false, true, true, true, false, false), 
    ROWS("ROWS", false, false, true, true, false, false),
    ONLY("ONLY", false, false, true, true, true, true), 
    FIRST("FIRST", false, false, true, true, false, true), 
    NEXT("NEXT", false, false, true, true, false, false),
    OF("OF", false, false, true, true, false, false),
    SHARE("SHARE", false, false, true, true, false, false),
    NOWAIT("NOWAIT", false, false, true, true, false, false),
    RECURSIVE("RECURSIVE", false, false, true, false, false, false),
    TEMPORARY("TEMPORARY", false, false, true, false, false, false),
    TEMP("TEMP", false, false, true, false, false, false),
    UNLOGGED("UNLOGGED", false, false, true, false, false, false),
    RESTART("RESTART", false, false, true, false, false, false),
    IDENTITY("IDENTITY", false, false, true, false, true, false),
    CONTINUE("CONTINUE", false, false, true, true, false, false),
    CASCADE("CASCADE", false, false, true, false, false, false),
    RESTRICT("RESTRICT", false, false, true, false, false, false),
    USING("USING", true, false, false, false, false, false), 
    CURRENT("CURRENT", false, false, true, false, false, false),
    RETURNING("RETURNING", false, false, true, true, false, false),
    TYPE("TYPE", false, false, true, false, false, false),
    START("START", false, false, false, true, false, false),
    PRIOR("PRIOR", false, false, false, true, false, false),
    CONNECT("CONNECT", false, false, false, true, false, false),
    EXTRACT("EXTRACT", false, false, false, true, false, false),
    MODEL("MODEL", false, false, false, true, false, false),
    MERGE("MERGE", false, false, false, true, false, false),
    MATCHED("MATCHED", false, false, false, true, false, false),
    ERRORS("ERRORS", false, false, false, true, false, false),
    REJECT("REJECT", false, false, false, true, false, false),
    UNLIMITED("UNLIMITED", false, false, false, true, false, false),
    BEGIN("BEGIN", false, true, false, true, true, false),
    EXCLUSIVE("EXCLUSIVE", false, false, false, true, false, false),
    MODE("MODE", false, false, false, true, false, false),
    WAIT("WAIT", false, false, false, true, false, false),
    SYSDATE("SYSDATE", false, false, false, true, false, false),
    EXCEPTION("EXCEPTION", false, false, false, true, false, false),
    GOTO("GOTO", false, false, false, true, false, false),
    COMMIT("COMMIT", false, false, false, true, false, false),
    SAVEPOINT("SAVEPOINT", false, false, false, true, false, false),
    CROSS("CROSS", true, false, false, false, false, false),
    APPLY("APPLY", false, false, false, true, true, false),
    PCTFREE("PCTFREE", false, false, false, true, false, false),
    INITRANS("INITRANS", false, false, false, true, false, false),
    MAXTRANS("MAXTRANS", false, false, false, true, false, false),
    INITIALLY("INITIALLY", false, false, false, true, false, false),
    SEGMENT("SEGMENT", false, false, false, true, false, false),
    CREATION("CREATION", false, false, false, true, false, false),
    IMMEDIATE("IMMEDIATE", false, false, false, true, false, false),
    DEFERRED("DEFERRED", false, false, false, true, false, false),
    STORAGE("STORAGE", false, false, false, true, false, false),
    MINEXTENTS("MINEXTENTS", false, false, false, true, false, false),
    MAXEXTENTS("MAXEXTENTS", false, false, false, true, false, false),
    MAXSIZE("MAXSIZE", false, false, false, true, false, false),
    PCTINCREASE("PCTINCREASE", false, false, false, true, false, false),
    FLASH_CACHE("FLASH_CACHE", false, false, false, true, false, false),
    CELL_FLASH_CACHE("CELL_FLASH_CACHE", false, false, false, true, false, false),
    KEEP("KEEP", false, false, false, true, false, false),
    NONE("NONE", false, false, false, true, false, false),
    LOB("LOB", false, false, false, true, false, false),
    STORE("STORE", false, false, false, true, false, false),
    CHUNK("CHUNK", false, false, false, true, false, false),
    CACHE("CACHE", false, true, false, true, false, false),
    NOCACHE("NOCACHE", false, false, false, true, false, false),
    LOGGING("LOGGING", false, false, false, true, false, false),
    NOCOMPRESS("NOCOMPRESS", false, false, false, true, false, false),
    KEEP_DUPLICATES("KEEP_DUPLICATES", false, false, false, true, false, false),
    EXCEPTIONS("EXCEPTIONS", false, false, false, true, false, false),
    PURGE("PURGE", false, false, false, true, false, false),
    COMPUTE("COMPUTE", true, false, false, false, false, false),
    ANALYZE("ANALYZE", false, true, false, false, false, false),
    OPTIMIZE("OPTIMIZE", false, true, false, false, false, true),
    TOP("TOP", false, false, false, false, true, false),
    ARRAY("ARRAY", false, false, true, false, false, false),
    PARTITION("PARTITION", false, true, false, true, false, false),
    SUBPARTITION("SUBPARTITION", false, false, false, true, false, false),
    IGNORE("IGNORE", false, true, false, false, false, false),
    QUICK("QUICK", false, true, false, false, false, false),
    LOW_PRIORITY("LOW_PRIORITY", false, true, false, false, false, false),
    HIGH_PRIORITY("HIGH_PRIORITY", false, true, false, false, false, false),
    STRAIGHT_JOIN("STRAIGHT_JOIN", false, true, false, false, false, false),
    SQL_SMALL_RESULT("SQL_SMALL_RESULT", false, true, false, false, false, false),
    SQL_BIG_RESULT("SQL_BIG_RESULT", false, true, false, false, false, false),
    SQL_BUFFER_RESULT("SQL_BUFFER_RESULT", false, true, false, false, false, false),
    SQL_CACHE("SQL_CACHE", false, true, false, false, false, false),
    SQL_NO_CACHE("SQL_NO_CACHE", false, true, false, false, false, false),
    SQL_CALC_FOUND_ROWS("SQL_CALC_FOUND_ROWS", false, true, false, false, false, false),
    OUTPUT("OUTPUT", false, false, false, false, true, false),
    CONNECT_BY_ROOT("CONNECT_BY_ROOT", false, false, false, true, false, false),
    UPDATED("UPDATED", false, false, false, true, false, false),
    REFERENCE("REFERENCE", false, false, false, true, false, false),
    RETURN("RETURN", false, false, false, true, false, false),
    MAIN("MAIN", false, false, false, true, false, false),
    UPSERT("UPSERT", false, false, false, true, false, false),
    NULLS("NULLS", false, false, false, true, false, false),
    SIBLINGS("SIBLINGS", false, false, true, true, false, false),
    
    
    EOF, 
    ERROR,
    IDENTIFIER,
    HINT,
    VARIANT,
    LITERAL_INT,
    LITERAL_FLOAT,
    LITERAL_HEX,
    LITERAL_CHARS,
    LITERAL_NCHARS,
    
    LITERAL_ALIAS,
    LINE_COMMENT,
    MULTI_LINE_COMMENT,
    
    // Oracle
    BINARY_FLOAT,
    BINARY_DOUBLE,
    
    LEFT_PAREN("(", true, false, false, false, false, false), 
    RIGHT_PAREN(")", true, false, false, false, false, false), 
    LEFT_BRACE("{", true, false, false, false, false, false),
    RIGHT_BRACE("}", true, false, false, false, false, false),
    LEFT_BRACKET("[", true, false, false, false, false, false),
    RIGHT_BRACKET("]", true, false, false, false, false, false), 
    SEMI(";", true, false, false, false, false, false), 
    COMMA(",", true, false, false, false, false, false), 
    DOT(".", true, false, false, false, false, false),
    DOUBLE_DOT("..", true, false, false, false, false, false),
    EQ("=", true, false, false, false, false, false), 
    GT(">", true, false, false, false, false, false), 
    LT("<", true, false, false, false, false, false), 
    BANG("!", true, false, false, false, false, false), 
    TILDE("~", true, false, false, false, false, false), 
    QUESTION("?", true, false, false, false, false, false), 
    COLON(":", true, false, false, false, false, false), 
    DOUBLE_COLON("::", true, false, false, false, false, false), 
    COLON_EQ(":=", true, false, false, false, false, false), 
    LT_EQ("<=", true, false, false, false, false, false),
    GT_EQ(">=", true, false, false, false, false, false),
    LT_EQ_GT("<=>", true, false, false, false, false, false), 
    LT_GT("<>", true, false, false, false, false, false),
    BANG_EQ("!=", true, false, false, false, false, false), 
    BANG_GT("!>", true, false, false, false, false, false), 
    BANG_LT("!<", true, false, false, false, false, false),
    PLUS("+", true, false, false, false, false, false), 
    SUB("-", true, false, false, false, false, false), 
    STAR("*", true, false, false, false, false, false), 
    SLASH("/", true, false, false, false, false, false), 
    AMP("&", true, false, false, false, false, false), 
    BAR("|", true, false, false, false, false, false),
    DOUBLE_AMP("&&", true, false, false, false, false, false),
    DOUBLE_BAR("||", true, false, false, false, false, false),
    CARET("^", true, false, false, false, false, false), 
    PERCENT("%", true, false, false, false, false, false), 
    DOUBLE_LT("<<", true, false, false, false, false, false), 
    DOUBLE_GT(">>", true, false, false, false, false, false), 
    MONKEYS_AT("@", true, false, false, false, false, false),
    POUND("#", true, false, false, false, false, false);
    
    @Getter
    private static Map<String, Token> defaultKeywords;
    
    @Getter
    private static Map<String, Token> mysqlKeywords;
    
    @Getter
    private static Map<String, Token> postgresqlKeywords;
    
    @Getter
    private static Map<String, Token> oracleKeywords;
    
    @Getter
    private static Map<String, Token> sqlserverKeywords;
    
    @Getter
    private static Map<String, Token> db2Keywords;
    
    @Getter
    private final String name;
    
    private final boolean keyword;
    
    private final boolean mysqlKeyword;
    
    private final boolean postgresqlKeyword;
    
    private final boolean oracleKeyword;
    
    private final boolean sqlserverKeyword;
    
    private final boolean db2Keyword;
    
    Token() {
        this(null, false, false, false, false, false, false);
    }
    
    static {
        defaultKeywords = getAllDefaultKeywords();
        mysqlKeywords = getAllMySQLKeywords();
        postgresqlKeywords = getAllPostgreSQLKeywords();
        oracleKeywords = getAllOracleKeywords();
        sqlserverKeywords = getAllSQLServerKeywords();
        db2Keywords = getAllDB2Keywords();
    }
    
    private static Map<String, Token> getAllDefaultKeywords() {
        Token[] allTokens = Token.values();
        Map<String, Token> result = new HashMap<>(allTokens.length, 1);
        for (Token each : allTokens) {
            if (each.keyword) {
                addKeyword(result, each);
            }
        }
        return result;
    }
    
    private static Map<String, Token> getAllMySQLKeywords() {
        Map<String, Token> result = getAllDefaultKeywords();
        Token[] allTokens = Token.values();
        for (Token each : allTokens) {
            if (each.mysqlKeyword) {
                addKeyword(result, each);
            }
        }
        return result;
    }
    
    private static Map<String, Token> getAllPostgreSQLKeywords() {
        Map<String, Token> result = getAllDefaultKeywords();
        Token[] allTokens = Token.values();
        for (Token each : allTokens) {
            if (each.postgresqlKeyword) {
                addKeyword(result, each);
            }
        }
        return result;
    }
    
    private static Map<String, Token> getAllOracleKeywords() {
        Map<String, Token> result = getAllDefaultKeywords();
        Token[] allTokens = Token.values();
        for (Token each : allTokens) {
            if (each.oracleKeyword) {
                addKeyword(result, each);
            }
        }
        return result;
    }
    
    private static Map<String, Token> getAllSQLServerKeywords() {
        Map<String, Token> result = getAllDefaultKeywords();
        Token[] allTokens = Token.values();
        for (Token each : allTokens) {
            if (each.sqlserverKeyword) {
                addKeyword(result, each);
            }
        }
        return result;
    }
    
    private static Map<String, Token> getAllDB2Keywords() {
        Map<String, Token> result = getAllDefaultKeywords();
        Token[] allTokens = Token.values();
        for (Token each : allTokens) {
            if (each.db2Keyword) {
                addKeyword(result, each);
            }
        }
        return result;
    }
    
    private static void addKeyword(final Map<String, Token> keywords, final Token token) {
        keywords.put(token.name, token);
    }
}
