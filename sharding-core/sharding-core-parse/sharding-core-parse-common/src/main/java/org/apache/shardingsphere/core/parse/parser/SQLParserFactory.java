/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.core.parse.api.SQLParser;
import org.apache.shardingsphere.core.parse.spi.SQLParserEntry;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * SQL parser factory.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    private static final Collection<String> DATABASE_TYPES = new HashSet<>();
    
    private static final Map<String, String> DATABASE_ALIAS = new HashMap<>();
    
    static {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
        for (SQLParserEntry each : NewInstanceServiceLoader.newServiceInstances(SQLParserEntry.class)) {
            DATABASE_TYPES.add(each.getDatabaseType());
            for (String alias : each.getDatabaseTypeAliases()) {
                DATABASE_ALIAS.put(alias, each.getDatabaseType());
            }
        }
    }
    
    /**
     * Get add on database types.
     * 
     * @return add on database types
     */
    public static Collection<String> getAddOnDatabaseTypes() {
        return DATABASE_TYPES;
    }
    
    /**
     * Is database alias.
     *
     * @param databaseAlias database alias
     * @return is database alias or not
     */
    public static boolean isDatabaseAlias(final String databaseAlias) {
        return DATABASE_ALIAS.containsKey(databaseAlias);
    }
    
    /**
     * Get add on database type.
     *
     * @param databaseAlias database alias
     * @return add on database type
     */
    public static String getAddOnDatabaseType(final String databaseAlias) {
        return DATABASE_ALIAS.get(databaseAlias);
    }
    
    /** 
     * New instance of SQL parser.
     * 
     * @param databaseType database type
     * @param sql SQL
     * @return SQL parser
     */
    public static SQLParser newInstance(final String databaseType, final String sql) {
        for (SQLParserEntry each : NewInstanceServiceLoader.newServiceInstances(SQLParserEntry.class)) {
            if (isCurrentDatabaseType(databaseType, each)) {
                return createSQLParser(sql, each);
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseType));
    }
    
    private static boolean isCurrentDatabaseType(final String databaseType, final SQLParserEntry sqlParserEntry) {
        if (sqlParserEntry.getDatabaseType().equals(databaseType)) {
            return true;
        }
        for (String each : sqlParserEntry.getDatabaseTypeAliases()) {
            if (each.equals(databaseType)) {
                return true;
            }
        }
        return false;
    }
    
    @SneakyThrows
    private static SQLParser createSQLParser(final String sql, final SQLParserEntry parserEntry) {
        Lexer lexer = parserEntry.getLexerClass().getConstructor(CharStream.class).newInstance(CharStreams.fromString(sql));
        return parserEntry.getParserClass().getConstructor(TokenStream.class).newInstance(new CommonTokenStream(lexer));
    }
}
