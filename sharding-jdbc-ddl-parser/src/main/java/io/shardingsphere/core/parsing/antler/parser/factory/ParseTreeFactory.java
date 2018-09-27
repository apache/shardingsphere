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
import io.shardingsphere.core.parsing.antler.ast.mysql.MySQLAlterTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.mysql.MySQLCreateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.mysql.MySQLDropTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.mysql.MySQLTruncateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.oracle.OracleAlterTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.oracle.OracleCreateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.oracle.OracleDropTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.oracle.OracleTruncateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.postgre.PostgreAlterTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.postgre.PostgreCreateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.postgre.PostgreDropTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.postgre.PostgreTruncateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.sqlserver.SQLServerAlterTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.sqlserver.SQLServerCreateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.sqlserver.SQLServerDropTableParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.ast.sqlserver.SQLServerTruncateTableParseTreeBuilder;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.rule.ShardingRule;

public class ParseTreeFactory {
    public static ParserRuleContext getTableDDLParser(final DatabaseType dbType, final TokenType tokenType,
            final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch (dbType) {
        case MySQL:
            return getMySQLTableDDLParser(tokenType, shardingRule, lexerEngine);
        case Oracle:
            return getOracleTableDDLParser(tokenType, shardingRule, lexerEngine);
        case SQLServer:
            return getSQLServerTableDDLParser(tokenType, shardingRule, lexerEngine);
        case PostgreSQL:
            return getPostgreTableDDLParser(tokenType, shardingRule, lexerEngine);

        default:
            throw new SQLParsingUnsupportedException(tokenType);
        }
    }
    
    private static ParserRuleContext getMySQLTableDDLParser(final TokenType tokenType, final ShardingRule shardingRule,
            final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
        case CREATE:
            return new MySQLCreateTableParseTreeBuilder().parse(lexerEngine.getInput());
        case ALTER:
            return new MySQLAlterTableParseTreeBuilder().parse(lexerEngine.getInput());
        case DROP:
            return new MySQLDropTableParseTreeBuilder().parse(lexerEngine.getInput());
        case TRUNCATE:
            return new MySQLTruncateTableParseTreeBuilder().parse(lexerEngine.getInput());
        default:
            throw new SQLParsingUnsupportedException(tokenType);
        }
    }

    private static ParserRuleContext getOracleTableDDLParser(final TokenType tokenType, final ShardingRule shardingRule,
            final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
        case CREATE:
            return new OracleCreateTableParseTreeBuilder().parse(lexerEngine.getInput());
        case ALTER:
            return new OracleAlterTableParseTreeBuilder().parse(lexerEngine.getInput());
        case DROP:
            return new OracleDropTableParseTreeBuilder().parse(lexerEngine.getInput());
        case TRUNCATE:
            return new OracleTruncateTableParseTreeBuilder().parse(lexerEngine.getInput());
        default:
            throw new SQLParsingUnsupportedException(tokenType);
        }
    }

    private static ParserRuleContext getSQLServerTableDDLParser(final TokenType tokenType,
            final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
        case CREATE:
            return new SQLServerCreateTableParseTreeBuilder().parse(lexerEngine.getInput());
        case ALTER:
            return new SQLServerAlterTableParseTreeBuilder().parse(lexerEngine.getInput());
        case DROP:
            return new SQLServerDropTableParseTreeBuilder().parse(lexerEngine.getInput());
        case TRUNCATE:
            return new SQLServerTruncateTableParseTreeBuilder().parse(lexerEngine.getInput());
        default:
            throw new SQLParsingUnsupportedException(tokenType);
        }
    }

    private static ParserRuleContext getPostgreTableDDLParser(final TokenType tokenType,
            final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
        case CREATE:
            return new PostgreCreateTableParseTreeBuilder().parse(lexerEngine.getInput());
        case ALTER:
            return new PostgreAlterTableParseTreeBuilder().parse(lexerEngine.getInput());
        case DROP:
            return new PostgreDropTableParseTreeBuilder().parse(lexerEngine.getInput());
        case TRUNCATE:
            return new PostgreTruncateTableParseTreeBuilder().parse(lexerEngine.getInput());
        default:
            throw new SQLParsingUnsupportedException(tokenType);
        }
    }

}
