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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.create;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLCreateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleCreateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLCreateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerCreateParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Create语句解析器工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateParserFactory {
    
    /**
     * 创建Create Table语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param commonParser 解析器
     * @return Create语句解析器
     */
    public static AbstractCreateParser newInstance(final ShardingRule shardingRule, final CommonParser commonParser) {
        if (commonParser.getLexer() instanceof MySQLLexer) {
            return new MySQLCreateParser(shardingRule, commonParser);
        }
        if (commonParser.getLexer() instanceof OracleLexer) {
            return new OracleCreateParser(shardingRule, commonParser);
        }
        if (commonParser.getLexer() instanceof SQLServerLexer) {
            return new SQLServerCreateParser(shardingRule, commonParser);
        }
        if (commonParser.getLexer() instanceof PostgreSQLLexer) {
            return new PostgreSQLCreateParser(shardingRule, commonParser);
        }
        throw new UnsupportedOperationException(String.format("Cannot support lexer class [%s].", commonParser.getLexer().getClass()));
    } 
}
