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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.insert;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.parser.MySQLInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.parser.OracleInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.parser.PostgreSQLInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.parser.SQLServerInsertParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

import java.util.List;

/**
 * Insert语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLInsertParserFactory {
    
    /**
     * 创建Insert语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param parameters 参数列表
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Insert语句解析器
     */
    public static AbstractInsertParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLInsertParser(shardingRule, parameters, exprParser);
            case Oracle:
                return new OracleInsertParser(shardingRule, parameters, exprParser);
            case SQLServer:
                return new SQLServerInsertParser(shardingRule, parameters, exprParser);
            case PostgreSQL:
                return new PostgreSQLInsertParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
