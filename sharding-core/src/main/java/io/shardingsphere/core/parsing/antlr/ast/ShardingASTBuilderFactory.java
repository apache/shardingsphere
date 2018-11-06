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
import io.shardingsphere.core.parsing.antlr.autogen.MySQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.OracleStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.PostgreStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.SQLServerStatementLexer;
import io.shardingsphere.core.parsing.antlr.parser.dialect.MySQLStatementAdvancedParser;
import io.shardingsphere.core.parsing.antlr.parser.dialect.OracleStatementAdvancedParser;
import io.shardingsphere.core.parsing.antlr.parser.dialect.PostgreStatementAdvancedParser;
import io.shardingsphere.core.parsing.antlr.parser.dialect.SQLServerStatementAdvancedParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding AST builder factory.
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingASTBuilderFactory {
    
    /** 
     * New instance of {@code ShardingASTBuilder}.
     * 
     * @param dbType database type
     * @return instance of {@code ShardingASTBuilder}
     */
    public static ShardingASTBuilder newInstance(final DatabaseType dbType) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new ShardingASTBuilder(MySQLStatementLexer.class, MySQLStatementAdvancedParser.class);
            case PostgreSQL:
                return new ShardingASTBuilder(PostgreStatementLexer.class, PostgreStatementAdvancedParser.class);
            case SQLServer:
                return new ShardingASTBuilder(SQLServerStatementLexer.class, SQLServerStatementAdvancedParser.class);
            case Oracle:
                return new ShardingASTBuilder(OracleStatementLexer.class, OracleStatementAdvancedParser.class);
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", dbType.name()));
        }
    }
}
