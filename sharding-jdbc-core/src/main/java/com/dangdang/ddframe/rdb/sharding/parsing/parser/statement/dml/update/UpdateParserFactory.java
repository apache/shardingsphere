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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.update;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerUpdateParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Update语句解析器工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateParserFactory {
    
    /**
     * 创建Update语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param commonParser 解析器
     * @return Update语句解析器
     */
    public static AbstractUpdateParser newInstance(final ShardingRule shardingRule, final CommonParser commonParser) {
        if (commonParser.getLexer() instanceof MySQLLexer) {
            return new MySQLUpdateParser(shardingRule, commonParser);
        }
        if (commonParser.getLexer() instanceof OracleLexer) {
            return new OracleUpdateParser(shardingRule, commonParser);
        }
        if (commonParser.getLexer() instanceof SQLServerLexer) {
            return new SQLServerUpdateParser(shardingRule, commonParser);
        }
        if (commonParser.getLexer() instanceof PostgreSQLLexer) {
            return new PostgreSQLUpdateParser(shardingRule, commonParser);
        }
        throw new UnsupportedOperationException(String.format("Cannot support lexer class [%s].", commonParser.getLexer().getClass()));
    } 
}
