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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.update;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.parser.MySQLUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.parser.OracleUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.parser.PostgreSQLUpdateParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.parser.SQLServerUpdateParser;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

/**
 * Update语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLUpdateParserFactory {
    
    /**
     * 创建Update语句解析器.
     * 
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Update语句解析器
     */
    public static AbstractUpdateParser newInstance(final SQLParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLUpdateParser(exprParser);
            case Oracle:
                return new OracleUpdateParser(exprParser);
            case SQLServer:
                return new SQLServerUpdateParser(exprParser);
            case PostgreSQL:
                return new PostgreSQLUpdateParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
