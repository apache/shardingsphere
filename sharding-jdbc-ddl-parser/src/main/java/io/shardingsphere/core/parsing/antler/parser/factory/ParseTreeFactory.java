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

package io.shardingsphere.core.parsing.antler.parser.factory;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antler.ast.MySQLStatementParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.OracleStatementParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.PostgreStatementParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.SQLServerStatementParseTreeBuilder;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.rule.ShardingRule;

public class ParseTreeFactory {
    
    /** Get DDL table statement parser.
     * @param dbType database type.
     * @param tokenType token type.
     * @param shardingRule databases and tables sharding rule.
     * @param sql input sql text.
     * @return parse tree
     */
    public static ParserRuleContext getTableDDLParser(final DatabaseType dbType, final TokenType tokenType,
                                                      final ShardingRule shardingRule, final String sql) {
        switch (dbType) {
            case MySQL:
                return new MySQLStatementParseTreeBuilder().parse(sql);
            case Oracle:
                return new OracleStatementParseTreeBuilder().parse(sql);
            case SQLServer:
                return new SQLServerStatementParseTreeBuilder().parse(sql);
            case PostgreSQL:
                return new PostgreStatementParseTreeBuilder().parse(sql);

            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }

}
